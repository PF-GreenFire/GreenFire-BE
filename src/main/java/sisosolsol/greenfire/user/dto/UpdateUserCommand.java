package sisosolsol.greenfire.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record UpdateUserCommand(
    UUID userCode,
    String name,
    String nickname,
    LocalDate birth,
    String phone,
    String email,
    String storageKey,
    boolean deleteProfileImage
) {
    public static UpdateUserCommand of(UUID userCode, UpdateUserDTO request, String storageKey) {
        return UpdateUserCommand.builder()
            .userCode(userCode)
            .name(request.getName())
            .nickname(request.getNickname())
            .birth(request.getBirth())
            .phone(request.getPhone())
            .email(request.getEmail())
            .storageKey(storageKey)
            .deleteProfileImage(request.isDeleteProfileImage())
            .build();
    }
}
