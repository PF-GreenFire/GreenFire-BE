package sisosolsol.greenfire.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.notice.dto.request.NoticeCreateRequest;
import sisosolsol.greenfire.notice.dto.request.NoticeUpdateRequest;
import sisosolsol.greenfire.notice.dto.response.NoticeDetailResponse;
import sisosolsol.greenfire.notice.dto.response.NoticeListResponse;
import sisosolsol.greenfire.notice.dto.response.NoticePageResponse;
import sisosolsol.greenfire.notice.service.NoticeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "공지사항", description = "공지사항 조회/관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 목록 조회
     * GET /api/notices?page=1&limit=20&category=NOTICE&searchKeyword=검색어
     */
    @Operation(summary = "공지사항 목록 조회")
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
     * GET /api/notices/{noticeCode}?userCode=UUID
     */
    @Operation(summary = "공지사항 상세 조회")
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
     * POST /api/notices/{noticeCode}/view
     * - 로그인 사용자: userCode 기반 중복 방지
     * - 비로그인 사용자: IP 기반 중복 방지
     */
    @Operation(summary = "공지사항 조회수 증가")
    @PostMapping("/{noticeCode}/view")
    public ResponseEntity<Map<String, Object>> incrementViewCount(
            @PathVariable Integer noticeCode,
            @RequestBody(required = false) Map<String, String> request,
            HttpServletRequest httpRequest
    ) {
        UUID userCode = (request != null && request.get("userCode") != null)
                ? UUID.fromString(request.get("userCode"))
                : null;

        String ipAddress = getClientIpAddress(httpRequest);

        noticeService.incrementViewCount(noticeCode, userCode, ipAddress);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "조회수가 증가되었습니다."
        ));
    }

    /**
     * 클라이언트 IP 주소 추출 (프록시 환경 고려)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 콤마로 구분된 IP 목록일 수 있음
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 관련 공지사항 조회
     * GET /api/notices/{noticeCode}/related?limit=5
     */
    @Operation(summary = "관련 공지사항 조회")
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
     * GET /api/notices/latest-important
     */
    @Operation(summary = "최신 중요 공지사항 조회 (미리보기용)")
    @GetMapping("/latest-important")
    public ResponseEntity<NoticeDetailResponse> getLatestImportantNotice() {
        NoticeDetailResponse response = noticeService.getLatestImportantNotice();
        return ResponseEntity.ok(response);
    }

    /**
     * 공지사항 생성 (관리자)
     * POST /api/notices
     */
    @Operation(summary = "공지사항 생성 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createNotice(
            @Valid @RequestPart("notice") NoticeCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        Integer noticeCode = noticeService.createNotice(
                request, currentUser.userId(), files, httpRequest.getRemoteAddr());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "noticeCode", noticeCode,
                "message", "공지사항이 등록되었습니다."
        ));
    }

    /**
     * 공지사항 수정 (관리자)
     * PUT /api/notices/{noticeCode}
     */
    @Operation(summary = "공지사항 수정 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{noticeCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateNotice(
            @PathVariable Integer noticeCode,
            @Valid @RequestPart("notice") NoticeUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        noticeService.updateNotice(noticeCode, request, files,
                currentUser.userId(), httpRequest.getRemoteAddr());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "공지사항이 수정되었습니다."
        ));
    }

    /**
     * 공지사항 삭제 (관리자)
     * DELETE /api/notices/{noticeCode}
     */
    @Operation(summary = "공지사항 삭제 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{noticeCode}")
    public ResponseEntity<Map<String, Object>> deleteNotice(
            @PathVariable Integer noticeCode,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        noticeService.deleteNotice(noticeCode, currentUser.userId(), httpRequest.getRemoteAddr());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "공지사항이 삭제되었습니다."
        ));
    }
}