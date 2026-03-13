package sisosolsol.greenfire.challenge.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.challenge.model.dto.ChallengeCreateDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengePartDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeSearchCondition;
import sisosolsol.greenfire.challenge.model.dto.ChallengeUpdateDTO;
import sisosolsol.greenfire.common.enums.challenge.ChallengeStatus;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ChallengeMapper {

    void registChallenge(ChallengeCreateDTO challengeCreate);

    int countChallenges(ChallengeSearchCondition condition);

    List<ChallengeDTO> selectChallenges(ChallengeSearchCondition condition);

    ChallengeDTO selectChallengeByCode(Integer challengeCode);

    int updateChallenge(@Param("challengeCode") Integer challengeCode,
                        @Param("update") ChallengeUpdateDTO update);

    int updateChallengeStatus(@Param("challengeCode") Integer challengeCode,
                              @Param("status") ChallengeStatus status);

    int countCurrentParticipants(Integer challengeCode);

    void insertChallengePart(ChallengePartDTO challengePart);

    ChallengePartDTO selectChallengePart(Integer challengeCode, UUID userCode);

    int cancelChallengePart(Integer challengeCode, UUID userCode);

    int countActiveChallenges();
}
