package sisosolsol.greenfire.report.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.report.dto.ReportCreateRequest;
import sisosolsol.greenfire.report.dto.ReportHandleRequest;
import sisosolsol.greenfire.report.dto.ReportPageResponse;
import sisosolsol.greenfire.report.enums.ReportStatus;
import sisosolsol.greenfire.report.service.ReportService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 접수
     * POST /api/v1/reports
     */
    @PostMapping("/reports")
    public ResponseEntity<Map<String, Object>> createReport(
            @Valid @RequestBody ReportCreateRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        Long reportId = reportService.createReport(
                request, currentUser.userId(), httpRequest.getRemoteAddr());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "reportId", reportId,
                "message", "신고가 접수되었습니다."
        ));
    }

    /**
     * 신고 목록 조회 (관리자)
     * GET /api/v1/admin/reports?page=1&size=20&status=PENDING
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reports")
    public ResponseEntity<ReportPageResponse> getReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) ReportStatus status
    ) {
        ReportPageResponse response = reportService.getReports(page, size, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 신고 처리 (관리자)
     * PATCH /api/v1/admin/reports/{id}/handle
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/reports/{id}/handle")
    public ResponseEntity<Map<String, Object>> handleReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportHandleRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        reportService.handleReport(id, request, currentUser.userId(), httpRequest.getRemoteAddr());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "신고가 처리되었습니다."
        ));
    }

    /**
     * 대기 중인 신고 건수 (관리자 대시보드용)
     * GET /api/v1/admin/reports/pending-count
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reports/pending-count")
    public ResponseEntity<Map<String, Object>> getPendingReportCount() {
        long count = reportService.countPendingReports();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
