package sisosolsol.greenfire.challenge.model.dto.completion;

import sisosolsol.greenfire.challenge.completion.CompletionRuleType;

/**
 * 완주 조건 편집기용 유형 메타데이터. 최대 횟수는 모든 유형에서 챌린지 기간 일수.
 */
public record CompletionRuleTypeResponse(String type, String label, String guide,
                                         boolean requiresCount, Integer minCount) {

    public static CompletionRuleTypeResponse from(CompletionRuleType type) {
        return new CompletionRuleTypeResponse(type.name(), type.getLabel(), type.getGuide(),
                type.isRequiresCount(), type.getMinCount());
    }
}
