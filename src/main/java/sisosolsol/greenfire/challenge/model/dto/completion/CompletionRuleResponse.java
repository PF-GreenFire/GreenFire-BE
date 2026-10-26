package sisosolsol.greenfire.challenge.model.dto.completion;

import sisosolsol.greenfire.challenge.completion.CompletionRule;

/** 챌린지 상세에 노출하는 완주 조건 */
public record CompletionRuleResponse(String ruleType, String label, Integer targetCount, String description) {

    public static CompletionRuleResponse from(CompletionRule rule) {
        return new CompletionRuleResponse(rule.type().name(), rule.type().getLabel(),
                rule.targetCount(), rule.describe());
    }
}
