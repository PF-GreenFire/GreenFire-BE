package sisosolsol.greenfire.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.common.page.Pagination;
import sisosolsol.greenfire.common.page.SelectCriteria;
import sisosolsol.greenfire.image.model.dto.ImageUploadDTO;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.location.model.dao.LocationMapper;
import sisosolsol.greenfire.location.model.dto.LocationDTO;
import sisosolsol.greenfire.location.service.LocationService;
import sisosolsol.greenfire.store.model.dao.StoreMapper;
import sisosolsol.greenfire.store.model.dto.StoreCreateDTO;
import sisosolsol.greenfire.store.model.dto.StoreApplyListDTO;
import sisosolsol.greenfire.store.model.dto.StoreDetailDTO;
import sisosolsol.greenfire.store.model.dto.StoreListDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;
    private final LocationMapper locationMapper;
    private final LocationService locationService;
    private final ImageService imageService;

    // 초록불 메인 장소 목록 조회 TODO: 현재 위치 정보를 기반으로 반경 지도 목록을 보여주는 것으로 수정 예정
    public List<StoreListDTO> getStoreList() {
        return storeMapper.findStoreList();
    }

    // 관리자 초록불 장소 상태에 따른 목록 페이징 조회 [신청 대기, 신청 승인]
    public Map<String, Object> getStoreListByStoreStatus(String storeStatus, int page, int limit) {
        int totalCount = storeMapper.countStoresByStoreStatus(storeStatus); // `WAITING`과 'APPROVE' 등 스토어 상태에 따른 총 개수를 조회

        int buttonAmount = 5; // 페이지 하단에 보일 버튼 수
        SelectCriteria selectCriteria = Pagination.getSelectCriteria(page, totalCount, limit, buttonAmount);

        List<StoreApplyListDTO> storeList = storeMapper.findStoreListByStoreStatus(selectCriteria, storeStatus);

        Map<String, Object> storeListResponse = new HashMap<>();
        storeListResponse.put("paging", selectCriteria);
        storeListResponse.put("storeList", storeList);

        return storeListResponse;
    }

    // 초록불 장소 신청 등록
    @Transactional
    public int registApplyStore(StoreCreateDTO storeCreateDTO) {
        int locationCode = processLocation(storeCreateDTO.getLocation()); // 지역 코드 중복 조회 후 없다면 등록

        storeMapper.registApplyStore(storeCreateDTO, locationCode); // 장소 신청 등록

        if (storeCreateDTO.getImages() != null) { // 이미지 파일 있을때 만 이미지 등록 TODO: fileName incoding 적용 예정 [현재 fileName 제외 등록]
            processImages(storeCreateDTO.getStoreCode(), storeCreateDTO.getImages()); // 이미지 파일 삭제 후 등록
        }

        return storeCreateDTO.getStoreCode();
    }

    // 초록불 회원 본인이 신청한 장소 목록 페이징 조회
    public Map<String, Object> getStoreListByUserCode(int page, int limit, UUID userCode) {
        int totalCount = storeMapper.countApplyStoresByUserCode(userCode); // 로그인 한 UserCode 에 따른 apply Store 총 개수 조회

        int buttonAmount = 5; // 페이지 하단에 보일 버튼 수
        SelectCriteria selectCriteria = Pagination.getSelectCriteria(page, totalCount, limit, buttonAmount);

        List<StoreApplyListDTO> storeList = storeMapper.findApplyStoreListByUserCode(selectCriteria, userCode);

        Map<String, Object> storeListResponse = new HashMap<>();
        storeListResponse.put("paging", selectCriteria);
        storeListResponse.put("storeList", storeList);

        return storeListResponse;
    }

    // 장소 상세 정보 조회
    public StoreDetailDTO getStoreDetailByStoreCode(Integer storeCode) {
        StoreDetailDTO storeDetail = storeMapper.findStoreDetailByStoreCode(storeCode);
        return storeDetail;
    }

    // 관리자 장소 정보 수정
    @Transactional
    public void updateStore(int storeCode, StoreCreateDTO updateDTO) {
        int locationCode = processLocation(updateDTO.getLocation()); // 지역 코드 중복 조회 후 없다면 등록

        storeMapper.updateStore(storeCode, updateDTO, locationCode); // 장소 정보 수정

        if (updateDTO.getImages() != null) { // 이미지 파일 있을때 만 이미지 등록 TODO: fileName incoding 적용 예정 [현재 fileName 제외 등록]
            processImages(storeCode, updateDTO.getImages()); // 이미지 파일 삭제 후 등록
        }
    }



    // locationCode 중복 확인 및 등록수 locationCode 반환 메서드
    private int processLocation(LocationDTO location) {
        int locationCode = locationMapper.findLocationByCoordinates(location.getLatitude(), location.getLongitude());

        if (locationCode == 0) {
            locationService.registLocation(location);
            locationCode = locationMapper.findLocationByCoordinates(location.getLatitude(), location.getLongitude());
        }

        return locationCode;
    }

    // image 파일 삭제 및 등록 매서드
    private void processImages(int storeCode, List<ImageUploadDTO> images) {
        imageService.deleteAllInStore(storeCode);

        for (ImageUploadDTO image : images) {
            imageService.saveImage(ImageType.STORE, storeCode, image);
        }
    }

}
