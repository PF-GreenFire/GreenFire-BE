package sisosolsol.greenfire.notification.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.notification.model.dto.NotificationCreateParam;
import sisosolsol.greenfire.notification.model.dto.NotificationDTO;

import java.util.List;
import java.util.UUID;

@Mapper
public interface NotificationMapper {

    void insert(NotificationCreateParam param);

    List<NotificationDTO> findByRecipient(@Param("recipientCode") UUID recipientCode,
                                          @Param("unreadOnly") boolean unreadOnly,
                                          @Param("size") int size);

    int countUnread(@Param("recipientCode") UUID recipientCode);

    void markRead(@Param("notificationCode") Integer notificationCode,
                  @Param("recipientCode") UUID recipientCode);

    void markAllRead(@Param("recipientCode") UUID recipientCode);

    int countByRecipientAndTypeAndResource(
            @Param("recipientCode") UUID recipientCode,
            @Param("type") String type,
            @Param("resourceType") String resourceType,
            @Param("resourceCode") String resourceCode);
}
