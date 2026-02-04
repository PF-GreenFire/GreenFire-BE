package sisosolsol.greenfire.notice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.notice.dto.request.NoticeCreateRequest;
import sisosolsol.greenfire.notice.dto.request.NoticeUpdateRequest;
import sisosolsol.greenfire.notice.dto.response.NoticeDetailResponse;
import sisosolsol.greenfire.notice.dto.response.NoticeListResponse;
import sisosolsol.greenfire.notice.dto.response.NoticePageResponse;
import sisosolsol.greenfire.notice.service.NoticeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 목록 조회
     * GET /api/v1/notices?page=1&limit=20&category=NOTICE&searchKeyword=검색어
     */
    @GetMapping
    public ResponseEntity<NoticePageResponse> getNoticeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) UUID userCode
    ) {
        NoticePageResponse response = noticeService.getNoticeList(
                page, limit, category, searchKeyword, userCode);
        return ResponseEntity.ok(response);
    }

    /**
     * 공지사항 상세 조회
     * GET /api/v1/notices/{noticeCode}?userCode=UUID
     */
    @GetMapping("/{noticeCode}")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(
            @PathVariable Integer noticeCode,
            @RequestParam(required = false) UUID userCode
    ) {
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeCode, userCode);
        return ResponseEntity.ok(response);
    }

    /**
     * 조회수 증가
     * POST /api/v1/notices/{noticeCode}/view
     */
    @PostMapping("/{noticeCode}/view")
    public ResponseEntity<Map<String, Object>> incrementViewCount(
            @PathVariable Integer noticeCode,
            @RequestBody Map<String, String> request
    ) {
        UUID userCode = request.get("userCode") != null
                ? UUID.fromString(request.get("userCode"))
                : null;

        noticeService.incrementViewCount(noticeCode, userCode);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "조회수가 증가되었습니다."
        ));
    }

    /**
     * 관련 공지사항 조회
     * GET /api/v1/notices/{noticeCode}/related?limit=5
     */
    @GetMapping("/{noticeCode}/related")
    public ResponseEntity<List<NoticeListResponse>> getRelatedNotices(
            @PathVariable Integer noticeCode,
            @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<NoticeListResponse> response = noticeService.getRelatedNotices(noticeCode, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 최신 중요 공지사항 조회 (미리보기용)
     * GET /api/v1/notices/latest-important
     */
    @GetMapping("/latest-important")
    public ResponseEntity<NoticeDetailResponse> getLatestImportantNotice() {
        NoticeDetailResponse response = noticeService.getLatestImportantNotice();
        return ResponseEntity.ok(response);
    }

    /**
     * 공지사항 생성 (관리자)
     * POST /api/v1/notices
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createNotice(
            @Valid @RequestPart("notice") NoticeCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Integer noticeCode = noticeService.createNotice(request, user.getId(), files);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "noticeCode", noticeCode,
                "message", "공지사항이 등록되었습니다."
        ));
    }

    /**
     * 공지사항 수정 (관리자)
     * PUT /api/v1/notices/{noticeCode}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{noticeCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateNotice(
            @PathVariable Integer noticeCode,
            @Valid @RequestPart("notice") NoticeUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        noticeService.updateNotice(noticeCode, request, files);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "공지사항이 수정되었습니다."
        ));
    }

    /**
     * 공지사항 삭제 (관리자)
     * DELETE /api/v1/notices/{noticeCode}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{noticeCode}")
    public ResponseEntity<Map<String, Object>> deleteNotice(
            @PathVariable Integer noticeCode
    ) {
        noticeService.deleteNotice(noticeCode);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "공지사항이 삭제되었습니다."
        ));
    }
}