package sisosolsol.greenfire.badge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sisosolsol.greenfire.badge.service.BadgeService;
import sisosolsol.greenfire.common.security.model.AuthUser;

@Tag(name = "뱃지", description = "사용자 뱃지 API")
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    /** NEW 빨간 점 끄기. 본인이 본 뱃지 한 개를 viewed=true로. */
    @Operation(summary = "뱃지 NEW 표시 끄기 (열람 처리)")
    @PostMapping("/{badgeCode}/view")
    public ResponseEntity<Void> markViewed(@PathVariable String badgeCode,
                                           @AuthenticationPrincipal AuthUser user) {
        if (user == null) return ResponseEntity.status(401).build();
        badgeService.markViewed(user.userId(), badgeCode);
        return ResponseEntity.ok().build();
    }
}
