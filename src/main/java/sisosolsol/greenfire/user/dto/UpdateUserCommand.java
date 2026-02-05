package sisosolsol.greenfire.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public record UpdateUserCommand(
    UUID userCode,
    String name,
    String nickname,
    LocalDate birth,
    String storageKey
) {
    public static UpdateUserCommand of(UpdateUserDTO request, String storageKey) {
        return new UpdateUserCommand(request.getUserCode(), request.getName(), request.getNickname(), request.getBirth(), storageKey);
    }
}
