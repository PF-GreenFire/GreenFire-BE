package sisosolsol.greenfire.challenge.model.dto.completion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** challenge_completion_rule 테이블 행 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompletionRuleRow {
    private String ruleType;
    private Integer targetCount;
    private Integer ruleOrder;
}
