package sisosolsol.greenfire.banner.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class BannerDTO {
    private Integer bannerCode;
    private String bannerTitle;
    private String linkUrl;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
