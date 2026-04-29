package sisosolsol.greenfire.location.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sisosolsol.greenfire.common.config.UploadAllowConfig;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.store.model.dto.StoreDetailDTO;
import sisosolsol.greenfire.store.model.dto.StoreListDTO;
import sisosolsol.greenfire.store.service.StoreService;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final StoreService storeService;
    private final UploadAllowConfig uploadAllowConfig;

    @GetMapping("/categories")
    public ResponseEntity getStoreCategories() {

        return ResponseEntity.ok(storeService.getStoreCategories());
    }

    // 초록불 메인 장소 목록 조회 TODO: 현재 위치 정보를 기반으로 반경 지도 목록을 보여주는 것으로 수정 예정, 썸네일이 필요할것 같은 예감인데 order값 1인 것으로 할지 썸네일 만들지 추후 협의 및 적용 예정
    @GetMapping
    public ResponseEntity<List<StoreListDTO>> getStoreList(@AuthenticationPrincipal AuthUser user) {
        UUID userId = user != null ? user.userId() : null;
        List<StoreListDTO> stores = storeService.getStoreList(userId);
        return ResponseEntity.ok(stores);
    }

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
    @GetMapping("/stores/{storeCode}")
    public ResponseEntity<StoreDetailDTO> getStoreDetail (@PathVariable("storeCode") Integer storeCode, @AuthenticationPrincipal AuthUser user) {
        UUID userId = user != null ? user.userId() : null;
        StoreDetailDTO storeDetail = storeService.getStoreDetailByStoreCode(userId, storeCode);
        return ResponseEntity.ok(storeDetail);
    }

    @PostMapping("/stores/{storeCode}/like")
    public ResponseEntity<Void> storeLike(@PathVariable("storeCode") int storeCode, @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        storeService.storeLike(user.userId(), storeCode);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/stores/{storeCode}/like")
    public ResponseEntity<Void> deleteStoreLike(@PathVariable("storeCode") int storeCode, @AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        storeService.deleteStoreLike(user.userId(), storeCode);
        return ResponseEntity.ok().build();
    }
}
