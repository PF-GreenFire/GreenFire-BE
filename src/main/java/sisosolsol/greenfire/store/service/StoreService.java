package sisosolsol.greenfire.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.common.page.Pagination;
import sisosolsol.greenfire.common.page.SelectCriteria;
import sisosolsol.greenfire.image.model.dto.ImageUploadDTO;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.location.model.dao.LocationMapper;
import sisosolsol.greenfire.location.model.dto.LocationDTO;
import sisosolsol.greenfire.location.service.LocationService;
import sisosolsol.greenfire.spark.service.SparkService;
import sisosolsol.greenfire.store.model.dao.StoreMapper;
import sisosolsol.greenfire.store.model.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;
    private final LocationMapper locationMapper;
    private final LocationService locationService;
    private final ImageService imageService;
    private final SparkService sparkService;
    private final sisosolsol.greenfire.notification.service.NotificationService notificationService;

    // 초록불 메인 장소 목록 조회 (좌표 없을 때 전체 list)
    public List<StoreListDTO> getStoreList(UUID userCode) {
        return storeMapper.findStoreList(userCode);
    }

    // 현재 위치 기반 반경(km) 내 장소 조회 (가까운 순)
    public List<StoreListDTO> getNearbyStoreList(UUID userCode,
                                                 double latitude,
                                                 double longitude,
                                                 double radiusKm) {
        return storeMapper.findStoreListWithinRadius(userCode, latitude, longitude, radiusKm);
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

        try {
            storeMapper.registApplyStore(storeCreateDTO, locationCode); // 장소 신청 등록
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.InvalidForeignKeyException);
        } catch (DataAccessException e) {
            log.error("장소 신청 등록 실패 (userCode={}, locationCode={})",
                    storeCreateDTO.getUserCode(), locationCode, e);
            throw new CustomException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }

        if (storeCreateDTO.getImages() != null) { // TODO: fileName 인코딩 정책 결정 후 적용
            processImages(storeCreateDTO.getStoreCode(), storeCreateDTO.getImages());
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
    public StoreDetailDTO getStoreDetailByStoreCode(UUID userCode, Integer storeCode) {
        StoreDetailDTO storeDetail = storeMapper.findStoreDetailByStoreCode(userCode, storeCode);
        return storeDetail;
    }

    // 관리자 장소 정보 수정
    @Transactional
    public void updateStore(int storeCode, StoreCreateDTO updateDTO) {
        int locationCode = processLocation(updateDTO.getLocation()); // 지역 코드 중복 조회 후 없다면 등록

        try {
            storeMapper.updateStore(storeCode, updateDTO, locationCode);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.InvalidForeignKeyException);
        } catch (DataAccessException e) {
            log.error("장소 정보 수정 실패 (storeCode={}, locationCode={})",
                    storeCode, locationCode, e);
            throw new CustomException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }

        if (updateDTO.getImages() != null) { // TODO: fileName 인코딩 정책 결정 후 적용
            processImages(storeCode, updateDTO.getImages());
        }
    }

    // 관리자 장소 상태 변경
    public void updateStoreStatus(int storeCode, StoreUpdateStatusDTO storeUpdateStatusDTO) {
        storeMapper.updateStoreStatus(storeCode, storeUpdateStatusDTO);

        UUID applicant = storeMapper.findApplicantUserCode(storeCode);
        if (applicant == null) return;

        var status = storeUpdateStatusDTO.getStatus();
        if (status == sisosolsol.greenfire.common.enums.store.StoreStatus.APPROVE) {
            sparkService.award(applicant, 50, "STORE_APPROVED", "STORE", storeCode);
            notificationService.notify(applicant,
                    sisosolsol.greenfire.notification.model.NotificationType.STORE_APPROVED,
                    null, "STORE", String.valueOf(storeCode));
        } else if (status == sisosolsol.greenfire.common.enums.store.StoreStatus.REJECT) {
            notificationService.notify(applicant,
                    sisosolsol.greenfire.notification.model.NotificationType.STORE_REJECTED,
                    null, "STORE", String.valueOf(storeCode));
        }
    }

    // locationCode 중복 확인 및 등록수 locationCode 반환 메서드
    private int processLocation(LocationDTO location) {
        int locationCode = locationMapper.findLocationByCoordinates(location.getLatitude(), location.getLongitude());

        if (locationCode == 0) {
            locationService.registerLocation(location);
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

    public List<StoreCategory> getStoreCategories() {
        return storeMapper.getStoreCategories();
    }

    public String findImagePathByImageCode(int imageCode) {
        return storeMapper.findImagePathByImageCode(imageCode);
    }

    public void storeLike(UUID userCode, int storeCode) {
        storeMapper.storeLike(userCode, storeCode);
    }

    public void deleteStoreLike(UUID userCode, int storeCode) {
        storeMapper.deleteStoreLike(userCode, storeCode);
    }
}
