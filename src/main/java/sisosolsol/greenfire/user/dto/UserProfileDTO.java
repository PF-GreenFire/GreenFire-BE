package sisosolsol.greenfire.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import sisosolsol.greenfire.badge.model.dto.AchievementSummaryDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;

@Getter
@Builder
public class UserProfileDTO {
    private User user;
    private ScrapbookSummaryDTO scrapbookSummary;
    private ChallengeSummaryDTO challengeSummary;
    private EchoMemorySummaryDTO echoMemorySummary;
    private AchievementSummaryDTO achievementSummary;
}
