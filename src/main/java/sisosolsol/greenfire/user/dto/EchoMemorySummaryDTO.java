package sisosolsol.greenfire.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EchoMemorySummaryDTO {

    private int postCount;
    private int followers;
    private int followings;
}
