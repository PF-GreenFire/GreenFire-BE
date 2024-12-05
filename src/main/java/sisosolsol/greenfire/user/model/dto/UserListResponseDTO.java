package sisosolsol.greenfire.user.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.ToString;
import sisosolsol.greenfire.common.enums.user.UserStatus;
import sisosolsol.greenfire.common.security.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@ToString
public class UserListResponseDTO {

    private UUID userCode;
    private String name;
    private String email;
    private UserStatus status;
    private UserRole role;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

}
