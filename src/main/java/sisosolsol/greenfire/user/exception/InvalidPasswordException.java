package sisosolsol.greenfire.user.exception;

import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;

public class InvalidPasswordException extends CustomException {

    public InvalidPasswordException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
