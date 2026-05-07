package sisosolsol.greenfire.spark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.badge.model.Outcome;
import sisosolsol.greenfire.badge.service.BadgeService;
import sisosolsol.greenfire.common.enums.spark.Tier;
import sisosolsol.greenfire.spark.model.dao.SparkMapper;

import java.util.UUID;

@Slf4j
@Service
public class SparkService {

    private final SparkMapper sparkMapper;
    private final BadgeService badgeService;
    private final sisosolsol.greenfire.notification.service.NotificationService notificationService;

    // BadgeService(SparkMapper 사용) ↔ SparkService 잠재 순환을 끊기 위해 lazy
    public SparkService(SparkMapper sparkMapper,
                        @Lazy @Autowired BadgeService badgeService,
                        sisosolsol.greenfire.notification.service.NotificationService notificationService) {
        this.sparkMapper = sparkMapper;
        this.badgeService = badgeService;
        this.notificationService = notificationService;
    }

    public static final int LIKE_PER_REWARD = 10;   // 좋아요 N개 단위로 +1
    public static final int LIKE_REWARD_CAP = 10;   // 게시물당 LIKE_RECEIVED 적립 횟수 상한

    /**
     * 점수 적립의 단일 진입점.
     *
     * @param userCode    적립 대상
     * @param amount      적립량 (양수)
     * @param action      POST_CREATE / STORE_APPROVED / LIKE_RECEIVED / CHALLENGE_COMPLETE / DAILY_FIRST 등
     * @param sourceType  POST / STORE / CHALLENGE 등 (NULL 가능)
     * @param sourceCode  해당 도메인 PK (NULL 가능)
     * @return            갱신된 누적 spark
     */
    @Transactional
    public int award(UUID userCode, int amount, String action, String sourceType, Integer sourceCode) {
        if (userCode == null || amount <= 0) return 0;
        int total;
        try {
            total = sparkMapper.addSpark(userCode, amount);
            sparkMapper.insertHistory(userCode, action, amount, sourceType, sourceCode);
        } catch (Exception e) {
            log.warn("spark award failed: user={} action={} amount={} ({})",
                    userCode, action, amount, e.getMessage());
            return 0;
        }

        // 등급 변동 감지 → 알림
        sisosolsol.greenfire.common.enums.spark.Tier prevTier =
                sisosolsol.greenfire.common.enums.spark.Tier.of(total - amount);
        sisosolsol.greenfire.common.enums.spark.Tier newTier =
                sisosolsol.greenfire.common.enums.spark.Tier.of(total);
        if (newTier != prevTier) {
            notificationService.notify(userCode,
                    sisosolsol.greenfire.notification.model.NotificationType.TIER_REACHED,
                    null, "USER", userCode.toString());
        }

        // 뱃지 후크 (실패해도 본 트랜잭션 깨지 않게 BadgeService 내부 try/catch)
        Outcome outcome = mapToOutcome(action);
        if (outcome != null) badgeService.checkAfterOutcome(userCode, outcome, total);
        // 등급 도달 뱃지는 outcomeCount 무관하므로 항상 검사
        badgeService.checkAfterOutcome(userCode, Outcome.TIER_REACHED, total);
        return total;
    }

    private Outcome mapToOutcome(String action) {
        for (Outcome o : Outcome.values()) {
            if (o.getAction().equals(action)) return o;
        }
        return null;
    }

    public int getTotalSpark(UUID userCode) {
        if (userCode == null) return 0;
        return sparkMapper.getTotalSpark(userCode);
    }

    public Tier getTier(UUID userCode) {
        return Tier.of(getTotalSpark(userCode));
    }

    /** 좋아요 수신에 의한 적립이 게시물당 캡 도달했는지 */
    public boolean isLikeRewardCapped(UUID userCode, int postCode) {
        return sparkMapper.countHistory(userCode, "LIKE_RECEIVED", "POST", postCode) >= LIKE_REWARD_CAP;
    }
}
