package sisosolsol.greenfire.challenge.completion;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * 한 참여자가 챌린지 기간 중 인증한 "날"의 모음.
 * 하루에 여러 번 인증해도 1일로 센다. 기간 밖의 날짜는 버린다.
 */
public final class CertificationLog {

    private final ChallengePeriod period;
    private final NavigableSet<LocalDate> certifiedDays;

    public CertificationLog(ChallengePeriod period, Collection<LocalDate> certifiedDates) {
        this.period = period;
        TreeSet<LocalDate> days = new TreeSet<>();
        if (certifiedDates != null) {
            for (LocalDate date : certifiedDates) {
                if (period.contains(date)) days.add(date);
            }
        }
        this.certifiedDays = Collections.unmodifiableNavigableSet(days);
    }

    public static CertificationLog empty(ChallengePeriod period) {
        return new CertificationLog(period, Collections.emptyList());
    }

    public ChallengePeriod period() {
        return period;
    }

    /** 기간 중 인증한 날 수 */
    public int certifiedDayCount() {
        return certifiedDays.size();
    }

    /** 가장 길게 이어진 연속 인증 일수 */
    public int longestStreak() {
        int longest = 0;
        int current = 0;
        LocalDate previous = null;
        for (LocalDate day : certifiedDays) {
            current = (previous != null && previous.plusDays(1).equals(day)) ? current + 1 : 1;
            longest = Math.max(longest, current);
            previous = day;
        }
        return longest;
    }
}
