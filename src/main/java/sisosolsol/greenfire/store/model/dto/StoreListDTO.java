package sisosolsol.greenfire.store.model.dto;

import lombok.Getter;
import sisosolsol.greenfire.common.enums.store.StoreFoodType;
import sisosolsol.greenfire.common.enums.store.StoreStatus;

import java.time.OffsetDateTime;

@Getter
public class StoreListDTO {

    // 초록불 메인 장소 목록 조회용 DTO
    private Integer storeCode;
    private String storeName;
    private String storeCategory;
    private String address;
    private double latitude;
    private double longitude;
    private String areaCode;
}
