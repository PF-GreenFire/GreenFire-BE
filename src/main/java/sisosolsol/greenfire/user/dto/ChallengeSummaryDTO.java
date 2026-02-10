package sisosolsol.greenfire.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;

@Getter
@Builder
public class ChallengeSummaryDTO {

    private int totalCount;
    private List<ChallengeDTO> challenges;
}
