package sisosolsol.greenfire.user.service;

import java.nio.file.Path;
import java.nio.file.Paths;
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
import sisosolsol.greenfire.common.config.UploadAllowConfig;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.NotFoundException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.user.dao.UserMapper;
import sisosolsol.greenfire.user.dto.ChallengeSummaryDTO;
import sisosolsol.greenfire.user.dto.EchoMemorySummaryDTO;
import sisosolsol.greenfire.user.dto.FriendDTO;
import sisosolsol.greenfire.user.dto.PasswordChangeRequest;
import sisosolsol.greenfire.user.dto.FileStorage;
import sisosolsol.greenfire.user.dto.ScrapbookSummaryDTO;
import sisosolsol.greenfire.user.dto.UpdateCoverImageDTO;
import sisosolsol.greenfire.user.dto.UpdateUserCommand;
import sisosolsol.greenfire.user.dto.User;
import sisosolsol.greenfire.user.dto.UserProfileDTO;
import sisosolsol.greenfire.user.dto.UpdateUserDTO;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
    );

    private final UploadAllowConfig uploadAllowConfig;
    private final FileStorage fileStorage;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final sisosolsol.greenfire.badge.service.BadgeService badgeService;
    private final sisosolsol.greenfire.notification.service.NotificationService notificationService;
    private final sisosolsol.greenfire.post.model.dao.PostMapper postMapper;

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
        String storageKey = resolveStoreKey(request.isDeleteProfileImage(), file, "users/" + userCode + "/profile.jpg");

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
     * 프로필 이미지 storageKey 반환
     * - null  : 이미지 변경 없음 (기존 유지)
     * - ""    : 이미지 삭제 요청
     * - 그 외 : 새 이미지 업로드 경로
     */
    private String resolveStoreKey(boolean deleteImage, MultipartFile file, String storageKey) {
        if (deleteImage) {
            return "";
        }
        if (file != null && !file.isEmpty()) {
            return fileStorage.save(storageKey, file);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getMyChallenges(UUID userCode, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        List<ChallengeDTO> challenges = userMapper.findMyChallenges(userCode, offset, safeSize);
        int totalCount = userMapper.countParticipatingChallenge(userCode);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("challenges", challenges);
        result.put("totalCount", totalCount);
        result.put("currentPage", safePage);
        result.put("hasMore", safePage * safeSize < totalCount);
        return result;
    }

    public UserProfileDTO getUserSummaryData(UUID userCode) {
        ScrapbookSummaryDTO scrapbookSummary = userMapper.getScrapbookSummary(userCode);

        int challengeCount = userMapper.countParticipatingChallenge(userCode);
        List<ChallengeDTO> challenges = userMapper.getChallengeSummary(userCode);

        ChallengeSummaryDTO challengeSummary = ChallengeSummaryDTO.builder()
                                                                .totalCount(challengeCount)
                                                                .challenges(challenges)
                                                                .build();


        int postCount = postMapper.countPostsByUserCode(userCode);
        int followers = userMapper.countFollowers(userCode);
        int followings = userMapper.countFollowings(userCode);

        EchoMemorySummaryDTO echoMemorySummary = EchoMemorySummaryDTO.builder()
                                                                .postCount(postCount)
                                                                .followers(followers)
                                                                .followings(followings)
                                                                .build();

        User user = userMapper.findUserSummary(userCode);

        // 뱃지 컬렉션 — Badge enum 전체 + 사용자 보유분 합쳐서 응답
        java.util.List<sisosolsol.greenfire.badge.model.dto.UserBadgeRecord> ownedRecords = badgeService.getUserBadges(userCode);
        java.util.Map<String, sisosolsol.greenfire.badge.model.dto.UserBadgeRecord> ownedMap = new java.util.HashMap<>();
        for (sisosolsol.greenfire.badge.model.dto.UserBadgeRecord r : ownedRecords) {
            ownedMap.put(r.getBadgeCode(), r);
        }

        java.util.List<sisosolsol.greenfire.badge.model.dto.AchievementBadgeDTO> achievementList = new java.util.ArrayList<>();
        for (sisosolsol.greenfire.badge.model.Badge b : sisosolsol.greenfire.badge.model.Badge.values()) {
            sisosolsol.greenfire.badge.model.dto.UserBadgeRecord owned = ownedMap.get(b.name());
            boolean unlocked = owned != null;
            boolean viewed = unlocked && owned.isViewed();
            achievementList.add(
                sisosolsol.greenfire.badge.model.dto.AchievementBadgeDTO.builder()
                    .id(b.name())
                    .name(b.getLabel())
                    .category(b.getCategory())
                    .description(b.getDescription())
                    .image(b.getEmoji())
                    .unlocked(unlocked)
                    .unlockedDate(owned != null ? owned.getEarnedAt() : null)
                    .isNew(unlocked && !viewed)
                    .isViewed(viewed)
                    .build()
            );
        }
        sisosolsol.greenfire.badge.model.dto.AchievementSummaryDTO achievementSummary =
            sisosolsol.greenfire.badge.model.dto.AchievementSummaryDTO.builder()
                .totalCount(ownedRecords.size())
                .totalDefined(sisosolsol.greenfire.badge.model.Badge.values().length)
                .achievements(achievementList)
                .build();

        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
                                                    .user(user)
                                                    .scrapbookSummary(scrapbookSummary)
                                                    .challengeSummary(challengeSummary)
                                                    .echoMemorySummary(echoMemorySummary)
                                                    .achievementSummary(achievementSummary)
                                                    .build();
        return userProfileDTO;
    }

    /** 다른 사용자 공개 프로필. viewerCode null 가능(비로그인 조회) */
    public sisosolsol.greenfire.user.dto.PublicProfileResponse getPublicProfile(UUID targetCode, UUID viewerCode) {
        sisosolsol.greenfire.user.dto.PublicProfileRow row = userMapper.findPublicProfile(targetCode, viewerCode);
        if (row == null) return null;

        int total = row.getTotalSpark() == null ? 0 : row.getTotalSpark();
        sisosolsol.greenfire.spark.model.dto.SparkInfo spark = sisosolsol.greenfire.spark.model.dto.SparkInfo.from(total);
        boolean isMe = viewerCode != null && viewerCode.equals(targetCode);

        // 보유 뱃지 중 최근 3개만 응답에 노출
        java.util.List<sisosolsol.greenfire.user.dto.PublicProfileResponse.RecentBadge> recent =
                badgeService.getUserBadges(targetCode).stream()
                        .limit(3)
                        .map(rec -> {
                            try {
                                sisosolsol.greenfire.badge.model.Badge b =
                                        sisosolsol.greenfire.badge.model.Badge.valueOf(rec.getBadgeCode());
                                return new sisosolsol.greenfire.user.dto.PublicProfileResponse.RecentBadge(
                                        b.name(), b.getLabel(), b.getCategory(), b.getEmoji());
                            } catch (IllegalArgumentException e) {
                                return null; // 코드 미상 뱃지는 응답에서 제외 (rename 등 대응)
                            }
                        })
                        .filter(java.util.Objects::nonNull)
                        .toList();

        return new sisosolsol.greenfire.user.dto.PublicProfileResponse(
                row.getUserCode(),
                row.getNickname(),
                row.getProfileKey(),
                row.getCoverKey(),
                spark,
                row.getFollowerCount(),
                row.getFollowingCount(),
                row.isFollowing(),
                isMe,
                recent
        );
    }

    public List<ChallengeDTO> getScrapChallenges(UUID userCode) {
        return userMapper.getScrapChallenges(userCode);
    }

    public List<FriendDTO> getScrapFriends(UUID userCode) {
        return userMapper.getScrapFriends(userCode);
    }

    @Transactional
    public void changePassword(UUID userCode, PasswordChangeRequest request) {
        User user = userMapper.findByUserCode(userCode);
        if (user == null) {
            throw new NotFoundException(ExceptionCode.USER_NOT_FOUND);
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException(ExceptionCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호와 현재 비밀번호 동일 체크
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException(ExceptionCode.SAME_PASSWORD);
        }

        // 비밀번호 강도 검증
        if (!PASSWORD_PATTERN.matcher(request.newPassword()).matches()) {
            throw new BadRequestException(ExceptionCode.WEAK_PASSWORD);
        }

        userMapper.changePassword(userCode, passwordEncoder.encode(request.newPassword()));
    }

    public void followUser(UUID userCode, UUID targetUser) {
        userMapper.followUser(userCode, targetUser);
        notificationService.notify(targetUser,
                sisosolsol.greenfire.notification.model.NotificationType.FOLLOWED,
                userCode, "USER", userCode.toString());
    }

    public void deleteFollow(UUID userCode, UUID targetUser) {
        userMapper.deleteFollow(userCode, targetUser);
    }

    public String changeCoverImage(UUID userCode, UpdateCoverImageDTO request, MultipartFile file) {
        String storageKey = resolveStoreKey(request.isDeleteProfileImage(), file, "users/" + userCode + "/cover.jpg");

        userMapper.changeCoverImage(userCode, storageKey);
        return storageKey;
    }

    public String findImagePathByImageCode(int profileImageCode) {
        return userMapper.findImagePathByImageCode(profileImageCode);
    }

    public Path getProfileImage(int profileImageCode) {
        return getPath(profileImageCode);
    }

    public Path getCoverImage(int coverImageCode) {
        return getPath(coverImageCode);
    }

    private Path getPath(int coverImageCode) {
        String storedKey = userMapper.findImagePathByImageCode(coverImageCode);

        Path filePath = Paths.get(uploadAllowConfig.getDirectory(), storedKey);
        return filePath;
    }
}