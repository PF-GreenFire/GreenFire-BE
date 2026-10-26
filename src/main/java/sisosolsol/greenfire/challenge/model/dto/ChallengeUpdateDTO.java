package sisosolsol.greenfire.challenge.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChallengeUpdateDTO {

    private String challengeTitle;
    private String challengeContent;
    private Integer recruitmentNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer xp;
    private String thumbnailUrl;
    private Integer challengeCategoryCode;
    /** 완주 조건. 등록 시 비우면 기본 조건, 수정 시 null이면 기존 조건 유지 */
    private java.util.List<sisosolsol.greenfire.challenge.model.dto.completion.CompletionRuleRequest> completionRules;
}
