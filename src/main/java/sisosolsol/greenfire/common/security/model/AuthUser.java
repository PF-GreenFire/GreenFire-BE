package sisosolsol.greenfire.common.security.model;

import java.util.UUID;

public record AuthUser(
        UUID userId,
        String email,
        String role
) {}
