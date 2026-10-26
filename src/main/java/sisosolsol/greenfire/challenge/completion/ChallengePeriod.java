package sisosolsol.greenfire.challenge.completion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 챌린지 기간 (시작일·종료일 모두 포함). 날짜 판정은 항상 한국 시간 기준.
 */
public record ChallengePeriod(LocalDate start, LocalDate end) {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public ChallengePeriod {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must not be before start");
        }
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    /** 기간의 총 일수 (양 끝 포함) */
    public int days() {
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(start) && !date.isAfter(end);
    }
}
