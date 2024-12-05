package sisosolsol.greenfire.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.user.model.dao.UserMapper;
import sisosolsol.greenfire.user.model.dto.UserListResponseDTO;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserMapper userMapper;

    public List<UserListResponseDTO> getAllUsers() {
        try {
            return Optional.ofNullable(userMapper.findAll())
                    .filter(users -> !users.isEmpty())
                    .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.INVALID_FOREIGN_KEY);
        } catch (DataAccessException e) {
            throw new BadRequestException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }
    }
}
