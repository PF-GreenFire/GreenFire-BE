package sisosolsol.greenfire.admin.dto;

import sisosolsol.greenfire.user.entity.UserAccount;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MemberDetailResponse(
        UUID userId,
        String email,
        String role,
        Instant deletedAt,
        String deleteReason,
        Instant suspendedUntil,
        String suspendReason,
        boolean isSuspended,
        List<ActivityLogResponse> recentActivities
) {
    public static MemberDetailResponse from(UserAccount user, List<ActivityLogResponse> activities) {
        return new MemberDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : "USER",
                user.getDeletedAt(),
                user.getDeleteReason(),
                user.getSuspendedUntil(),
                user.getSuspendReason(),
                user.isSuspended(),
                activities
        );
    }
}
