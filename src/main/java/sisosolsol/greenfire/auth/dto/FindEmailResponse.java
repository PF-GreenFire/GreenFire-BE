package sisosolsol.greenfire.auth.dto;

public record FindEmailResponse(
        boolean exists,
        String maskedEmail
) {}
