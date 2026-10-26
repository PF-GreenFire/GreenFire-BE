package sisosolsol.greenfire.challenge.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.challenge.model.dao.ChallengeMapper;
import sisosolsol.greenfire.challenge.model.dto.ChallengeCreateDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.ChallengePartDTO;
import sisosolsol.greenfire.common.enums.challenge.ChallengeStatus;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.notification.service.NotificationService;
import sisosolsol.greenfire.spark.model.dao.SparkMapper;
import sisosolsol.greenfire.spark.service.SparkService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeMapper challengeMapper;

    @Mock
    private SparkService sparkService;

    @Mock
    private SparkMapper sparkMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ChallengeService challengeService;

    private ChallengeCreateDTO validCreate() {
        ChallengeCreateDTO dto = new ChallengeCreateDTO();
        dto.setChallengeTitle("텀블러 챌린지");
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(7));
        dto.setRecruitmentNum(10);
        dto.setXp(30);
        return dto;
    }

    private ChallengeDTO challenge(int code, ChallengeStatus status, UUID host) {
        ChallengeDTO ch = new ChallengeDTO();
        ch.setChallengeCode(code);
        ch.setChallengeStatus(status);
        ch.setHostUser(host);
        return ch;
    }

    // ─── CH-3: 등록 검증 ───

    @Test
    @DisplayName("registChallenge: 정상 입력이면 호스트를 요청자로 지정해 저장한다")
    void registChallenge_savesWithHost() {
        UUID userId = UUID.randomUUID();
        ChallengeCreateDTO dto = validCreate();

        challengeService.registChallenge(dto, userId);

        verify(challengeMapper).registChallenge(dto);
        org.assertj.core.api.Assertions.assertThat(dto.getHostUser()).isEqualTo(userId);
    }

    @Test
    @DisplayName("registChallenge: 시작일이 과거면 CHALLENGE_INVALID_PERIOD")
    void registChallenge_rejectsPastStart() {
        ChallengeCreateDTO dto = validCreate();
        dto.setStartDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> challengeService.registChallenge(dto, UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기간");
        verify(challengeMapper, never()).registChallenge(any());
    }

    @Test
    @DisplayName("registChallenge: 종료일이 시작일보다 빠르면 CHALLENGE_INVALID_PERIOD")
    void registChallenge_rejectsEndBeforeStart() {
        ChallengeCreateDTO dto = validCreate();
        dto.setEndDate(dto.getStartDate().minusDays(1));

        assertThatThrownBy(() -> challengeService.registChallenge(dto, UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기간");
    }

    @Test
    @DisplayName("registChallenge: 모집 인원 0 이하, 제목 공백, 음수 xp는 거부한다")
    void registChallenge_rejectsInvalidFields() {
        ChallengeCreateDTO noRecruit = validCreate();
        noRecruit.setRecruitmentNum(0);
        ChallengeCreateDTO blankTitle = validCreate();
        blankTitle.setChallengeTitle("  ");
        ChallengeCreateDTO negativeXp = validCreate();
        negativeXp.setXp(-1);

        assertThatThrownBy(() -> challengeService.registChallenge(noRecruit, UUID.randomUUID()))
                .hasMessageContaining("모집 인원");
        assertThatThrownBy(() -> challengeService.registChallenge(blankTitle, UUID.randomUUID()))
                .hasMessageContaining("제목");
        assertThatThrownBy(() -> challengeService.registChallenge(negativeXp, UUID.randomUUID()))
                .hasMessageContaining("보상");
    }

    // ─── FD-2: 인증 자격 ───

    @Test
    @DisplayName("assertCanCertify: 진행 중 챌린지의 참여자는 통과")
    void assertCanCertify_participantOfOngoing_passes() {
        UUID userId = UUID.randomUUID();
        when(challengeMapper.selectChallengeByCode(1))
                .thenReturn(challenge(1, ChallengeStatus.ONGOING, UUID.randomUUID()));
        when(challengeMapper.selectChallengePart(1, userId)).thenReturn(new ChallengePartDTO());

        assertThatCode(() -> challengeService.assertCanCertify(1, userId)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("assertCanCertify: 호스트는 참여 기록이 없어도 통과")
    void assertCanCertify_host_passes() {
        UUID host = UUID.randomUUID();
        when(challengeMapper.selectChallengeByCode(1))
                .thenReturn(challenge(1, ChallengeStatus.ONGOING, host));

        assertThatCode(() -> challengeService.assertCanCertify(1, host)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("assertCanCertify: 참여하지 않은 사용자는 CHALLENGE_NOT_PARTICIPATED")
    void assertCanCertify_nonParticipant_throws() {
        UUID userId = UUID.randomUUID();
        when(challengeMapper.selectChallengeByCode(1))
                .thenReturn(challenge(1, ChallengeStatus.ONGOING, UUID.randomUUID()));
        when(challengeMapper.selectChallengePart(1, userId)).thenReturn(null);

        assertThatThrownBy(() -> challengeService.assertCanCertify(1, userId))
                .hasMessageContaining("참여하지 않은");
    }

    @Test
    @DisplayName("assertCanCertify: 진행 중이 아니면 CHALLENGE_NOT_ONGOING, 챌린지 미지정이면 CHALLENGE_CODE_REQUIRED")
    void assertCanCertify_notOngoingOrMissing_throws() {
        when(challengeMapper.selectChallengeByCode(1))
                .thenReturn(challenge(1, ChallengeStatus.RECRUITING, UUID.randomUUID()));

        assertThatThrownBy(() -> challengeService.assertCanCertify(1, UUID.randomUUID()))
                .hasMessageContaining("진행 중인");
        assertThatThrownBy(() -> challengeService.assertCanCertify(null, UUID.randomUUID()))
                .hasMessageContaining("선택");
    }

    // ─── CH-6: 참여 신청 잠금 ───

    @Test
    @DisplayName("applyChallenge: 챌린지 행을 잠근 뒤 정원을 확인한다. 없는 챌린지면 NOT_FOUND")
    void applyChallenge_locksRow() {
        when(challengeMapper.lockChallenge(99)).thenReturn(null);

        assertThatThrownBy(() -> challengeService.applyChallenge(99, UUID.randomUUID()))
                .hasMessageContaining("존재하지 않는");
        verify(challengeMapper, never()).insertChallengePart(any());
    }

    // ─── CH-5: xp 0 챌린지도 완주 기록 ───

    @Test
    @DisplayName("runStatusTransitions: xp가 0이어도 참여자 완주 이력을 남기고, 보상 알림은 보내지 않는다")
    void runStatusTransitions_zeroXp_recordsCompletionWithoutRewardNotice() {
        UUID host = UUID.randomUUID();
        UUID participant = UUID.randomUUID();
        ChallengeDTO closed = challenge(5, ChallengeStatus.CLOSED, host);
        closed.setXp(0);
        when(challengeMapper.selectOngoingChallenges()).thenReturn(List.of());
        when(challengeMapper.selectClosedChallenges()).thenReturn(List.of(closed));
        when(challengeMapper.selectParticipantCodes(5)).thenReturn(List.of(host, participant));
        when(sparkMapper.countHistory(participant, "CHALLENGE_COMPLETE", "CHALLENGE", 5)).thenReturn(0);

        ChallengeService.TransitionReport report = challengeService.runStatusTransitions();

        verify(sparkService).award(participant, 0, "CHALLENGE_COMPLETE", "CHALLENGE", 5);
        verify(sparkService, never()).award(eq(host), any(Integer.class), anyString(), anyString(), any());
        verify(notificationService, never()).notify(any(), any(), any(), anyString(), anyString());
        org.assertj.core.api.Assertions.assertThat(report.rewardsGranted()).isZero();
    }
}
