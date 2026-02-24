package sisosolsol.greenfire.user.dto;

import java.util.UUID;
import lombok.Getter;

@Getter
public class FriendDTO {

    private UUID userCode;
    private String nickname;
    private String profileImage;
    private boolean isFollowing;
    private boolean isFollower;
}
