package sisosolsol.greenfire.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.user.model.dao.UserMapper;
import sisosolsol.greenfire.user.model.dto.UserDTO;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public UserDTO getUserProfile(CustomUserDetails loginUser) {
        try {
            UserDTO userDTO = userMapper.findByUserCode(loginUser.getId());
            if (userDTO == null) {
                throw new BadRequestException(ExceptionCode.UserNotFoundException);
            }
            return userDTO;
        } catch (DataAccessException e) {
            if (e instanceof DataIntegrityViolationException) {
                throw new BadRequestException(ExceptionCode.InvalidForeignKeyException);
            } else {
                // TODO: 적절한 예외 처리나 로깅 추가
                throw new BadRequestException(ExceptionCode.DatabaseAccessException);
            }
        }
    }
}
