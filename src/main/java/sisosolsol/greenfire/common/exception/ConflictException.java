package sisosolsol.greenfire.common.exception;

import lombok.Getter;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;

@Getter
public class ConflictException extends CustomException {

    public ConflictException(final ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
