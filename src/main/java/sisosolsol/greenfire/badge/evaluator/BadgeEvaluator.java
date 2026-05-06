package sisosolsol.greenfire.badge.evaluator;

import sisosolsol.greenfire.badge.model.BadgeContext;

/**
 * 뱃지 획득 조건을 평가하는 인터페이스.
 *
 * 단순 누적 카운트 임계는 ThresholdEvaluator로 충분.
 * 시간 윈도우, 스트릭, 복합 조건 등은 별도 구현체로 추가:
 *   - WithinDaysEvaluator(days, threshold)
 *   - StreakEvaluator(consecutiveDays)
 *   - CompositeEvaluator(eval1, eval2, AND/OR)
 *
 * Badge enum의 각 entry가 evaluator 인스턴스를 가지고, BadgeService가 evaluate(ctx) 호출.
 */
public interface BadgeEvaluator {
    boolean evaluate(BadgeContext context);
}
