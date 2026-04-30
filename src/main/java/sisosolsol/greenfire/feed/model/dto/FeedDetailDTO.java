package sisosolsol.greenfire.feed.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sisosolsol.greenfire.common.enums.post.PostType;
import sisosolsol.greenfire.image.model.dto.ImageDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
public class FeedDetailDTO {

    private Integer postCode;
    private UUID userCode;
    private String nickname;
    private String profileImage; // 추후 user 도메인과 연동
    private Boolean isFollowing; // 추후 follow 도메인과 연동 (현재는 false)
    private Boolean featured;    // 관리자 추천 토글 도입 후 연동

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String postContent;
    private PostType postType;

    private Integer storeCode;
    private String storeName;
    private Integer challengeCode;
    private String challengeTitle;

    private Boolean liked;
    private Integer likeCount;
    private Integer commentCount;

    private List<ImageDTO> images;
}
