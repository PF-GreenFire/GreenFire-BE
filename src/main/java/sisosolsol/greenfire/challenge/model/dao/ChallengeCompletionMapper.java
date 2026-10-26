package sisosolsol.greenfire.challenge.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.completion.CertificationDayRow;
import sisosolsol.greenfire.challenge.model.dto.completion.CompletionRuleRow;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ChallengeCompletionMapper {

    List<CompletionRuleRow> selectRules(@Param("challengeCode") Integer challengeCode);

    void deleteRules(@Param("challengeCode") Integer challengeCode);

    void insertRules(@Param("challengeCode") Integer challengeCode,
                     @Param("rules") List<CompletionRuleRow> rules);

    /** userCode가 null이면 챌린지 전체 참여자 */
    List<CertificationDayRow> selectCertificationDays(@Param("challengeCode") Integer challengeCode,
                                                      @Param("userCode") UUID userCode);

    int updatePartCompleted(@Param("challengeCode") Integer challengeCode,
                            @Param("userCode") UUID userCode,
                            @Param("completed") boolean completed);

    /** 종료됐지만 아직 완주 평가를 하지 않은 챌린지 */
    List<ChallengeDTO> selectChallengesToEvaluate();

    int markEvaluated(@Param("challengeCode") Integer challengeCode);
}
