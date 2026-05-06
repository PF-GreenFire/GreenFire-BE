package sisosolsol.greenfire.badge.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AchievementSummaryDTO {
    private int totalCount;            // 보유한 뱃지 수
    private int totalDefined;          // 전체 정의된 뱃지 수
    private List<AchievementBadgeDTO> achievements;  // 모든 뱃지 (잠금 포함). 마이페이지는 unlocked만 slice해서 사용
}
