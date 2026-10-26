package sisosolsol.greenfire.challenge.model.dto.completion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sisosolsol.greenfire.challenge.completion.CompletionRuleType;

/** 챌린지 등록·수정 요청 안의 완주 조건 한 개 */
@Getter
@Setter
@NoArgsConstructor
public class CompletionRuleRequest {
    private CompletionRuleType ruleType;
    private Integer targetCount;
}
