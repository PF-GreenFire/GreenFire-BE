package sisosolsol.greenfire.badge.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 마이페이지 / 뱃지 컬렉션 화면용 응답 항목.
 * Badge enum + user_badge를 합쳐서 빌드.
 */
@Getter
@Builder
public class AchievementBadgeDTO {
    private String id;          // Badge enum.name() — FE의 key
    private String name;        // 한국어 라벨
    private String category;
    private String description;
    private String image;       // 이모지 (추후 일러스트 path로 교체 시 그대로 호환)
    private boolean unlocked;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime unlockedDate;

    private boolean isNew;       // unlocked && !isViewed
    private boolean isViewed;
}
