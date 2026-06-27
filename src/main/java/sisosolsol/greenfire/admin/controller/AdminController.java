package sisosolsol.greenfire.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sisosolsol.greenfire.admin.dto.*;
import sisosolsol.greenfire.admin.service.AdminService;
import sisosolsol.greenfire.common.security.model.AuthUser;

import java.util.UUID;

@Tag(name = "관리자", description = "관리자 전용 API (대시보드, 회원 관리, 챌린지 운영)")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final sisosolsol.greenfire.challenge.service.ChallengeService challengeService;

    /** 챌린지 상태 전이 + 보상을 즉시 실행. 데모/디버깅용. cron(매일 1:05)과 동일 로직. */
    @Operation(summary = "챌린지 상태 전이 + 보상 즉시 실행 (데모/디버깅용)")
    @PostMapping("/challenges/run-transitions")
    public ResponseEntity<sisosolsol.greenfire.challenge.service.ChallengeService.TransitionReport> runChallengeTransitions() {
        return ResponseEntity.ok(challengeService.runStatusTransitions());
    }

    @Operation(summary = "관리자 대시보드 통계 조회")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @Operation(summary = "회원 목록 조회 (페이징, 키워드 검색)")
    @GetMapping("/members")
    public ResponseEntity<MemberPageResponse> getMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(adminService.getMembers(page, size, keyword));
    }

    @Operation(summary = "회원 권한 변경")
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

    @Operation(summary = "회원 상세 정보 조회")
    @GetMapping("/members/{userId}")
    public ResponseEntity<MemberDetailResponse> getMemberDetail(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getMemberDetail(userId));
    }

    @Operation(summary = "회원 정지")
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

    @Operation(summary = "회원 정지 해제")
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
