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
import sisosolsol.greenfire.challenge.completion.CompletionPolicy;
import sisosolsol.greenfire.challenge.completion.CompletionResult;
import sisosolsol.greenfire.notification.model.NotificationType;
import sisosolsol.greenfire.common.enums.challenge.ChallengeStatus;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.notification.service.NotificationService;
import sisosolsol.greenfire.spark.model.dao.SparkMapper;
import sisosolsol.greenfire.spark.service.SparkService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Mock
    private ChallengeCompletionService completionService;

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
        // 오늘이 기간 안에 들도록 (인증 자격 검증이 날짜도 확인)
        ch.setStartDate(LocalDate.now().minusDays(3));
        ch.setEndDate(LocalDate.now().plusDays(3));
        return ch;
    }

    private static CompletionResult result(boolean completed) {
        return new CompletionResult(completed, 0, List.of());
    }

    // ─── CH-3: 등록 검증 ───

    @Test
    @DisplayName("registChallenge: 정상 입력이면 호스트를 요청자로 지정해 저장한다")
    void registChallenge_savesWithHost() {
        UUID userId = UUID.randomUUID();
        ChallengeCreateDTO dto = validCreate();
        CompletionPolicy policy = CompletionPolicy.defaultPolicy();
        when(completionService.buildPolicy(any(), any())).thenReturn(policy);

        challengeService.registChallenge(dto, userId);

        verify(challengeMapper).registChallenge(dto);
        verify(completionService).savePolicy(dto.getChallengeCode(), policy);
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

    // ─── 종료 정산: 완주 조건 ───

    @Test
    @DisplayName("runStatusTransitions: 완주자만 xp 보상, 미완주자는 결과 알림, 호스트는 평가 대상에서 제외")
    void runStatusTransitions_rewardsOnlyCompleters() {
        UUID host = UUID.randomUUID();
        UUID finisher = UUID.randomUUID();
        UUID dropout = UUID.randomUUID();
        ChallengeDTO closed = challenge(5, ChallengeStatus.CLOSED, host);
        closed.setXp(30);
        Map<UUID, CompletionResult> results = new LinkedHashMap<>();
        results.put(finisher, result(true));
        results.put(dropout, result(false));

        when(challengeMapper.selectOngoingChallenges()).thenReturn(List.of());
        when(completionService.findChallengesToEvaluate()).thenReturn(List.of(closed));
        when(challengeMapper.selectParticipantCodes(5)).thenReturn(List.of(host, finisher, dropout));
        when(completionService.evaluateAll(closed, List.of(finisher, dropout))).thenReturn(results);

        ChallengeService.TransitionReport report = challengeService.runStatusTransitions();

        verify(sparkService).award(finisher, 30, "CHALLENGE_COMPLETE", "CHALLENGE", 5);
        verify(sparkService, never()).award(eq(dropout), any(Integer.class), anyString(), anyString(), any());
        verify(notificationService).notify(finisher, NotificationType.CHALLENGE_REWARDED, null, "CHALLENGE", "5");
        verify(notificationService).notifyIfAbsent(dropout, NotificationType.CHALLENGE_NOT_COMPLETED, null, "CHALLENGE", "5");
        verify(completionService).recordCompletion(5, finisher, true);
        verify(completionService).recordCompletion(5, dropout, false);
        verify(completionService).markEvaluated(5);
        org.assertj.core.api.Assertions.assertThat(report.rewardsGranted()).isEqualTo(1);
    }

    @Test
    @DisplayName("runStatusTransitions: xp 0 챌린지 완주자는 완주 이력만 남고 '완주' 알림을 받는다")
    void runStatusTransitions_zeroXp_recordsCompletion() {
        UUID participant = UUID.randomUUID();
        ChallengeDTO closed = challenge(6, ChallengeStatus.CLOSED, UUID.randomUUID());
        closed.setXp(0);
        when(challengeMapper.selectOngoingChallenges()).thenReturn(List.of());
        when(completionService.findChallengesToEvaluate()).thenReturn(List.of(closed));
        when(challengeMapper.selectParticipantCodes(6)).thenReturn(List.of(participant));
        when(completionService.evaluateAll(closed, List.of(participant)))
                .thenReturn(Map.of(participant, result(true)));

        ChallengeService.TransitionReport report = challengeService.runStatusTransitions();

        verify(sparkService).award(participant, 0, "CHALLENGE_COMPLETE", "CHALLENGE", 6);
        verify(notificationService).notify(participant, NotificationType.CHALLENGE_COMPLETED, null, "CHALLENGE", "6");
        org.assertj.core.api.Assertions.assertThat(report.rewardsGranted()).isZero();
    }

    @Test
    @DisplayName("assertCanCertify: 상태는 ONGOING이어도 종료일이 지났으면 인증 불가")
    void assertCanCertify_afterEndDate_throws() {
        UUID userId = UUID.randomUUID();
        ChallengeDTO ch = challenge(1, ChallengeStatus.ONGOING, UUID.randomUUID());
        ch.setStartDate(LocalDate.now().minusDays(7));
        ch.setEndDate(LocalDate.now().minusDays(1));
        when(challengeMapper.selectChallengeByCode(1)).thenReturn(ch);

        assertThatThrownBy(() -> challengeService.assertCanCertify(1, userId))
                .hasMessageContaining("진행 중인");
    }

    @Test
    @DisplayName("getMyProgress: 참여자도 호스트도 아니면 CHALLENGE_NOT_PARTICIPATED")
    void getMyProgress_nonParticipant_throws() {
        UUID userId = UUID.randomUUID();
        when(challengeMapper.selectChallengeByCode(1))
                .thenReturn(challenge(1, ChallengeStatus.ONGOING, UUID.randomUUID()));
        when(challengeMapper.selectChallengePart(1, userId)).thenReturn(null);

        assertThatThrownBy(() -> challengeService.getMyProgress(1, userId))
                .hasMessageContaining("참여하지 않은");
    }
}
