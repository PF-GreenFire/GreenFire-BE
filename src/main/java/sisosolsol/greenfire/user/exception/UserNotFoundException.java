package sisosolsol.greenfire.user.exception;

import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
