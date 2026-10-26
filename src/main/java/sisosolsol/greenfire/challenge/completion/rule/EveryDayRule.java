package sisosolsol.greenfire.challenge.completion.rule;

import sisosolsol.greenfire.challenge.completion.CertificationLog;
import sisosolsol.greenfire.challenge.completion.CompletionRule;
import sisosolsol.greenfire.challenge.completion.CompletionRuleType;
import sisosolsol.greenfire.challenge.completion.RuleProgress;

/** 기간 동안 매일 인증 (목표 일수 = 기간 일수) */
public final class EveryDayRule implements CompletionRule {

    @Override
    public CompletionRuleType type() {
        return CompletionRuleType.EVERY_DAY;
    }

    @Override
    public Integer targetCount() {
        return null;
    }

    @Override
    public RuleProgress evaluate(CertificationLog log) {
        return new RuleProgress(log.certifiedDayCount(), log.period().days());
    }

    @Override
    public String describe() {
        return "기간 동안 매일 인증";
    }
}
