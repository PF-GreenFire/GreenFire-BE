package sisosolsol.greenfire.challenge.model.dao;

import org.apache.ibatis.annotations.Mapper;
import sisosolsol.greenfire.challenge.model.dto.ChallengeCreateDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengePartDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeSearchCondition;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ChallengeMapper {

    void registChallenge(ChallengeCreateDTO challengeCreate);

    int countChallenges(ChallengeSearchCondition condition);

    List<ChallengeDTO> selectChallenges(ChallengeSearchCondition condition);

    ChallengeDTO selectChallengeByCode(Integer challengeCode);

    int countCurrentParticipants(Integer challengeCode);

    void insertChallengePart(ChallengePartDTO challengePart);

    ChallengePartDTO selectChallengePart(Integer challengeCode, UUID userCode);

    int cancelChallengePart(Integer challengeCode, UUID userCode);

    int countActiveChallenges();
}
