package sisosolsol.greenfire.admin.dto;

import sisosolsol.greenfire.common.audit.entity.ActivityLog;

import java.time.Instant;

public record ActivityLogResponse(
        Long id,
        String actionType,
        String resourceType,
        String resourceId,
        String content,
        String ipAddress,
        Instant createdAt
) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getActionType() != null ? log.getActionType().name() : null,
                log.getResourceType() != null ? log.getResourceType().name() : null,
                log.getResourceId(),
                log.getContent(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}
