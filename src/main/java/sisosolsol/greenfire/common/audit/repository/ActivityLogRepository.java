package sisosolsol.greenfire.common.audit.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sisosolsol.greenfire.common.audit.entity.ActionType;
import sisosolsol.greenfire.common.audit.entity.ActivityLog;
import sisosolsol.greenfire.common.audit.entity.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /** 특정 유저의 전체 활동 이력 (신고 조사 시) */
    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** 특정 유저의 특정 리소스 타입 활동 (예: 유저의 모든 댓글 이력) */
    List<ActivityLog> findByUserIdAndResourceTypeOrderByCreatedAtDesc(UUID userId, ResourceType resourceType);

    /** 특정 리소스에 대한 행위 횟수 (예: 42번 게시글의 VIEW 수) */
    long countByResourceTypeAndResourceIdAndActionType(ResourceType resourceType, String resourceId, ActionType actionType);

    /** 기간별 인기 리소스 (예: 최근 7일간 VIEW가 많은 게시글 TOP N) */
    @Query("SELECT a.resourceId, COUNT(a) AS cnt FROM ActivityLog a " +
            "WHERE a.resourceType = :type AND a.actionType = :action AND a.createdAt >= :since " +
            "GROUP BY a.resourceId ORDER BY cnt DESC")
    List<Object[]> findTrendingResources(@Param("type") ResourceType type,
                                         @Param("action") ActionType action,
                                         @Param("since") Instant since,
                                         Pageable pageable);

    /** 특정 유저의 기간별 활동 횟수 (활동량 집계) */
    long countByUserIdAndCreatedAtAfter(UUID userId, Instant since);
}
