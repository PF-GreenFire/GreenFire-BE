package sisosolsol.greenfire.challenge.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.challenge.completion.ChallengePeriod;
import sisosolsol.greenfire.challenge.completion.CompletionPolicy;
import sisosolsol.greenfire.challenge.completion.CompletionResult;
import sisosolsol.greenfire.challenge.completion.CompletionRuleType;
import sisosolsol.greenfire.challenge.model.dao.ChallengeCompletionMapper;
import sisosolsol.greenfire.challenge.model.dto.ChallengeDTO;
import sisosolsol.greenfire.challenge.model.dto.completion.CertificationDayRow;
import sisosolsol.greenfire.challenge.model.dto.completion.CompletionRuleRequest;
import sisosolsol.greenfire.challenge.model.dto.completion.CompletionRuleRow;
import sisosolsol.greenfire.common.exception.CustomException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeCompletionServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 2);
    private static final ChallengePeriod PERIOD = new ChallengePeriod(START, START.plusDays(6));

    @Mock
    private ChallengeCompletionMapper completionMapper;

    @InjectMocks
    private ChallengeCompletionService completionService;

    private static CompletionRuleRequest request(CompletionRuleType type, Integer count) {
        CompletionRuleRequest req = new CompletionRuleRequest();
        req.setRuleType(type);
        req.setTargetCount(count);
        return req;
    }

    private static ChallengeDTO challenge() {
        ChallengeDTO ch = new ChallengeDTO();
        ch.setChallengeCode(1);
        ch.setStartDate(PERIOD.start());
        ch.setEndDate(PERIOD.end());
        return ch;
    }

    private static CertificationDayRow day(UUID user, int offset) {
        CertificationDayRow row = new CertificationDayRow();
        row.setUserCode(user);
        row.setCertDate(START.plusDays(offset));
        return row;
    }

    @Test
    @DisplayName("buildPolicy: 요청이 없으면 기본 정책, 유형이 빠진 요청은 거부")
    void buildPolicy_defaultsAndValidation() {
        assertThat(completionService.buildPolicy(null, PERIOD).rules())
                .extracting(r -> r.type()).containsExactly(CompletionRuleType.TOTAL_DAYS);

        assertThatThrownBy(() -> completionService.buildPolicy(List.of(request(null, 3)), PERIOD))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("유형");
    }

    @Test
    @DisplayName("savePolicy: 기존 조건을 지우고 순서대로 저장")
    @SuppressWarnings("unchecked")
    void savePolicy_replacesRows() {
        CompletionPolicy policy = completionService.buildPolicy(List.of(
                request(CompletionRuleType.STREAK, 3),
                request(CompletionRuleType.EVERY_DAY, null)), PERIOD);

        completionService.savePolicy(1, policy);

        ArgumentCaptor<List<CompletionRuleRow>> rows = ArgumentCaptor.forClass(List.class);
        verify(completionMapper).deleteRules(1);
        verify(completionMapper).insertRules(eq(1), rows.capture());
        assertThat(rows.getValue()).extracting(CompletionRuleRow::getRuleType, CompletionRuleRow::getTargetCount,
                        CompletionRuleRow::getRuleOrder)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("STREAK", 3, 0),
                        org.assertj.core.groups.Tuple.tuple("EVERY_DAY", null, 1));
    }

    @Test
    @DisplayName("revalidate: 기간을 줄여 조건이 불가능해지면 거부")
    void revalidate_rejectsWhenPeriodShrinks() {
        CompletionPolicy policy = completionService.buildPolicy(
                List.of(request(CompletionRuleType.STREAK, 7)), PERIOD);
        ChallengePeriod shorter = new ChallengePeriod(START, START.plusDays(4));

        assertThatThrownBy(() -> completionService.revalidate(policy, shorter))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("evaluateAll: 저장된 조건으로 참여자별 판정, 인증이 없는 참여자도 결과에 포함")
    void evaluateAll_perParticipant() {
        UUID diligent = UUID.randomUUID();
        UUID lazy = UUID.randomUUID();
        UUID absent = UUID.randomUUID();
        when(completionMapper.selectRules(1))
                .thenReturn(List.of(new CompletionRuleRow("TOTAL_DAYS", 3, 0)));
        when(completionMapper.selectCertificationDays(1, null)).thenReturn(List.of(
                day(diligent, 0), day(diligent, 2), day(diligent, 5),
                day(lazy, 1)));

        Map<UUID, CompletionResult> results =
                completionService.evaluateAll(challenge(), List.of(diligent, lazy, absent));

        assertThat(results.get(diligent).completed()).isTrue();
        assertThat(results.get(lazy).completed()).isFalse();
        assertThat(results.get(absent).completed()).isFalse();
        assertThat(results.get(absent).certifiedDays()).isZero();
    }
}
