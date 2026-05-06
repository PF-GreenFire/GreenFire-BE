package sisosolsol.greenfire.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import sisosolsol.greenfire.admin.dto.*;
import sisosolsol.greenfire.admin.service.AdminService;
import sisosolsol.greenfire.common.security.model.AuthUser;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final sisosolsol.greenfire.challenge.service.ChallengeService challengeService;

    /** 챌린지 상태 전이 + 보상을 즉시 실행. 데모/디버깅용. cron(매일 1:05)과 동일 로직. */
    @PostMapping("/challenges/run-transitions")
    public ResponseEntity<sisosolsol.greenfire.challenge.service.ChallengeService.TransitionReport> runChallengeTransitions() {
        return ResponseEntity.ok(challengeService.runStatusTransitions());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/members")
    public ResponseEntity<MemberPageResponse> getMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(adminService.getMembers(page, size, keyword));
    }

    @PutMapping("/members/{userId}/role")
    public ResponseEntity<Void> changeUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody RoleChangeRequest request,
            Authentication authentication
    ) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        adminService.changeUserRole(userId, request.role(), currentUser.userId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members/{userId}")
    public ResponseEntity<MemberDetailResponse> getMemberDetail(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getMemberDetail(userId));
    }

    @PostMapping("/members/{userId}/suspend")
    public ResponseEntity<Void> suspendMember(
            @PathVariable UUID userId,
            @Valid @RequestBody SuspendRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        adminService.suspendMember(userId, currentUser.userId(), request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/members/{userId}/suspend")
    public ResponseEntity<Void> unsuspendMember(
            @PathVariable UUID userId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();
        adminService.unsuspendMember(userId, currentUser.userId(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}
