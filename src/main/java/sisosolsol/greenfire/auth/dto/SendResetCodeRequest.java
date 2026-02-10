package sisosolsol.greenfire.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendResetCodeRequest(
        @Email @NotBlank String email
) {}
