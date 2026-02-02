package sisosolsol.greenfire.user.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.user.dao.UserMapper;
import sisosolsol.greenfire.user.dto.ScrapbookSummaryDTO;
import sisosolsol.greenfire.user.dto.UserDTO;
import sisosolsol.greenfire.user.dto.UserUpdateDTO;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public UserDTO getUserProfile(CustomUserDetails loginUser) {
        try {
            return Optional.ofNullable(userMapper.findByUserCode(loginUser.getId()))
                    .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.INVALID_FOREIGN_KEY);
        } catch (DataAccessException e) {
            throw new BadRequestException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }
    }

    @Transactional
    public UserDTO updateUserProfile(CustomUserDetails loginUser, UserUpdateDTO request) {
        try {
            UserDTO user = Optional.ofNullable(userMapper.findByUserCode(loginUser.getId()))
                    .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));

            userMapper.updateUserProfile(loginUser.getId(), request);

            return userMapper.findByUserCode(loginUser.getId());
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.INVALID_FOREIGN_KEY);
        } catch (DataAccessException e) {
            throw new BadRequestException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }
    }

    public ScrapbookSummaryDTO getScrapbookSummary(UUID userCode) {
        return userMapper.getScrapbookSummary(userCode);
    }
}