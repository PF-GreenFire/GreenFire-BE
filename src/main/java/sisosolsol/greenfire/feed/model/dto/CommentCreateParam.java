package sisosolsol.greenfire.feed.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

// MyBatis insert에서 useGeneratedKeys로 commentCode를 받기 위한 파라미터 객체
@Getter
@Setter
public class CommentCreateParam {

    private Integer postCode;
    private UUID userCode;
    private String content;
    private Integer commentCode; // OUT (insert 후 채워짐)

    public CommentCreateParam(Integer postCode, UUID userCode, String content) {
        this.postCode = postCode;
        this.userCode = userCode;
        this.content = content;
    }
}
