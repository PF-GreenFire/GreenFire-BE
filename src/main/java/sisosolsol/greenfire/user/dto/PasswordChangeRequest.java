package sisosolsol.greenfire.user.dto;

public record PasswordChangeRequest(
    String currentPassword,
    String newPassword
) {}
