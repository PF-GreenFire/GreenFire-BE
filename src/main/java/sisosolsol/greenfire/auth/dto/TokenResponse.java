package sisosolsol.greenfire.auth.dto;

public record TokenResponse(
        String accessToken,
        long expiresInSeconds
) {}
