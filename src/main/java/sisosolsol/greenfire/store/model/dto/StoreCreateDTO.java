package sisosolsol.greenfire.store.model.dto;

import lombok.Getter;
import lombok.ToString;
import sisosolsol.greenfire.common.enums.store.StoreFoodType;
import sisosolsol.greenfire.common.enums.store.StoreStatus;
import sisosolsol.greenfire.image.model.dto.ImageUploadDTO;
import sisosolsol.greenfire.location.model.dto.LocationDTO;

import java.util.List;
import java.util.UUID;

@Getter
@ToString
public class StoreCreateDTO {

    // 장소 신청 등록용 DTO
    private Integer storeCode;
    private String storeName;
    private StoreStatus storeStatus;
    private String storePhone;
    private String storeLink;
    private String storeBusinessHours;
    private LocationDTO location;
    private Integer storeCategoryCode;
    private StoreFoodType storeFoodType;
    private String detailAddress;
    private String description;
    private String storeBreaktimeHours;
    private String thumbnailUrl;
    private UUID userCode;
    private List<ImageUploadDTO> images;

    public void setUserCode(UUID userCode) {
        this.userCode = userCode;
    }
}
