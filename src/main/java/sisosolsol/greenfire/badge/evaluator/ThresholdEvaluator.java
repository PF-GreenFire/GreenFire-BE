package sisosolsol.greenfire.badge.evaluator;

import sisosolsol.greenfire.badge.model.BadgeContext;

/**
 * 가장 흔한 패턴: 특정 outcome의 누적 카운트가 임계 이상이면 부여.
 * 예) POST_CREATED 누적 10회 → POST_10 뱃지.
 */
public class ThresholdEvaluator implements BadgeEvaluator {

    private final int threshold;

    public ThresholdEvaluator(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean evaluate(BadgeContext context) {
        return context.getOutcomeCount() >= threshold;
    }

    public int getThreshold() {
        return threshold;
    }
}
