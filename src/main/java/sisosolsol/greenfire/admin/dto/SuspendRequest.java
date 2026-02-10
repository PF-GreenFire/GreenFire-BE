package sisosolsol.greenfire.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record SuspendRequest(
        @NotBlank(message = "정지 사유를 입력해주세요.") String reason,
        String suspendUntil
) {
}
