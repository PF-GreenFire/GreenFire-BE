package sisosolsol.greenfire.badge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.badge.model.Badge;
import sisosolsol.greenfire.badge.model.BadgeContext;
import sisosolsol.greenfire.badge.model.Outcome;
import sisosolsol.greenfire.badge.model.dao.BadgeMapper;
import sisosolsol.greenfire.badge.model.dto.UserBadgeRecord;
import sisosolsol.greenfire.common.enums.spark.Tier;
import sisosolsol.greenfire.spark.model.dao.SparkMapper;

import java.util.*;

/**
 * 뱃지 매핑 진입점.
 *
 * 도메인은 spark_history에 outcome 발행만 한다 (SparkService.award).
 * 그 직후 SparkService가 checkAfterOutcome을 1회 호출 → enum 순회 → 조건 충족 뱃지 INSERT.
 *
 * 조건 검사 자체는 BadgeEvaluator 구현체에 위임. 여기는 데이터 흐름만.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeMapper badgeMapper;
    private final SparkMapper sparkMapper;
    private final sisosolsol.greenfire.notification.service.NotificationService notificationService;

    @Transactional
    public List<Badge> checkAfterOutcome(UUID userCode, Outcome outcome, int totalSpark) {
        if (userCode == null || outcome == null) return Collections.emptyList();

        try {
            int outcomeCount = sparkMapper.countHistory(userCode, outcome.getAction(), null, null);
            Tier tier = Tier.of(totalSpark);
            BadgeContext ctx = new BadgeContext(userCode, outcome, outcomeCount, totalSpark, tier);

            Set<String> owned = new HashSet<>(badgeMapper.findOwnedCodes(userCode));
            List<Badge> awarded = new ArrayList<>();

            for (Badge b : Badge.values()) {
                if (b.getOutcome() != outcome) continue;
                if (owned.contains(b.name())) continue;
                if (!b.getEvaluator().evaluate(ctx)) continue;

                badgeMapper.insert(userCode, b.name());
                awarded.add(b);
                notificationService.notify(userCode,
                        sisosolsol.greenfire.notification.model.NotificationType.BADGE_EARNED,
                        null, "BADGE", b.name());
            }
            return awarded;
        } catch (Exception e) {
            log.warn("badge check failed: user={} outcome={} ({})", userCode, outcome, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<UserBadgeRecord> getUserBadges(UUID userCode) {
        return badgeMapper.findUserBadges(userCode);
    }

    @Transactional
    public void markViewed(UUID userCode, String badgeCode) {
        badgeMapper.markViewed(userCode, badgeCode);
    }
}
