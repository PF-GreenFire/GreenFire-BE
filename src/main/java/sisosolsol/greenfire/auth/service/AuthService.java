package sisosolsol.greenfire.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.auth.dto.*;
import sisosolsol.greenfire.auth.entity.PasswordResetCode;
import sisosolsol.greenfire.auth.entity.RefreshToken;
import sisosolsol.greenfire.auth.repository.PasswordResetCodeRepository;
import sisosolsol.greenfire.auth.repository.RefreshTokenRepository;
import sisosolsol.greenfire.common.service.EmailService;
import sisosolsol.greenfire.common.audit.entity.ActionType;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.common.audit.service.ActivityLogService;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.ConflictException;
import sisosolsol.greenfire.common.exception.NotFoundException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.common.security.jwt.JwtUtil;
import sisosolsol.greenfire.common.security.model.UserRole;
import sisosolsol.greenfire.user.entity.UserAccount;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
    );
    private static final int REJOIN_COOLDOWN_DAYS = 30;
    private static final int RESET_CODE_EXPIRY_MINUTES = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;

    @Transactional
    public void signup(SignupRequest req, String ipAddress) {
        // 비밀번호 강도 검증
        if (!PASSWORD_PATTERN.matcher(req.password()).matches()) {
            throw new BadRequestException(ExceptionCode.WEAK_PASSWORD);
        }

        // 탈퇴 후 30일 이내 재가입 차단
        Optional<UserAccount> existing = userAccountRepository.findByEmail(req.email());
        if (existing.isPresent()) {
            UserAccount account = existing.get();
            if (account.isDeleted()) {
                Instant cooldownEnd = account.getDeletedAt().plus(REJOIN_COOLDOWN_DAYS, ChronoUnit.DAYS);
                if (Instant.now().isBefore(cooldownEnd)) {
                    throw new ConflictException(ExceptionCode.REJOIN_COOLDOWN);
                }
            } else {
                throw new ConflictException(ExceptionCode.EMAIL_ALREADY_EXISTS);
            }
        }

        UserAccount user = new UserAccount(
                req.email(),
                passwordEncoder.encode(req.password()),
                UserRole.USER
        );
        userAccountRepository.save(user);

        activityLogService.log(user.getId(), ActionType.SIGNUP, ResourceType.ACCOUNT, null, null, ipAddress);
    }

    /**
     * 로그인: Access Token(body) + Refresh Token(DB 저장, 원문 반환 → Controller가 쿠키에 세팅)
     */
    @Transactional
    public LoginResult login(LoginRequest req, String ipAddress) {
        UserAccount user = userAccountRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 탈퇴 계정 로그인 차단
        if (user.isDeleted()) {
            throw new BadCredentialsException("탈퇴한 계정입니다. 다시 가입해 주세요.");
        }

        // 정지 계정 로그인 차단
        if (user.isSuspended()) {
            String msg = "정지된 계정입니다.";
            if (user.getSuspendReason() != null) {
                msg += " 사유: " + user.getSuspendReason();
            }
            Instant permanentThreshold = Instant.parse("9999-01-01T00:00:00Z");
            if (user.getSuspendedUntil() != null && user.getSuspendedUntil().isBefore(permanentThreshold)) {
                msg += " (해제일: " + user.getSuspendedUntil() + ")";
            } else {
                msg += " (영구 정지)";
            }
            throw new BadCredentialsException(msg);
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        long expiresIn = jwtUtil.getAccessExpSeconds();

        // Refresh Token 생성 + DB 저장
        String rawRefreshToken = jwtUtil.generateRefreshToken();
        String family = UUID.randomUUID().toString();
        saveRefreshToken(user.getId(), rawRefreshToken, family);

        activityLogService.log(user.getId(), ActionType.LOGIN, ResourceType.AUTH, null, null, ipAddress);

        TokenResponse tokenResponse = new TokenResponse(accessToken, expiresIn);
        return new LoginResult(tokenResponse, rawRefreshToken);
    }

    /**
     * Refresh: Rotation + 재사용 탐지
     *  - 이미 폐기된 토큰 → family 전체 폐기 (탈취 의심)
     *  - 유효 → 현재 폐기, 새 Refresh Token 발급 (같은 family)
     */
    @Transactional
    public RefreshResult refresh(String rawRefreshToken, String ipAddress) {
        String tokenHash = jwtUtil.hashToken(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("유효하지 않은 리프레시 토큰입니다."));

        // 재사용 탐지: 이미 폐기된 토큰이 다시 사용됨 → family 전체 폐기
        if (stored.isRevoked()) {
            log.warn("Refresh token reuse detected! family={}, userId={}", stored.getFamily(), stored.getUserId());
            refreshTokenRepository.revokeAllByFamily(stored.getFamily());
            throw new BadCredentialsException("토큰 재사용이 감지되었습니다. 다시 로그인해주세요.");
        }

        // 만료 확인
        if (stored.isExpired()) {
            stored.revoke();
            throw new BadCredentialsException("리프레시 토큰이 만료되었습니다.");
        }

        // 현재 토큰 폐기 (Rotation)
        stored.revoke();

        // 유저 조회 → 새 토큰 쌍 발급
        UserAccount user = userAccountRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));

        // 정지 계정 토큰 갱신 차단
        if (user.isSuspended()) {
            refreshTokenRepository.revokeAllByUserId(user.getId());
            throw new BadCredentialsException("정지된 계정입니다.");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        long expiresIn = jwtUtil.getAccessExpSeconds();

        // 새 Refresh Token (같은 family)
        String newRawRefreshToken = jwtUtil.generateRefreshToken();
        saveRefreshToken(user.getId(), newRawRefreshToken, stored.getFamily());

        activityLogService.log(user.getId(), ActionType.REFRESH, ResourceType.AUTH, null, null, ipAddress);

        TokenResponse tokenResponse = new TokenResponse(newAccessToken, expiresIn);
        return new RefreshResult(tokenResponse, newRawRefreshToken);
    }

    /**
     * 로그아웃: 해당 family 전체 폐기
     */
    @Transactional
    public void logout(String rawRefreshToken, String ipAddress) {
        String tokenHash = jwtUtil.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    activityLogService.log(token.getUserId(), ActionType.LOGOUT, ResourceType.AUTH, null, null, ipAddress);
                    refreshTokenRepository.revokeAllByFamily(token.getFamily());
                });
    }

    /**
     * 회원 탈퇴: 비밀번호 검증 → 소프트 삭제 → 전체 세션 폐기
     */
    @Transactional
    public void deleteAccount(UUID userId, String password, String reason, String ipAddress) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }

        // 전체 세션 폐기
        refreshTokenRepository.revokeAllByUserId(userId);

        // 소프트 삭제
        user.markDeleted(reason);

        activityLogService.log(userId, ActionType.DELETE_ACCOUNT, ResourceType.ACCOUNT, null, reason, ipAddress);
    }

    /**
     * 이메일 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        Optional<UserAccount> existing = userAccountRepository.findByEmail(email);
        if (existing.isEmpty()) {
            return true;
        }
        UserAccount account = existing.get();
        // 탈퇴 계정이고 쿨다운 지났으면 사용 가능
        if (account.isDeleted()) {
            Instant cooldownEnd = account.getDeletedAt().plus(REJOIN_COOLDOWN_DAYS, ChronoUnit.DAYS);
            return Instant.now().isAfter(cooldownEnd);
        }
        return false;
    }

    /**
     * 아이디 찾기: 이메일로 가입 여부 확인 (마스킹 처리)
     */
    @Transactional(readOnly = true)
    public FindEmailResponse findEmail(String email) {
        Optional<UserAccount> existing = userAccountRepository.findByEmail(email);
        if (existing.isEmpty() || existing.get().isDeleted()) {
            return new FindEmailResponse(false, null);
        }
        return new FindEmailResponse(true, maskEmail(email));
    }

    /**
     * 비밀번호 재설정: 인증코드 발송
     */
    @Transactional
    public void sendResetCode(String email) {
        UserAccount user = userAccountRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.USER_NOT_FOUND));

        // 기존 코드 삭제
        passwordResetCodeRepository.deleteByEmail(email);

        // 6자리 랜덤 코드 생성
        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plus(RESET_CODE_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        passwordResetCodeRepository.save(new PasswordResetCode(email, code, expiresAt));
        emailService.sendPasswordResetCode(email, code);
    }

    /**
     * 비밀번호 재설정: 인증코드 검증
     */
    @Transactional
    public void verifyResetCode(String email, String code) {
        PasswordResetCode resetCode = passwordResetCodeRepository
                .findByEmailAndCodeAndVerifiedFalse(email, code)
                .orElseThrow(() -> new BadRequestException(ExceptionCode.RESET_CODE_INVALID));

        if (resetCode.isExpired()) {
            throw new BadRequestException(ExceptionCode.RESET_CODE_EXPIRED);
        }

        resetCode.markVerified();
    }

    /**
     * 비밀번호 재설정: 새 비밀번호 설정
     */
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        // verified 코드 확인
        PasswordResetCode resetCode = passwordResetCodeRepository
                .findByEmailAndCodeAndVerifiedTrue(email, code)
                .orElseThrow(() -> new BadRequestException(ExceptionCode.RESET_CODE_NOT_VERIFIED));

        if (resetCode.isExpired()) {
            throw new BadRequestException(ExceptionCode.RESET_CODE_EXPIRED);
        }

        // 비밀번호 강도 검증
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new BadRequestException(ExceptionCode.WEAK_PASSWORD);
        }

        UserAccount user = userAccountRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(newPassword));

        // 코드 삭제 + 전체 세션 폐기
        passwordResetCodeRepository.deleteByEmail(email);
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email;
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.length() <= 2) return local.charAt(0) + "*" + domain;
        return local.charAt(0) + "*".repeat(local.length() - 2) + local.charAt(local.length() - 1) + domain;
    }

    private void saveRefreshToken(UUID userId, String rawToken, String family) {
        String hash = jwtUtil.hashToken(rawToken);
        Instant expiresAt = Instant.now().plusMillis(jwtUtil.getRefreshExpMillis());
        RefreshToken entity = new RefreshToken(userId, hash, family, expiresAt);
        refreshTokenRepository.save(entity);
    }

    /** login() 결과: body 응답 + 쿠키용 raw refresh token */
    public record LoginResult(TokenResponse tokenResponse, String rawRefreshToken) {}

    /** refresh() 결과: body 응답 + 쿠키용 raw refresh token */
    public record RefreshResult(TokenResponse tokenResponse, String rawRefreshToken) {}
}
