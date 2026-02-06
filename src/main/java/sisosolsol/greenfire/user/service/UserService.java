package sisosolsol.greenfire.user.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.user.dao.UserMapper;
import sisosolsol.greenfire.user.dto.ChallengeSummaryDTO;
import sisosolsol.greenfire.user.dto.PasswordChangeRequest;
import sisosolsol.greenfire.user.dto.FileStorage;
import sisosolsol.greenfire.user.dto.ScrapbookSummaryDTO;
import sisosolsol.greenfire.user.dto.UpdateUserCommand;
import sisosolsol.greenfire.user.dto.User;
import sisosolsol.greenfire.user.dto.UserProfileDTO;
import sisosolsol.greenfire.user.dto.UpdateUserDTO;

import sisosolsol.greenfire.user.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final FileStorage fileStorage;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User getUserProfile(UUID userCode) {
//        try {
//            return Optional.ofNullable(userMapper.findByUserCode(userCode))
//                    .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));
//        } catch (DataIntegrityViolationException e) {
//            throw new BadRequestException(ExceptionCode.INVALID_FOREIGN_KEY);
//        } catch (DataAccessException e) {
//            throw new BadRequestException(ExceptionCode.DATABASE_ACCESS_ERROR);
//        }
        return userMapper.findByUserCode(userCode);
    }

    /**
     * TODO
     * 1. 프로필 이미지 지우기 일 때
     *  - DB에 profile_key를 null이나 빈 값으로 업데이트 하고 파일 삭제
     * 2. 프로필 이미지 아닌 다른거 업데이트
     *  - DB에 profile_key는 업데이트 X
     * 3. 프로필 이미지 업데이트
     *  - DB에 profile_key 업데이트 O
     *
     *  Transactional 쓸거면 DB 먼저 타고 파일 삭제 ㄱㄱ
     */
    @Transactional
    public User updateUserProfile(UUID userCode, UpdateUserDTO request, MultipartFile file) {
        // 1. 프로필 이미지 처리 (storageKey 결정 + 파일 저장)
        String storageKey = resolveProfileImageKey(request, file, userCode);

        // 2. DB 업데이트
        UpdateUserCommand command = UpdateUserCommand.of(userCode, request, storageKey);
        userMapper.updateUserProfile(command);

        // 3. 이미지 삭제 요청인 경우, 트랜잭션 커밋 후 파일 삭제
        if (request.isDeleteProfileImage()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileStorage.delete("users/" + userCode + "/profile.jpg");
                }
            });
        }
        return userMapper.findByUserCode(userCode);
    }

    /**
     * 프로필 이미지 storageKey를 결정합니다.
     * - null  : 이미지 변경 없음 (기존 유지)
     * - ""    : 이미지 삭제 요청
     * - 그 외 : 새 이미지 업로드 경로
     */
    private String resolveProfileImageKey(UpdateUserDTO request, MultipartFile file, UUID userCode) {
        if (request.isDeleteProfileImage()) {
            return "";
        }
        if (file != null && !file.isEmpty()) {
            return fileStorage.save("users/" + userCode + "/profile.jpg", file);
        }
        return null;
    }

    public UserProfileDTO getUserSummaryData(UUID userCode) {
        ScrapbookSummaryDTO scrapbookSummary = userMapper.getScrapbookSummary(userCode);

        int challengeTotalCount = userMapper.countParticipatingChallenge(userCode);
        List<ChallengeDTO> challenges = userMapper.getChallengeSummary(userCode);
        ChallengeSummaryDTO challengeSummary = ChallengeSummaryDTO.builder()
                                                                .totalCount(challengeTotalCount)
                                                                .challenges(challenges)
                                                                .build();

        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
                                                    .scrapbookSummary(scrapbookSummary)
                                                    .challengeSummary(challengeSummary)
                                                    .build();
        return userProfileDTO;
    }

    @Transactional
    public void changePassword(UUID userCode, PasswordChangeRequest request) {
        User user = userMapper.findByUserCode(userCode);
        if (user == null) {
            throw new UserNotFoundException(ExceptionCode.USER_NOT_FOUND);
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호와 현재 비밀번호 동일 체크
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호와 다른 비밀번호를 입력해주세요");
        }

        userMapper.changePassword(userCode, passwordEncoder.encode(request.newPassword()));
    }

    public void deleteUser(UUID testUserCode) {
        userMapper.deleteUser(testUserCode);
    }
}