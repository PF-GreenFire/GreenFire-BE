package sisosolsol.greenfire.challenge.completion.rule;

import sisosolsol.greenfire.challenge.completion.CertificationLog;
import sisosolsol.greenfire.challenge.completion.CompletionRule;
import sisosolsol.greenfire.challenge.completion.CompletionRuleType;
import sisosolsol.greenfire.challenge.completion.RuleProgress;

/** 기간 중 한 번이라도 N일 연속으로 인증 */
public final class StreakRule implements CompletionRule {

    private final int length;

    public StreakRule(int length) {
        this.length = length;
    }

    @Override
    public CompletionRuleType type() {
        return CompletionRuleType.STREAK;
    }

    @Override
    public Integer targetCount() {
        return length;
    }

    @Override
    public RuleProgress evaluate(CertificationLog log) {
        return new RuleProgress(Math.min(log.longestStreak(), length), length);
    }

    @Override
    public String describe() {
        return length + "일 연속 인증";
    }
}
