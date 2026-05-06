package sisosolsol.greenfire.badge.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserBadgeRecord {
    private String badgeCode;       // Badge enum.name()
    private LocalDateTime earnedAt;
    private boolean viewed;
}
