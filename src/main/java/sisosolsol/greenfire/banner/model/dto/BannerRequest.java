package sisosolsol.greenfire.banner.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BannerRequest {
    private String bannerTitle;
    private String linkUrl;
    private Integer displayOrder;
    private Boolean isActive;
}
