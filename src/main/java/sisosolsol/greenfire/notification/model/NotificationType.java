package sisosolsol.greenfire.notification.model;

import lombok.Getter;

/**
 * 알림 종류. label은 화면용 prefix(이모지) 정도, 본문은 NotificationService에서 actor/resource 정보로 구성.
 * 새 종류 추가는 enum 한 줄 + 도메인 후크 한 줄.
 */
@Getter
public enum NotificationType {
    POST_LIKED         ("🌱"),  // 인증글 좋아요 받음
    POST_COMMENTED     ("💬"),  // 인증글 댓글 달림
    FOLLOWED           ("👥"),  // 팔로우 받음
    CHALLENGE_REWARDED ("🎁"),  // 챌린지 종료, 보상 받음
    TIER_REACHED       ("🏅"),  // 새 등급 도달
    BADGE_EARNED       ("🏆");  // 새 뱃지 획득

    private final String emoji;

    NotificationType(String emoji) {
        this.emoji = emoji;
    }
}
