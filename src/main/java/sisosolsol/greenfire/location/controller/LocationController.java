package sisosolsol.greenfire.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sisosolsol.greenfire.common.config.UploadAllowConfig;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.store.model.dto.StoreDetailDTO;
import sisosolsol.greenfire.store.model.dto.StoreListDTO;
import sisosolsol.greenfire.store.service.StoreService;

@Tag(name = "초록불 장소", description = "초록불 장소(지도) 조회, 이미지, 찜 API")
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final StoreService storeService;
    private final UploadAllowConfig uploadAllowConfig;

    @Operation(summary = "장소 카테고리 목록 조회")
    @GetMapping("/categories")
    public ResponseEntity getStoreCategories() {

        return ResponseEntity.ok(storeService.getStoreCategories());
    }

    // 초록불 메인 장소 목록 조회 (좌표 없이 전체)
    @Operation(summary = "초록불 메인 장소 목록 조회")
    @GetMapping
    public ResponseEntity<List<StoreListDTO>> getStoreList(@AuthenticationPrincipal AuthUser user) {
        UUID userId = user != null ? user.userId() : null;
        List<StoreListDTO> stores = storeService.getStoreList(userId);
        return ResponseEntity.ok(stores);
    }

    // 현재 위치 기반 반경 내 장소 조회. Haversine 거리로 필터링 후 가까운 순.
    @Operation(summary = "내 주변 장소 조회 (위도/경도/반경km)")
    @GetMapping("/nearby")
    public ResponseEntity<List<StoreListDTO>> getNearbyStoreList(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "3.0") Double radiusKm,
            @AuthenticationPrincipal AuthUser user) {
        UUID userId = user != null ? user.userId() : null;
        return ResponseEntity.ok(
                storeService.getNearbyStoreList(userId, latitude, longitude, radiusKm)
        );
    }

    @Operation(summary = "장소 이미지 조회")
    @GetMapping("/store-image/{imageCode}")
    public ResponseEntity<Resource> getStoreImage(@PathVariable("imageCode") int imageCode) {
        String storedKey = storeService.findImagePathByImageCode(imageCode);

        Path filePath = Paths.get(uploadAllowConfig.getDirectory(), storedKey);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IllegalArgumentException("등록된 이미지가 없습니다.");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
    }

    // 장소 상세 정보 조회
    @Operation(summary = "장소 상세 정보 조회")
    @GetMapping("/stores/{storeCode}")
    public ResponseEntity<StoreDetailDTO> getStoreDetail (@PathVariable("storeCode") Integer storeCode, @AuthenticationPrincipal AuthUser user) {
        UUID userId = user != null ? user.userId() : null;
        StoreDetailDTO storeDetail = storeService.getStoreDetailByStoreCode(userId, storeCode);
        return ResponseEntity.ok(storeDetail);
    }

    @Operation(summary = "장소 찜 등록")
    @PostMapping("/stores/{storeCode}/like")
    public ResponseEntity<Void> storeLike(@PathVariable("storeCode") int storeCode, @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        storeService.storeLike(user.userId(), storeCode);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "장소 찜 취소")
    @DeleteMapping("/stores/{storeCode}/like")
    public ResponseEntity<Void> deleteStoreLike(@PathVariable("storeCode") int storeCode, @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        storeService.deleteStoreLike(user.userId(), storeCode);
        return ResponseEntity.ok().build();
    }
}
