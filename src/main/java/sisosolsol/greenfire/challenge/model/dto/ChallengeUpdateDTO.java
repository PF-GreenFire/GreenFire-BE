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
}
