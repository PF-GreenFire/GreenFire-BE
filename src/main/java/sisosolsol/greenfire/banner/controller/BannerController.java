package sisosolsol.greenfire.banner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.banner.model.dto.BannerDTO;
import sisosolsol.greenfire.banner.model.dto.BannerRequest;
import sisosolsol.greenfire.banner.service.BannerService;

import java.net.URI;
import java.util.List;

@Tag(name = "배너", description = "메인 페이지 배너 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;

    // 활성 배너 (메인 페이지 — permitAll)
    @Operation(summary = "활성 배너 목록 조회 (메인 페이지용)")
    @GetMapping
    public ResponseEntity<List<BannerDTO>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

    // 어드민 전체 배너
    @Operation(summary = "전체 배너 목록 조회 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<BannerDTO>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    // 어드민 등록
    @Operation(summary = "배너 등록 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createBanner(
            @RequestPart("banner") BannerRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        Integer bannerCode = bannerService.createBanner(request, file);
        return ResponseEntity.created(URI.create("/api/banners/" + bannerCode)).build();
    }

    // 어드민 수정
    @Operation(summary = "배너 수정 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{bannerCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateBanner(
            @PathVariable Integer bannerCode,
            @RequestPart("banner") BannerRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        bannerService.updateBanner(bannerCode, request, file);
        return ResponseEntity.ok().build();
    }

    // 어드민 삭제
    @Operation(summary = "배너 삭제 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{bannerCode}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Integer bannerCode) {
        bannerService.deleteBanner(bannerCode);
        return ResponseEntity.noContent().build();
    }
}
