package sisosolsol.greenfire.feed.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sisosolsol.greenfire.common.enums.post.PostType;

@Getter
@Setter
@ToString
public class FeedCreateRequest {
    private String postContent;
    private PostType postType;
    private Integer storeCode;
    private Integer challengeCode;
}
