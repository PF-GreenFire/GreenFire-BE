package sisosolsol.greenfire.feed.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class CommentDTO {

    private Integer commentCode;
    private Integer postCode;
    private UUID userCode;
    private String nickname;
    private String profileImage;
    private String commentContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
