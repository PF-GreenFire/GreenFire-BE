package sisosolsol.greenfire.auth.dto;

import java.util.UUID;

public record MeResponse(
        boolean ok,
        UUID userId,
        String email,
        String role
) {}
