package sisosolsol.greenfire.challenge.completion;

import java.util.List;

/** 한 참여자에 대한 완주 정책 평가 결과 */
public record CompletionResult(boolean completed, int certifiedDays, List<RuleEvaluation> evaluations) {

    public record RuleEvaluation(CompletionRule rule, RuleProgress progress) {}
}
