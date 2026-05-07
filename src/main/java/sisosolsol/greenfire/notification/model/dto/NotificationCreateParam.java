package sisosolsol.greenfire.notification.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** insert keyProperty 받기용 */
@Getter
@Setter
public class NotificationCreateParam {
    private UUID recipientCode;
    private String type;
    private String title;
    private UUID actorCode;
    private String resourceType;
    private String resourceCode;
    private Integer notificationCode; // OUT
}
