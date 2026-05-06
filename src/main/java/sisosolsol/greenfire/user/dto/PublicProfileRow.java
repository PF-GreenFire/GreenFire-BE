package sisosolsol.greenfire.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** UserMapper.findPublicProfile 결과 매핑용 raw row */
@Getter
@Setter
public class PublicProfileRow {
    private UUID userCode;
    private String nickname;
    private String profileKey;
    private String coverKey;
    private Integer totalSpark;
    private long followerCount;
    private long followingCount;
    private boolean isFollowing;
}
