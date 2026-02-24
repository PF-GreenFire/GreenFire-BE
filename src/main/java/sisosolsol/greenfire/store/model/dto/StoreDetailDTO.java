package sisosolsol.greenfire.store.model.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sisosolsol.greenfire.common.enums.store.StoreFoodType;
import sisosolsol.greenfire.common.enums.store.StoreStatus;
import sisosolsol.greenfire.image.model.dto.ImageDTO;

import java.util.List;

@Getter
@ToString
public class StoreDetailDTO {

    // 장소 상세 정보 조회용 DTO
    private Integer storeCode;
    private String storeName;
    private StoreStatus storeStatus;
    private String storePhone;
    private String storeLink;
    private String storeBusinessHours;
    private String address;
    private String storeCategoryName;
    private StoreFoodType storeFoodType;
    private String detailAddress;
    private String description;
    private String storeBreaktimeHours;
    private List<ImageDTO> images;

}
