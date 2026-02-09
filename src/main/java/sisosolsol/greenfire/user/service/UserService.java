package sisosolsol.greenfire.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.user.model.dao.UserMapper;
import sisosolsol.greenfire.user.model.dto.UserDTO;
import sisosolsol.greenfire.user.model.dto.UserUpdateDTO;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public UserDTO getUserProfile(UUID userId) {
        try {
            return Optional.ofNullable(userMapper.findByUserCode(userId))
                    .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.INVALID_FOREIGN_KEY);
        } catch (DataAccessException e) {
            throw new BadRequestException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }
    }

    @Transactional
    public UserDTO updateUserProfile(UUID userId, UserUpdateDTO request) {
        try {
            Optional.ofNullable(userMapper.findByUserCode(userId))
                    .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));

            userMapper.updateUserProfile(userId, request);

            return userMapper.findByUserCode(userId);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.INVALID_FOREIGN_KEY);
        } catch (DataAccessException e) {
            throw new BadRequestException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }
    }
}