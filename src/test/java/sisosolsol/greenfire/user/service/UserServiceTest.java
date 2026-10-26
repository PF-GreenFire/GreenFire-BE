package sisosolsol.greenfire.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.notification.model.NotificationType;
import sisosolsol.greenfire.notification.service.NotificationService;
import sisosolsol.greenfire.user.dao.UserMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("followUser: 자기 자신은 팔로우할 수 없다")
    void followUser_rejectsSelf() {
        UUID me = UUID.randomUUID();

        assertThatThrownBy(() -> userService.followUser(me, me))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(userMapper, notificationService);
    }

    @Test
    @DisplayName("followUser: 이미 팔로우 중이면(INSERT 0건) 에러 없이 무시하고 알림도 보내지 않는다")
    void followUser_duplicate_isIdempotent() {
        UUID me = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        when(userMapper.followUser(me, target)).thenReturn(0);

        userService.followUser(me, target);

        verify(notificationService, never()).notifyIfAbsent(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("followUser: 새 팔로우면 같은 팔로워 기준 1회만 알림(notifyIfAbsent)")
    void followUser_new_notifiesOnce() {
        UUID me = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        when(userMapper.followUser(me, target)).thenReturn(1);

        userService.followUser(me, target);

        verify(notificationService).notifyIfAbsent(target, NotificationType.FOLLOWED,
                me, "USER", me.toString());
    }
}
