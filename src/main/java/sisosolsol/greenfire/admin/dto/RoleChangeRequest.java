package sisosolsol.greenfire.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoleChangeRequest(
        @NotBlank
        @Pattern(regexp = "USER|ADMIN", message = "역할은 USER 또는 ADMIN만 가능합니다.")
        String role
) {}
