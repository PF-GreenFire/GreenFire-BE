package sisosolsol.greenfire.common.exception.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {

    // 401 Error
    UNAUTHORIZED(401, "인증 되지 않은 요청입니다."),

    // 403 Error
    ACCESS_DENIED(403, "허가 되지 않은 요청입니다."),

    InvalidForeignKeyException(1100, "외래 키 제약을 위반한 요청입니다."),

    UserNotFoundException(1201, "사용자를 찾을 수 없습니다."),

    DatabaseAccessException(1301, "데이터베이스 접근 중 오류가 발생했습니다.");

    private final int code;
    private final String message;

}