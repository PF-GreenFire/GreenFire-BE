package sisosolsol.greenfire.challenge.completion.rule;

import sisosolsol.greenfire.challenge.completion.CertificationLog;
import sisosolsol.greenfire.challenge.completion.CompletionRule;
import sisosolsol.greenfire.challenge.completion.CompletionRuleType;
import sisosolsol.greenfire.challenge.completion.RuleProgress;

/** 기간 중 N일 이상 인증 */
public final class TotalDaysRule implements CompletionRule {

    private final int days;

    public TotalDaysRule(int days) {
        this.days = days;
    }

    @Override
    public CompletionRuleType type() {
        return CompletionRuleType.TOTAL_DAYS;
    }

    @Override
    public Integer targetCount() {
        return days;
    }

    @Override
    public RuleProgress evaluate(CertificationLog log) {
        return new RuleProgress(log.certifiedDayCount(), days);
    }

    @Override
    public String describe() {
        return "기간 중 " + days + "일 이상 인증";
    }
}
