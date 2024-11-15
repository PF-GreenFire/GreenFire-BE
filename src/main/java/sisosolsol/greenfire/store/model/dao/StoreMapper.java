package sisosolsol.greenfire.store.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.common.page.SelectCriteria;
import sisosolsol.greenfire.store.model.dto.StoreCreateDTO;
import sisosolsol.greenfire.store.model.dto.StoreApplyListDTO;
import sisosolsol.greenfire.store.model.dto.StoreDetailDTO;
import sisosolsol.greenfire.store.model.dto.StoreListDTO;

import java.util.List;
import java.util.UUID;

@Mapper
public interface StoreMapper {
    // 초록불 장소 목록 조회 TODO: 현재 위치 정보를 기반으로 반경 지도 목록을 보여주는 것으로 수정 예정
    List<StoreListDTO> findStoreList();

    // 관리자 초록불 장소 상태에 따른 목록 페이징 조회를 위한 토탈 갯수 조회
    int countStoresByStoreStatus(String storeStatus);

    // 관리자 초록불 장소 상태에 따른 목록 페이징 조회 [신청 대기, 신청 승인]
    List<StoreApplyListDTO> findStoreListByStoreStatus(@Param("criteria") SelectCriteria criteria, @Param("storeStatus") String storeStatus);

    // 초록불 장소 신청 등록
    void registApplyStore(@Param("storeCreateDTO") StoreCreateDTO storeCreateDTO, @Param("locationCode") int locationCode);

    // 초록불 회원 본인이 신청한 장소 목록 페이징 조회를 위한 토탈 갯수 조회
    int countApplyStoresByUserCode(UUID userCode);

    // 초록불 회원 본인이 신청한 장소 목록 페이징 조회
    List<StoreApplyListDTO> findApplyStoreListByUserCode(@Param("criteria") SelectCriteria selectCriteria, @Param("userCode")UUID userCode);

    // 장소 상세 정보 조회
    StoreDetailDTO findStoreDetailByStoreCode(Integer storeCode);
}
