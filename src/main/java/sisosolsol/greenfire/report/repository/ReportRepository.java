package sisosolsol.greenfire.report.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.report.entity.Report;
import sisosolsol.greenfire.report.enums.ReportStatus;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 상태별 신고 목록 조회 (페이징, 최신순)
     */
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    /**
     * 전체 신고 목록 조회 (페이징, 최신순)
     */
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 중복 신고 체크 (동일 사용자가 동일 콘텐츠를 PENDING 상태로 신고한 적 있는지)
     */
    boolean existsByReporterIdAndResourceTypeAndResourceIdAndStatus(
            UUID reporterId,
            ResourceType resourceType,
            String resourceId,
            ReportStatus status
    );

    /**
     * 특정 상태의 신고 건수 (대시보드용)
     */
    long countByStatus(ReportStatus status);

    /**
     * 특정 상태가 아닌 신고 건수 (처리된 신고 건수)
     */
    long countByStatusNot(ReportStatus status);
}
