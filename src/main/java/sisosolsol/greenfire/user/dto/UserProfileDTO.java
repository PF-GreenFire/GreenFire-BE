package sisosolsol.greenfire.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;

@Getter
@Builder
public class UserProfileDTO {
    private ScrapbookSummaryDTO scrapbookSummary;
    private List<ChallengeDTO> challenges;
}
