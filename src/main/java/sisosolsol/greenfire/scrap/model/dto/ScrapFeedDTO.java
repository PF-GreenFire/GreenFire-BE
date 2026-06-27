package sisosolsol.greenfire.scrap.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScrapFeedDTO {
    private Integer scrapCode;
    private Integer postCode;
    private String postContent;
    private String thumbnail;
    private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
