package sisosolsol.greenfire.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.ToString;
import sisosolsol.greenfire.common.enums.user.Gender;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@ToString
public class UpdateUserDTO {

    private UUID userCode;
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
    private String nickname;
    @Pattern(regexp = "^[가-힣]{2,20}$", message = "이름은 한글 2자 이상 20자 이하여야 합니다")
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;
//    private Gender gender;
//    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phone;
    private String email;

    private boolean deleteProfileImage;
}
