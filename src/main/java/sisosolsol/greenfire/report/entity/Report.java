package sisosolsol.greenfire.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.report.enums.ReportCategory;
import sisosolsol.greenfire.report.enums.ReportStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "report", indexes = {
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_reporter", columnList = "reporter_id"),
        @Index(name = "idx_report_resource", columnList = "resource_type, resource_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "reporter_id", nullable = false, columnDefinition = "uuid")
    private UUID reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false, length = 100)
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ReportCategory category;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "handled_by", columnDefinition = "uuid")
    private UUID handledBy;

    @Column(name = "handled_at")
    private Instant handledAt;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // ===== 비즈니스 메서드 =====

    public void handle(ReportStatus status, UUID handledBy, String adminNote) {
        this.status = status;
        this.handledBy = handledBy;
        this.handledAt = Instant.now();
        this.adminNote = adminNote;
    }

    public boolean isPending() {
        return this.status == ReportStatus.PENDING;
    }
}
