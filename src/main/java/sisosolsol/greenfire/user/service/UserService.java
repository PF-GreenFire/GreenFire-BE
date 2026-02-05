package sisosolsol.greenfire.user.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.common.config.UploadAllowConfig;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.user.dao.UserMapper;
import sisosolsol.greenfire.user.dto.ChallengeSummaryDTO;
import sisosolsol.greenfire.user.dto.PasswordChangeRequest;
import sisosolsol.greenfire.user.dto.ScrapbookSummaryDTO;
import sisosolsol.greenfire.user.dto.UpdateUserCommand;
import sisosolsol.greenfire.user.dto.User;
import sisosolsol.greenfire.user.dto.UserProfileDTO;
import sisosolsol.greenfire.user.dto.UpdateUserDTO;

import java.util.Optional;
import sisosolsol.greenfire.user.exception.InvalidPasswordException;
import sisosolsol.greenfire.user.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UploadAllowConfig config;
    private static final String FIXED_FILENAME = "profile.jpg";
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

    @Transactional
    public User updateUserProfile(UUID userCode, UpdateUserDTO request, MultipartFile file) {
        User user = Optional.ofNullable(userMapper.findByUserCode(userCode))
                .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("저장할 이미지 파일이 없습니다.");
        }

        String storageKey = buildStorageKey(userCode);
        Path savePath = resolveAbsolutePath(storageKey);
        Path saveDirectory = savePath.getParent();

        try (InputStream is = file.getInputStream()) {
            Files.createDirectories(saveDirectory);
            Files.copy(is, savePath, StandardCopyOption.REPLACE_EXISTING);
        }
         catch (Exception e) {
            throw new RuntimeException("Failed to create user image directory: " + saveDirectory, e);
        }

        UpdateUserCommand updateUserCommand = UpdateUserCommand.of(request, storageKey);
        userMapper.updateUserProfile(updateUserCommand);

        return userMapper.findByUserCode(userCode);
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

    public String buildStorageKey(UUID userCode) {
        return "users/" + userCode + "/" + FIXED_FILENAME;
    }

    public Path resolveAbsolutePath(String storageKey) {
        Path root = Paths.get(config.getDirectory()).toAbsolutePath().normalize();
        Path resolved = root.resolve(storageKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("잘못된 저장 경로입니다.");
        }
        return resolved;
    }
}