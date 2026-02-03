package sisosolsol.greenfire.user.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.user.dto.ScrapbookSummaryDTO;
import sisosolsol.greenfire.user.dto.UserDTO;
import sisosolsol.greenfire.user.dto.UserProfileDTO;
import sisosolsol.greenfire.user.dto.UserUpdateDTO;

import java.util.UUID;

@Mapper
public interface UserMapper {

    // 회원 프로필 정보 조회
    UserDTO findByUserCode(UUID userCode);

    // 회원 프로필 정보 수정
    void updateUserProfile(@Param("userCode") UUID userCode, @Param("userDTO") UserUpdateDTO user);

    ScrapbookSummaryDTO getScrapbookSummary(@Param("userCode") UUID userCode);

    List<ChallengeDTO> getChallengeSummary(@Param("userCode") UUID userCode);
}