package sisosolsol.greenfire.notice.enums;

import lombok.Getter;

@Getter
public enum NoticeCategory {
    NOTICE("공지사항"),
    EVENT("이벤트"),
    SYSTEM("시스템");

    private final String description;

    // Enum은 명시적으로 생성자를 만들어야 함
    NoticeCategory(String description) {
        this.description = description;
    }
}