package sisosolsol.greenfire.challenge.completion;

import sisosolsol.greenfire.challenge.completion.rule.TotalDaysRule;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 챌린지의 완주 정책 = 완주 조건들의 묶음. 모든 조건을 충족해야 완주(AND).
 * 조건을 설정하지 않은 챌린지는 기본 정책("기간 중 1일 이상 인증")을 쓴다.
 */
public final class CompletionPolicy {

    public static final int MAX_RULES = 3;

    private final List<CompletionRule> rules;

    private CompletionPolicy(List<CompletionRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static CompletionPolicy defaultPolicy() {
        return new CompletionPolicy(List.of(new TotalDaysRule(1)));
    }

    /** 조건 목록 검증 후 정책 생성. 비어 있으면 기본 정책 */
    public static CompletionPolicy of(List<CompletionRule> rules) {
        if (rules == null || rules.isEmpty()) return defaultPolicy();
        if (rules.size() > MAX_RULES) {
            throw new CustomException(ExceptionCode.COMPLETION_RULE_TOO_MANY);
        }
        Set<CompletionRuleType> seen = EnumSet.noneOf(CompletionRuleType.class);
        for (CompletionRule rule : rules) {
            if (!seen.add(rule.type())) {
                throw new CustomException(ExceptionCode.COMPLETION_RULE_DUPLICATED);
            }
        }
        return new CompletionPolicy(rules);
    }

    public List<CompletionRule> rules() {
        return rules;
    }

    public CompletionResult evaluate(CertificationLog log) {
        List<CompletionResult.RuleEvaluation> evaluations = new ArrayList<>();
        boolean completed = true;
        for (CompletionRule rule : rules) {
            RuleProgress progress = rule.evaluate(log);
            completed &= progress.satisfied();
            evaluations.add(new CompletionResult.RuleEvaluation(rule, progress));
        }
        return new CompletionResult(completed, log.certifiedDayCount(), evaluations);
    }
}
