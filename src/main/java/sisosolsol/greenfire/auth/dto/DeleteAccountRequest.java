package sisosolsol.greenfire.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank String password,
        @NotBlank String reason
) {}
