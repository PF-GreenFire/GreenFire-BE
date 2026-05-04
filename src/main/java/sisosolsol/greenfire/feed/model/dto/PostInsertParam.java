package sisosolsol.greenfire.feed.model.dto;

import lombok.Getter;
import lombok.Setter;
import sisosolsol.greenfire.common.enums.post.PostType;

import java.util.UUID;

// MyBatis insert에서 useGeneratedKeys로 postCode를 받기 위한 파라미터 객체
@Getter
@Setter
public class PostInsertParam {
    private UUID userCode;
    private String postContent;
    private PostType postType;
    private Integer storeCode;
    private Integer challengeCode;
    private Integer postCode; // OUT
}
