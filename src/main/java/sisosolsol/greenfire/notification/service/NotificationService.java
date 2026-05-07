package sisosolsol.greenfire.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.notification.model.NotificationType;
import sisosolsol.greenfire.notification.model.dao.NotificationMapper;
import sisosolsol.greenfire.notification.model.dto.NotificationCreateParam;
import sisosolsol.greenfire.notification.model.dto.NotificationDTO;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 알림 단일 진입점.
 *
 * 도메인 서비스에서:
 *   notificationService.notify(recipient, NotificationType.POST_LIKED, actor, "POST", postCode);
 *
 * - actor == recipient면 무시 (자기 자신 알림 방지)
 * - 적립/뱃지처럼 부수 작업이라 실패해도 본 트랜잭션을 깨지 않게 try/catch
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    @Transactional
    public void notify(UUID recipient, NotificationType type, UUID actor,
                       String resourceType, String resourceCode, String title) {
        if (recipient == null || type == null) return;
        if (Objects.equals(recipient, actor)) return;  // 본인 액션은 알림 X
        try {
            NotificationCreateParam p = new NotificationCreateParam();
            p.setRecipientCode(recipient);
            p.setType(type.name());
            p.setActorCode(actor);
            p.setResourceType(resourceType);
            p.setResourceCode(resourceCode);
            p.setTitle(title);
            notificationMapper.insert(p);
        } catch (Exception e) {
            log.warn("notification insert failed: recipient={} type={} ({})",
                    recipient, type, e.getMessage());
        }
    }

    /** 도메인 후크용 편의 오버로드 — title은 자동으로 type.emoji + 메시지 */
    public void notify(UUID recipient, NotificationType type, UUID actor,
                       String resourceType, String resourceCode) {
        notify(recipient, type, actor, resourceType, resourceCode, defaultTitle(type));
    }

    /** 같은 (recipient, type, resource) 조합이 이미 있으면 발송 X. 챌린지 시작 등 1회성 알림용. */
    public void notifyIfAbsent(UUID recipient, NotificationType type, UUID actor,
                               String resourceType, String resourceCode) {
        if (recipient == null || type == null) return;
        if (resourceType != null && resourceCode != null) {
            int existing = notificationMapper.countByRecipientAndTypeAndResource(
                    recipient, type.name(), resourceType, resourceCode);
            if (existing > 0) return;
        }
        notify(recipient, type, actor, resourceType, resourceCode);
    }

    private String defaultTitle(NotificationType type) {
        return switch (type) {
            case POST_LIKED         -> "내 인증글에 좋아요가 도착했어요";
            case POST_COMMENTED     -> "내 인증글에 댓글이 달렸어요";
            case FOLLOWED           -> "새 팔로워가 생겼어요";
            case CHALLENGE_REWARDED -> "챌린지 보상을 받았어요";
            case TIER_REACHED       -> "등급이 올라갔어요";
            case BADGE_EARNED       -> "새 뱃지를 획득했어요";
            case CHALLENGE_STARTED  -> "참여 중인 챌린지가 시작됐어요";
            case STORE_APPROVED     -> "신청한 매장이 승인됐어요";
            case STORE_REJECTED     -> "신청한 매장이 반려됐어요";
            case REPORT_HANDLED     -> "내가 보낸 신고가 처리됐어요";
        };
    }

    public List<NotificationDTO> list(UUID recipient, boolean unreadOnly, int size) {
        if (recipient == null) return List.of();
        List<NotificationDTO> rows = notificationMapper.findByRecipient(recipient, unreadOnly, size);
        // emoji 채우기
        for (NotificationDTO n : rows) {
            try {
                n.setEmoji(NotificationType.valueOf(n.getType()).getEmoji());
            } catch (IllegalArgumentException ignored) {
                n.setEmoji("");
            }
        }
        return rows;
    }

    public int countUnread(UUID recipient) {
        if (recipient == null) return 0;
        return notificationMapper.countUnread(recipient);
    }

    @Transactional
    public void markRead(Integer notificationCode, UUID recipient) {
        notificationMapper.markRead(notificationCode, recipient);
    }

    @Transactional
    public void markAllRead(UUID recipient) {
        notificationMapper.markAllRead(recipient);
    }
}
