package sisosolsol.greenfire.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.admin.dto.*;
import sisosolsol.greenfire.auth.repository.RefreshTokenRepository;
import sisosolsol.greenfire.challenge.model.dao.ChallengeMapper;
import sisosolsol.greenfire.common.audit.entity.ActionType;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.common.audit.repository.ActivityLogRepository;
import sisosolsol.greenfire.common.audit.service.ActivityLogService;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.common.security.model.UserRole;
import sisosolsol.greenfire.report.enums.ReportStatus;
import sisosolsol.greenfire.report.repository.ReportRepository;
import sisosolsol.greenfire.user.entity.UserAccount;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserAccountRepository userAccountRepository;
    private final ReportRepository reportRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ChallengeMapper challengeMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ActivityLogService activityLogService;

    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userAccountRepository.countByDeletedAtIsNull();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        long handledReports = reportRepository.countByStatusNot(ReportStatus.PENDING);
        int activeChallenges = challengeMapper.countActiveChallenges();
        long recentActivities = activityLogRepository.countByCreatedAtAfter(
                Instant.now().minus(24, ChronoUnit.HOURS)
        );

        return new DashboardStatsResponse(totalUsers, pendingReports, handledReports, activeChallenges, recentActivities);
    }

    public MemberPageResponse getMembers(int page, int size, String keyword) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "email"));

        Page<UserAccount> result;
        if (keyword != null && !keyword.isBlank()) {
            result = userAccountRepository.findByEmailContainingIgnoreCase(keyword, pageRequest);
        } else {
            result = userAccountRepository.findAll(pageRequest);
        }

        List<MemberResponse> members = result.getContent().stream()
                .map(MemberResponse::from)
                .toList();

        return new MemberPageResponse(
                members,
                result.getTotalElements(),
                page,
                size,
                result.hasNext()
        );
    }

    @Transactional
    public void changeUserRole(UUID targetUserId, String newRoleName, UUID currentUserId) {
        if (targetUserId.equals(currentUserId)) {
            throw new CustomException(ExceptionCode.CANNOT_CHANGE_OWN_ROLE);
        }

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(newRoleName);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ExceptionCode.INVALID_ROLE);
        }

        if (newRole == UserRole.MANAGER) {
            throw new CustomException(ExceptionCode.INVALID_ROLE);
        }

        UserAccount target = userAccountRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        target.updateRole(newRole);
    }

    public MemberDetailResponse getMemberDetail(UUID userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        List<ActionType> excluded = List.of(ActionType.REFRESH, ActionType.VIEW);
        List<ActivityLogResponse> activities = activityLogRepository
                .findByUserIdAndActionTypeNotInOrderByCreatedAtDesc(userId, excluded, PageRequest.of(0, 50))
                .stream()
                .map(ActivityLogResponse::from)
                .toList();

        return MemberDetailResponse.from(user, activities);
    }

    @Transactional
    public void suspendMember(UUID targetId, UUID adminId, SuspendRequest request, String ipAddress) {
        if (targetId.equals(adminId)) {
            throw new CustomException(ExceptionCode.CANNOT_SUSPEND_SELF);
        }

        UserAccount target = userAccountRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        if (target.getRole() == UserRole.ADMIN) {
            throw new CustomException(ExceptionCode.CANNOT_SUSPEND_ADMIN);
        }

        Instant until;
        if (request.suspendUntil() == null || request.suspendUntil().isBlank()) {
            until = Instant.parse("9999-12-31T23:59:59Z");
        } else {
            until = Instant.parse(request.suspendUntil());
        }

        target.suspend(until, request.reason());
        refreshTokenRepository.revokeAllByUserId(targetId);
        activityLogService.log(adminId, ActionType.SUSPEND, ResourceType.ACCOUNT,
                targetId.toString(), request.reason(), ipAddress);
    }

    @Transactional
    public void unsuspendMember(UUID targetId, UUID adminId, String ipAddress) {
        UserAccount target = userAccountRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        target.unsuspend();
        activityLogService.log(adminId, ActionType.UNSUSPEND, ResourceType.ACCOUNT,
                targetId.toString(), null, ipAddress);
    }
}
