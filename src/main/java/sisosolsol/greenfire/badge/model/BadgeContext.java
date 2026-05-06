package sisosolsol.greenfire.badge.model;

import sisosolsol.greenfire.common.enums.spark.Tier;

import java.util.UUID;

/**
 * 뱃지 평가에 필요한 정보 묶음. evaluator에 전달.
 *
 * 새 카운트가 필요할 때 필드 + getter 한 개씩만 추가하면 evaluator들이 재사용.
 */
public class BadgeContext {

    private final UUID userCode;
    private final Outcome outcome;       // 방금 발생한 outcome
    private final int outcomeCount;      // 해당 outcome의 누적 카운트 (spark_history)
    private final int totalSpark;
    private final Tier tier;

    public BadgeContext(UUID userCode, Outcome outcome, int outcomeCount,
                        int totalSpark, Tier tier) {
        this.userCode = userCode;
        this.outcome = outcome;
        this.outcomeCount = outcomeCount;
        this.totalSpark = totalSpark;
        this.tier = tier;
    }

    public UUID getUserCode() { return userCode; }
    public Outcome getOutcome() { return outcome; }
    public int getOutcomeCount() { return outcomeCount; }
    public int getTotalSpark() { return totalSpark; }
    public Tier getTier() { return tier; }
}
