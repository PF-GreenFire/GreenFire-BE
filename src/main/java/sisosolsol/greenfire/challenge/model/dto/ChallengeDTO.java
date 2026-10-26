package sisosolsol.greenfire.challenge.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sisosolsol.greenfire.common.enums.challenge.ChallengeStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class ChallengeDTO {
    private Integer challengeCode;
    private OffsetDateTime createdAt;
    private String challengeTitle;
    private String challengeContent;
    private UUID hostUser;
    private Integer recruitmentNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer xp;
    private String thumbnailUrl;
    private Integer challengeCategoryCode;
    private ChallengeStatus challengeStatus;
    /** 완주 조건 (상세 조회 시에만 채움) */
    private java.util.List<sisosolsol.greenfire.challenge.model.dto.completion.CompletionRuleResponse> completionRules;
}
