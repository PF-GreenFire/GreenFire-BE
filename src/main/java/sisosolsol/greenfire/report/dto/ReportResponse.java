package sisosolsol.greenfire.report.dto;

import lombok.Builder;
import lombok.Getter;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.report.entity.Report;
import sisosolsol.greenfire.report.enums.ReportCategory;
import sisosolsol.greenfire.report.enums.ReportStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ReportResponse {

    private Long id;
    private UUID reporterId;
    private String reporterEmail;       // 신고자 이메일 (조회 시 조인)
    private ResourceType resourceType;
    private String resourceId;
    private ReportCategory category;
    private String categoryDescription;
    private String reason;
    private ReportStatus status;
    private String statusDescription;
    private UUID handledBy;
    private String handledByEmail;      // 처리자 이메일 (조회 시 조인)
    private Instant handledAt;
    private String adminNote;
    private Instant createdAt;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .resourceType(report.getResourceType())
                .resourceId(report.getResourceId())
                .category(report.getCategory())
                .categoryDescription(report.getCategory().getDescription())
                .reason(report.getReason())
                .status(report.getStatus())
                .statusDescription(report.getStatus().getDescription())
                .handledBy(report.getHandledBy())
                .handledAt(report.getHandledAt())
                .adminNote(report.getAdminNote())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public static ReportResponse from(Report report, String reporterEmail, String handledByEmail) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reporterEmail(reporterEmail)
                .resourceType(report.getResourceType())
                .resourceId(report.getResourceId())
                .category(report.getCategory())
                .categoryDescription(report.getCategory().getDescription())
                .reason(report.getReason())
                .status(report.getStatus())
                .statusDescription(report.getStatus().getDescription())
                .handledBy(report.getHandledBy())
                .handledByEmail(handledByEmail)
                .handledAt(report.getHandledAt())
                .adminNote(report.getAdminNote())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
