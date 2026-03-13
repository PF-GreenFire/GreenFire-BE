package sisosolsol.greenfire.challenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.challenge.model.dao.ChallengeMapper;
import sisosolsol.greenfire.challenge.model.dto.ChallengeCreateDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengePartDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeSearchCondition;
import sisosolsol.greenfire.challenge.model.dto.ChallengeSearchDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeUpdateDTO;
import sisosolsol.greenfire.common.enums.challenge.ChallengeStatus;
import sisosolsol.greenfire.common.exception.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static sisosolsol.greenfire.common.exception.type.ExceptionCode.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeMapper challengeMapper;

    public Integer registChallenge(ChallengeCreateDTO challengeCreate, UUID userId) {
        challengeCreate.setHostUser(userId);
        challengeMapper.registChallenge(challengeCreate);
        return challengeCreate.getChallengeCode();
    }

    public ChallengeSearchDTO getChallenges(int page, int size, String searchKeyword, Integer categoryCode) {
        ChallengeSearchCondition condition = new ChallengeSearchCondition();
        condition.setOffset(page * size);
        condition.setSize(size);
        condition.setSearchKeyword(searchKeyword);
        condition.setCategoryCode(categoryCode);

        // 전체 개수 조회
        int totalCount = challengeMapper.countChallenges(condition);

        // 페이지 데이터 조회
        List<ChallengeDTO> challenges = challengeMapper.selectChallenges(condition);

        // 결과 데이터 설정
        ChallengeSearchDTO result = new ChallengeSearchDTO();
        result.setChallenges(challenges);
        result.setCurrentPage(page);
        result.setTotalCount(totalCount);
        result.setHasNext(totalCount > (page + 1) * size);

        return result;
    }

    public ChallengeDTO getChallengeDetails(Integer challengeCode) {

        ChallengeDTO challengeDetails = challengeMapper.selectChallengeByCode(challengeCode);

        return challengeDetails;
    }

    @Transactional
    public void updateChallenge(Integer challengeCode, ChallengeUpdateDTO update, UUID userId) {

        // 1. 챌린지 조회
        ChallengeDTO challenge = challengeMapper.selectChallengeByCode(challengeCode);
        if (challenge == null) {
            throw new CustomException(CHALLENGE_NOT_FOUND);
        }

        // 2. 호스트 권한 검증
        if (!challenge.getHostUser().equals(userId)) {
            throw new CustomException(ACCESS_DENIED);
        }

        // 3. RECRUITING 상태이고 시작 전인 경우만 수정 가능
        if (challenge.getChallengeStatus() != ChallengeStatus.RECRUITING) {
            throw new CustomException(CHALLENGE_INVALID_STATUS);
        }
        if (!LocalDateTime.now().toLocalDate().isBefore(challenge.getStartDate())) {
            throw new CustomException(CHALLENGE_ALREADY_STARTED);
        }

        // 4. 모집 인원을 현재 참여자 수보다 적게 설정하는 경우 방지
        if (update.getRecruitmentNum() != null) {
            int currentParticipants = challengeMapper.countCurrentParticipants(challengeCode);
            if (update.getRecruitmentNum() < currentParticipants) {
                throw new CustomException(CHALLENGE_FULL_CAPACITY);
            }
        }

        challengeMapper.updateChallenge(challengeCode, update);
    }

    @Transactional
    public void deleteChallenge(Integer challengeCode, UUID userId) {

        // 1. 챌린지 조회
        ChallengeDTO challenge = challengeMapper.selectChallengeByCode(challengeCode);
        if (challenge == null) {
            throw new CustomException(CHALLENGE_NOT_FOUND);
        }

        // 2. 호스트 권한 검증
        if (!challenge.getHostUser().equals(userId)) {
            throw new CustomException(ACCESS_DENIED);
        }

        // 3. 이미 취소된 경우
        if (challenge.getChallengeStatus() == ChallengeStatus.CANCELLED) {
            throw new CustomException(CHALLENGE_ALREADY_CANCELLED);
        }

        // 4. status → CANCELLED (소프트 딜리트)
        challengeMapper.updateChallengeStatus(challengeCode, ChallengeStatus.CANCELLED);
    }

    @Transactional
    public void applyChallenge(Integer challengeCode, UUID userId) {

        // 1. 챌린지 정보 조회
        ChallengeDTO challenge = challengeMapper.selectChallengeByCode(challengeCode);
        if (challenge == null) {
            throw new CustomException(CHALLENGE_NOT_FOUND);
        }

        // 2. 유효성 검사
        validateChallengeStatus(challenge, ChallengeAction.APPLY, userId);

        // 3. 현재 참여자 수 확인
        int currentParticipants = challengeMapper.countCurrentParticipants(challengeCode);

        // 4. 모집 정원 확인
        if (currentParticipants >= challenge.getRecruitmentNum()) {
            throw new CustomException(CHALLENGE_FULL_CAPACITY);
        }

        // 5. 챌린지 참여 정보 저장
        ChallengePartDTO challengePart = new ChallengePartDTO();
        challengePart.setChallengeCode(challengeCode);
        challengePart.setUserCode(userId);
        challengeMapper.insertChallengePart(challengePart);
    }

    @Transactional
    public void cancelChallengePart(Integer challengeCode, UUID userId) {

        // 1. 챌린지 정보 조회
        ChallengeDTO challenge = challengeMapper.selectChallengeByCode(challengeCode);
        if (challenge == null) {
            throw new CustomException(CHALLENGE_NOT_FOUND);
        }

        // 2. 참여 정보 확인
        ChallengePartDTO participation = challengeMapper.selectChallengePart(challengeCode, userId);
        if (participation == null) {
            throw new CustomException(CHALLENGE_NOT_PARTICIPATED);
        }

        // 3. 챌린지 상태 검증
        validateChallengeStatus(challenge, ChallengeAction.CANCEL, userId);
        // TODO : 이미 관련 게시글이나 기타 연관 관계가 생긴 경우 처리하는 로직도 추후 회의 후 구현

        // 4. 챌린지 취소 처리
        int result = challengeMapper.cancelChallengePart(challengeCode, userId);
        if (result == 0) {
            throw new CustomException(CHALLENGE_CANCEL_FAILED);
        }
    }

    /**
     * 챌린지 상태 검증을 위한 enum
     */
    private enum ChallengeAction {
        APPLY,   // 챌린지 참여
        CANCEL   // 챌린지 취소
    }

    /**
     * 챌린지 상태 검증
     * @param challenge 챌린지 정보
     * @param action 수행하려는 작업
     */
    private void validateChallengeStatus(ChallengeDTO challenge, ChallengeAction action, UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        switch (action) {
            case APPLY:
                // 모집 중이 아닌 경우
                if (challenge.getChallengeStatus() != ChallengeStatus.RECRUITING) {
                    throw new CustomException(CHALLENGE_INVALID_STATUS);
                }

                // 이미 시작된 챌린지인 경우
                if (!now.toLocalDate().isBefore(challenge.getStartDate())) {
                    throw new CustomException(CHALLENGE_ALREADY_STARTED);
                }

                // 이미 참여 중인지 확인
                ChallengePartDTO existingPart = challengeMapper.selectChallengePart(
                        challenge.getChallengeCode(), userId
                );
                if (existingPart != null) {
                    throw new CustomException(CHALLENGE_ALREADY_PARTICIPATED);
                }
                break;

            case CANCEL:
                // 챌린지 상태별 취소 가능 여부 검증
                switch (challenge.getChallengeStatus()) {
                    case RECRUITING:
                        // 시작일 이전에만 취소 가능
                        if (!now.toLocalDate().isBefore(challenge.getStartDate())) {
                            throw new CustomException(CHALLENGE_ALREADY_STARTED);
                        }
                        break;
                    case ONGOING:
                        throw new CustomException(CHALLENGE_ALREADY_STARTED);
                    case CLOSED:
                        throw new CustomException(CHALLENGE_ALREADY_COMPLETED);
                    case CANCELLED:
                        throw new CustomException(CHALLENGE_ALREADY_CANCELLED);
                    case PAUSED:
                        throw new CustomException(CHALLENGE_PAUSED);
                }
                break;
        }
    }
}
