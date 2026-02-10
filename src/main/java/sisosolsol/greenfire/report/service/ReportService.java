package sisosolsol.greenfire.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.audit.entity.ActionType;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.common.audit.service.ActivityLogService;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.report.dto.ReportCreateRequest;
import sisosolsol.greenfire.report.dto.ReportHandleRequest;
import sisosolsol.greenfire.report.dto.ReportPageResponse;
import sisosolsol.greenfire.report.dto.ReportResponse;
import sisosolsol.greenfire.report.entity.Report;
import sisosolsol.greenfire.report.enums.ReportStatus;
import sisosolsol.greenfire.report.repository.ReportRepository;
import sisosolsol.greenfire.user.entity.UserAccount;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserAccountRepository userAccountRepository;
    private final ActivityLogService activityLogService;

    /**
     * 신고 접수
     */
    @Transactional
    public Long createReport(ReportCreateRequest request, UUID reporterId, String ipAddress) {
        // 유효한 리소스 타입인지 확인 (POST, COMMENT, CHALLENGE만 허용)
        if (!isValidResourceType(request.getResourceType())) {
            throw new CustomException(ExceptionCode.INVALID_RESOURCE_TYPE);
        }

        // 중복 신고 체크 (동일 사용자가 동일 콘텐츠를 PENDING 상태로 신고한 적 있는지)
        boolean isDuplicate = reportRepository.existsByReporterIdAndResourceTypeAndResourceIdAndStatus(
                reporterId,
                request.getResourceType(),
                request.getResourceId(),
                ReportStatus.PENDING
        );

        if (isDuplicate) {
            throw new CustomException(ExceptionCode.DUPLICATE_REPORT);
        }

        Report report = Report.builder()
                .reporterId(reporterId)
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .category(request.getCategory())
                .reason(request.getReason())
                .build();

        Report savedReport = reportRepository.save(report);

        // 활동 로그 기록
        activityLogService.log(
                reporterId,
                ActionType.CREATE,
                ResourceType.REPORT,
                savedReport.getId().toString(),
                request.getResourceType() + ":" + request.getResourceId() + " - " + request.getCategory(),
                ipAddress
        );

        return savedReport.getId();
    }

    /**
     * 신고 목록 조회 (관리자)
     */
    public ReportPageResponse getReports(Integer page, Integer size, ReportStatus status) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Report> reportPage;
        if (status != null) {
            reportPage = reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            reportPage = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        Page<ReportResponse> responsePage = reportPage.map(report -> {
            String reporterEmail = userAccountRepository.findById(report.getReporterId())
                    .map(UserAccount::getEmail)
                    .orElse(null);

            String handledByEmail = null;
            if (report.getHandledBy() != null) {
                handledByEmail = userAccountRepository.findById(report.getHandledBy())
                        .map(UserAccount::getEmail)
                        .orElse(null);
            }

            return ReportResponse.from(report, reporterEmail, handledByEmail);
        });

        return ReportPageResponse.from(responsePage);
    }

    /**
     * 신고 처리 (관리자)
     */
    @Transactional
    public void handleReport(Long reportId, ReportHandleRequest request, UUID adminId, String ipAddress) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ExceptionCode.REPORT_NOT_FOUND));

        // PENDING 상태가 아니면 처리 불가
        if (!report.isPending()) {
            throw new CustomException(ExceptionCode.REPORT_ALREADY_HANDLED);
        }

        // PENDING 상태로는 처리할 수 없음
        if (request.getStatus() == ReportStatus.PENDING) {
            throw new CustomException(ExceptionCode.INVALID_REPORT_STATUS);
        }

        report.handle(request.getStatus(), adminId, request.getAdminNote());

        // 활동 로그 기록
        activityLogService.log(
                adminId,
                ActionType.UPDATE,
                ResourceType.REPORT,
                reportId.toString(),
                request.getStatus().getDescription(),
                ipAddress
        );
    }

    /**
     * 대기 중인 신고 건수 (대시보드용)
     */
    public long countPendingReports() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    /**
     * 신고 가능한 리소스 타입인지 확인
     */
    private boolean isValidResourceType(ResourceType resourceType) {
        return resourceType == ResourceType.POST
                || resourceType == ResourceType.COMMENT
                || resourceType == ResourceType.CHALLENGE;
    }
}
