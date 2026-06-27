package sisosolsol.greenfire.scrap.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScrapStoreDTO {
    private Integer scrapCode;
    private Integer storeCode;
    private String storeName;
    private String address;
    private String thumbnailUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
