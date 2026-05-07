package sisosolsol.greenfire.notification.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NotificationDTO {
    private Integer notificationCode;
    private String type;            // NotificationType.name()
    private String emoji;           // 화면용 prefix
    private String title;
    private UUID actorCode;
    private String resourceType;
    private String resourceCode;
    private boolean read;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
