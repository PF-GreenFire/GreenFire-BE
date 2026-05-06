package sisosolsol.greenfire.badge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sisosolsol.greenfire.badge.service.BadgeService;
import sisosolsol.greenfire.common.security.model.AuthUser;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    /** NEW 빨간 점 끄기. 본인이 본 뱃지 한 개를 viewed=true로. */
    @PostMapping("/{badgeCode}/view")
    public ResponseEntity<Void> markViewed(@PathVariable String badgeCode,
                                           @AuthenticationPrincipal AuthUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        badgeService.markViewed(user.userId(), badgeCode);
        return ResponseEntity.ok().build();
    }
}
