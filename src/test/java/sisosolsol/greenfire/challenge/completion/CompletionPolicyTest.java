package sisosolsol.greenfire.challenge.completion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sisosolsol.greenfire.common.exception.CustomException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 완주 조건 도메인 — DB 없이 인증 날짜만으로 판정을 검증한다 */
class CompletionPolicyTest {

    // 2026-11-02(월) ~ 2026-11-15(일): 14일, 정확히 2주
    private static final LocalDate START = LocalDate.of(2026, 11, 2);
    private static final ChallengePeriod TWO_WEEKS = new ChallengePeriod(START, START.plusDays(13));

    private static List<LocalDate> days(int... offsets) {
        List<LocalDate> list = new ArrayList<>();
        for (int offset : offsets) list.add(START.plusDays(offset));
        return list;
    }

    private static CertificationLog log(int... offsets) {
        return new CertificationLog(TWO_WEEKS, days(offsets));
    }

    @Nested
    @DisplayName("CertificationLog")
    class CertificationLogTest {

        @Test
        @DisplayName("같은 날 여러 번 인증해도 1일, 기간 밖 날짜는 버린다")
        void dedupesAndClipsToPeriod() {
            List<LocalDate> dates = days(0, 0, 1, 20);
            dates.add(START.minusDays(1));

            CertificationLog log = new CertificationLog(TWO_WEEKS, dates);

            assertThat(log.certifiedDayCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("가장 긴 연속 인증 일수를 센다")
        void longestStreak() {
            assertThat(log(0, 1, 2, 5, 6, 7, 8).longestStreak()).isEqualTo(4);
            assertThat(log().longestStreak()).isZero();
        }
    }

    @Nested
    @DisplayName("조건 유형별 판정")
    class Rules {

        @Test
        @DisplayName("TOTAL_DAYS: 기간 중 인증일 수가 목표 이상이면 충족")
        void totalDays() {
            CompletionRule rule = CompletionRuleType.TOTAL_DAYS.create(3, TWO_WEEKS);

            assertThat(rule.evaluate(log(0, 4, 9)).satisfied()).isTrue();
            assertThat(rule.evaluate(log(0, 0, 4)).satisfied()).isFalse(); // 같은 날 2회 = 1일
            assertThat(rule.describe()).isEqualTo("기간 중 3일 이상 인증");
        }

        @Test
        @DisplayName("EVERY_DAY: 기간 모든 날 인증해야 충족, 목표 = 기간 일수")
        void everyDay() {
            CompletionRule rule = CompletionRuleType.EVERY_DAY.create(null, TWO_WEEKS);
            int[] all = new int[14];
            for (int i = 0; i < 14; i++) all[i] = i;

            assertThat(rule.evaluate(log(all)).satisfied()).isTrue();
            RuleProgress missingOne = rule.evaluate(log(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
            assertThat(missingOne.satisfied()).isFalse();
            assertThat(missingOne.target()).isEqualTo(14);
            assertThat(rule.targetCount()).isNull();
        }

        @Test
        @DisplayName("STREAK: 한 번이라도 N일 연속 인증하면 충족, 진행도는 목표에서 멈춘다")
        void streak() {
            CompletionRule rule = CompletionRuleType.STREAK.create(5, TWO_WEEKS);

            assertThat(rule.evaluate(log(0, 1, 2, 3, 5, 6, 7)).satisfied()).isFalse();
            RuleProgress done = rule.evaluate(log(3, 4, 5, 6, 7, 8, 9, 10));
            assertThat(done.satisfied()).isTrue();
            assertThat(done.current()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("횟수 검증 (기간 기준)")
    class CountValidation {

        @Test
        @DisplayName("횟수가 필요한 유형은 null이나 최소값 미만을 거부한다")
        void rejectsMissingOrTooSmall() {
            assertThatThrownBy(() -> CompletionRuleType.TOTAL_DAYS.create(null, TWO_WEEKS))
                    .isInstanceOf(CustomException.class);
            assertThatThrownBy(() -> CompletionRuleType.STREAK.create(1, TWO_WEEKS))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("기간 일수를 넘는 누적·연속 일수를 거부한다")
        void rejectsAboveMax() {
            assertThatThrownBy(() -> CompletionRuleType.TOTAL_DAYS.create(15, TWO_WEEKS))
                    .isInstanceOf(CustomException.class);
            assertThatThrownBy(() -> CompletionRuleType.STREAK.create(15, TWO_WEEKS))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("CompletionPolicy")
    class Policy {

        @Test
        @DisplayName("모든 조건을 충족해야 완주 (AND)")
        void allRulesMustPass() {
            CompletionPolicy policy = CompletionPolicy.of(List.of(
                    CompletionRuleType.TOTAL_DAYS.create(5, TWO_WEEKS),
                    CompletionRuleType.STREAK.create(3, TWO_WEEKS)));

            // 6일 인증했지만 연속은 2일이 최대
            CompletionResult scattered = policy.evaluate(log(0, 1, 4, 5, 9, 12));
            // 6일 인증, 3일 연속 포함
            CompletionResult withStreak = policy.evaluate(log(0, 1, 2, 5, 9, 12));

            assertThat(scattered.completed()).isFalse();
            assertThat(scattered.evaluations()).extracting(e -> e.progress().satisfied())
                    .containsExactly(true, false);
            assertThat(withStreak.completed()).isTrue();
        }

        @Test
        @DisplayName("조건이 없으면 기본 정책: 기간 중 1일 이상 인증")
        void defaultPolicy() {
            CompletionPolicy policy = CompletionPolicy.of(List.of());

            assertThat(policy.evaluate(log()).completed()).isFalse();
            assertThat(policy.evaluate(log(13)).completed()).isTrue();
        }

        @Test
        @DisplayName("같은 유형 중복과 4개 이상 조건을 거부한다")
        void rejectsDuplicatesAndTooMany() {
            assertThatThrownBy(() -> CompletionPolicy.of(List.of(
                    CompletionRuleType.TOTAL_DAYS.create(3, TWO_WEEKS),
                    CompletionRuleType.TOTAL_DAYS.create(5, TWO_WEEKS))))
                    .hasMessageContaining("같은 유형");

            assertThatThrownBy(() -> CompletionPolicy.of(List.of(
                    CompletionRuleType.TOTAL_DAYS.create(3, TWO_WEEKS),
                    CompletionRuleType.EVERY_DAY.create(null, TWO_WEEKS),
                    CompletionRuleType.STREAK.create(2, TWO_WEEKS),
                    CompletionRuleType.STREAK.create(3, TWO_WEEKS))))
                    .hasMessageContaining("최대 3개");
        }
    }
}
