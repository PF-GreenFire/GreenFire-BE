package sisosolsol.greenfire.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.common.security.model.AuthUser;
import sisosolsol.greenfire.notification.model.dto.NotificationDTO;
import sisosolsol.greenfire.notification.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> list(
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @RequestParam(value = "size", defaultValue = "30") int size,
            @AuthenticationPrincipal AuthUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        if (size < 1 || size > 100) size = 30;
        return ResponseEntity.ok(notificationService.list(user.userId(), unreadOnly, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> unreadCount(@AuthenticationPrincipal AuthUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(user.userId())));
    }

    @PostMapping("/{notificationCode}/read")
    public ResponseEntity<Void> markRead(@PathVariable Integer notificationCode,
                                         @AuthenticationPrincipal AuthUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        notificationService.markRead(notificationCode, user.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal AuthUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        notificationService.markAllRead(user.userId());
        return ResponseEntity.ok().build();
    }
}
