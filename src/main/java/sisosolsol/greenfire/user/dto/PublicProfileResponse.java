package sisosolsol.greenfire.user.dto;

import sisosolsol.greenfire.spark.model.dto.SparkInfo;

import java.util.List;
import java.util.UUID;

/** GET /user/profile/{userId} 응답 */
public record PublicProfileResponse(
        UUID userCode,
        String nickname,
        String profileKey,
        String coverKey,
        SparkInfo spark,
        long followerCount,
        long followingCount,
        boolean isFollowing,
        boolean isMe,
        List<RecentBadge> recentBadges
) {
    public record RecentBadge(String code, String label, String category, String emoji) {}
}
