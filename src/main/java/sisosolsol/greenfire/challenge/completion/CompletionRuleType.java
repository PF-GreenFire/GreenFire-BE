package sisosolsol.greenfire.challenge.completion;

import lombok.Getter;
import sisosolsol.greenfire.challenge.completion.rule.EveryDayRule;
import sisosolsol.greenfire.challenge.completion.rule.StreakRule;
import sisosolsol.greenfire.challenge.completion.rule.TotalDaysRule;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;

/**
 * 완주 조건 유형 목록이자 생성 팩토리.
 * 횟수 허용 범위도 여기서만 정의한다 (FE 편집기도 /completion-rule-types 응답으로 같은 범위를 쓴다).
 * 횟수 상한은 모든 유형에서 기간 일수다.
 */
@Getter
public enum CompletionRuleType {

    TOTAL_DAYS("누적 인증", "기간 중 N일 이상 인증해요", true, 1) {
        @Override
        protected CompletionRule newRule(Integer count) {
            return new TotalDaysRule(count);
        }
    },

    EVERY_DAY("매일 인증", "기간 동안 하루도 빠짐없이 인증해요", false, null) {
        @Override
        protected CompletionRule newRule(Integer count) {
            return new EveryDayRule();
        }
    },

    STREAK("연속 인증", "N일 연속으로 인증해요", true, 2) {
        @Override
        protected CompletionRule newRule(Integer count) {
            return new StreakRule(count);
        }
    };

    private final String label;
    private final String guide;
    private final boolean requiresCount;
    private final Integer minCount;

    CompletionRuleType(String label, String guide, boolean requiresCount, Integer minCount) {
        this.label = label;
        this.guide = guide;
        this.requiresCount = requiresCount;
        this.minCount = minCount;
    }

    protected abstract CompletionRule newRule(Integer count);

    /** 사용자 입력으로 조건 생성 — 기간에 맞는 횟수인지 검증한다 */
    public CompletionRule create(Integer count, ChallengePeriod period) {
        if (requiresCount && (count == null || count < minCount || count > maxCount(period))) {
            throw new CustomException(ExceptionCode.COMPLETION_RULE_INVALID_COUNT);
        }
        return newRule(requiresCount ? count : null);
    }

    /** 저장된 조건 복원 — 저장 시점에 이미 검증했으므로 다시 검증하지 않는다 */
    public CompletionRule restore(Integer count) {
        return newRule(requiresCount ? count : null);
    }

    /** 이 기간에서 설정할 수 있는 최대 횟수 */
    public int maxCount(ChallengePeriod period) {
        return period.days();
    }
}
