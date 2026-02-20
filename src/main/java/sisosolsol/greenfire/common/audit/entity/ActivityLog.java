package sisosolsol.greenfire.common.audit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_user_created", columnList = "user_code, created_at"),
        @Index(name = "idx_activity_resource", columnList = "resource_type, resource_id, action_type"),
        @Index(name = "idx_activity_trending", columnList = "resource_type, action_type, created_at")
})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_code")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ActivityLog(UUID userId, ActionType actionType, ResourceType resourceType,
                       String resourceId, String content, String ipAddress) {
        this.userId = userId;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.content = content;
        this.ipAddress = ipAddress;
        this.createdAt = Instant.now();
    }
}
