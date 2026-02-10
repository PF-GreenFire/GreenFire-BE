package sisosolsol.greenfire.admin.dto;

import sisosolsol.greenfire.user.entity.UserAccount;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
        UUID userId,
        String email,
        String role,
        Instant deletedAt,
        Instant suspendedUntil,
        boolean isSuspended
) {
    public static MemberResponse from(UserAccount user) {
        return new MemberResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getDeletedAt(),
                user.getSuspendedUntil(),
                user.isSuspended()
        );
    }
}
