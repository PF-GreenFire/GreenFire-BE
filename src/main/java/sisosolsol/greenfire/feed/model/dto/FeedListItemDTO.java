package sisosolsol.greenfire.feed.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sisosolsol.greenfire.common.enums.post.PostType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class FeedListItemDTO {
    private Integer postCode;
    private UUID userCode;
    private String nickname;
    private String profileImage;
    private Boolean isFollowing;
    private Boolean featured;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String postContent;
    private PostType postType;
    private String thumbnail;

    private Integer storeCode;
    private String storeName;
    private Integer challengeCode;
    private String challengeTitle;

    private Boolean liked;
    private Integer likeCount;
    private Integer commentCount;
}
