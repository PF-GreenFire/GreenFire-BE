package sisosolsol.greenfire.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.common.enums.store.StoreStatus;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.store.model.dto.StoreCreateDTO;
import sisosolsol.greenfire.store.model.dto.StoreDetailDTO;
import sisosolsol.greenfire.store.model.dto.StoreListDTO;
import sisosolsol.greenfire.store.model.dto.StoreUpdateStatusDTO;
import sisosolsol.greenfire.store.service.StoreService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "장소(가맹점)", description = "초록불 장소 신청/관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private  final StoreService storeService;

    // 관리자 초록불 장소 상태에 따른 목록 페이징 조회
    @Operation(summary = "장소 상태별 목록 페이징 조회 (관리자)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{storeStatus}/list")
    public ResponseEntity<Map<String, Object>> getStoreListByStoreStatus(
            @PathVariable String storeStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        String normalized = storeStatus.toUpperCase();
        try {
            StoreStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ExceptionCode.INVALID_STORE_STATUS);
        }
        Map<String, Object> storeList = storeService.getStoreListByStoreStatus(normalized, page, limit);
        return ResponseEntity.ok(storeList);
    }

    // 초록불 장소 신청 등록 TODO: service 단 예외 처리 , 예워니 handler 설정 적용 or enum 타입 관리용 유효성 검사 적용, 아.. 썸네일... 필요할 듯...ㅠㅠ 힝, 이미지 등록 적용 완료 했으나 인코딩 적용 예정
    @Operation(summary = "초록불 장소 신청 등록")
    @PostMapping("/apply")
    public ResponseEntity<String> createApplyStore(
            @RequestBody StoreCreateDTO storeCreateDTO,
            @AuthenticationPrincipal AuthUser user
    ){
        storeCreateDTO.setUserCode(user.userId()); // 클라이언트가 보낸 userCode는 무시하고 서버 인증 정보로 덮어쓰기
        int storeCode = storeService.registApplyStore(storeCreateDTO);
        return ResponseEntity.created(URI.create("/api/store/detail/" + storeCode)).build();
    }

    // 초록불 회원 본인이 신청한 장소 목록 페이징 조회
    @Operation(summary = "본인이 신청한 장소 목록 페이징 조회")
    @GetMapping("/apply/list")
    public ResponseEntity<Map<String, Object>> getApplyStoreListByUserCode(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int limit,
            @AuthenticationPrincipal AuthUser user
    ) {
        Map<String, Object> storeList = storeService.getStoreListByUserCode(page, limit, user.userId());
        return ResponseEntity.ok(storeList);
    }

    // 장소 상세 정보 조회
    @Operation(summary = "장소 상세 정보 조회")
    @GetMapping("/detail/{storeCode}")
    public ResponseEntity<StoreDetailDTO> getStoreDetail (@PathVariable("storeCode") Integer storeCode, @AuthenticationPrincipal AuthUser user) {
        UUID userId = user != null ? user.userId() : null;
        StoreDetailDTO storeDetail = storeService.getStoreDetailByStoreCode(userId, storeCode);
        return ResponseEntity.ok(storeDetail);
    }

    // 관리자 장소 정보 수정
    @Operation(summary = "장소 정보 수정 (관리자)")
    @PutMapping("/update/{storeCode}")
    public ResponseEntity<StoreCreateDTO> updateStore (@PathVariable int storeCode, @RequestBody StoreCreateDTO updateDTO) {
        storeService.updateStore(storeCode, updateDTO);
        return ResponseEntity.ok(updateDTO);
    }

    // 관리자 장소 상태 변경
    // status 필드는 StoreUpdateStatusDTO에서 StoreStatus enum으로 받기 때문에
    // 잘못된 값은 Jackson이 400으로 자동 거절함.
    @Operation(summary = "장소 상태 변경 (관리자)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/change/{storeCode}")
    public ResponseEntity<StoreUpdateStatusDTO> updateStoreStatus(
            @PathVariable int storeCode,
            @RequestBody StoreUpdateStatusDTO storeUpdateStatusDTO
    ) {
        storeService.updateStoreStatus(storeCode, storeUpdateStatusDTO);
        return ResponseEntity.ok(storeUpdateStatusDTO);
    }

}
