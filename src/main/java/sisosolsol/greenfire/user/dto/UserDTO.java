package sisosolsol.greenfire.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record UserDTO(
    String nickname,
    String name,
    String email,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birth,
    String profileImage
) {
    public static UserDTO from(User user) {
        return new UserDTO(
            user.getNickname(),
            user.getName(),
            user.getEmail(),
            user.getBirth(),
            "/user/me/profile-image"
        );
    }
}
