package sisosolsol.greenfire.user.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.common.exception.BadRequestException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.common.security.model.CustomUserDetails;
import sisosolsol.greenfire.user.dao.UserMapper;
import sisosolsol.greenfire.user.dto.ChallengeSummaryDTO;
import sisosolsol.greenfire.user.dto.ScrapbookSummaryDTO;
import sisosolsol.greenfire.user.dto.UserDTO;
import sisosolsol.greenfire.user.dto.UserProfileDTO;
import sisosolsol.greenfire.user.dto.UserUpdateDTO;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public UserDTO getUserProfile(UUID userCode) {
//        try {
//            return Optional.ofNullable(userMapper.findByUserCode(userCode))
//                    .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));
//        } catch (DataIntegrityViolationException e) {
//            throw new BadRequestException(ExceptionCode.INVALID_FOREIGN_KEY);
//        } catch (DataAccessException e) {
//            throw new BadRequestException(ExceptionCode.DATABASE_ACCESS_ERROR);
//        }
        return userMapper.findByUserCode(userCode);
    }

    @Transactional
    public UserDTO updateUserProfile(UUID userCode, UserUpdateDTO request) {
        try {
            UserDTO user = Optional.ofNullable(userMapper.findByUserCode(userCode))
                    .orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));

            userMapper.updateUserProfile(userCode, request);

            return userMapper.findByUserCode(userCode);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ExceptionCode.INVALID_FOREIGN_KEY);
        } catch (DataAccessException e) {
            throw new BadRequestException(ExceptionCode.DATABASE_ACCESS_ERROR);
        }
    }

    public UserProfileDTO getUserSummaryData(UUID userCode) {
        ScrapbookSummaryDTO scrapbookSummary = userMapper.getScrapbookSummary(userCode);

        int challengeTotalCount = userMapper.countParticipatingChallenge(userCode);
        List<ChallengeDTO> challenges = userMapper.getChallengeSummary(userCode);
        ChallengeSummaryDTO challengeSummary = ChallengeSummaryDTO.builder()
                                                                .totalCount(challengeTotalCount)
                                                                .challenges(challenges)
                                                                .build();

        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
                                                    .scrapbookSummary(scrapbookSummary)
                                                    .challengeSummary(challengeSummary)
                                                    .build();
        return userProfileDTO;
    }
}