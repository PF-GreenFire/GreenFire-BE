package sisosolsol.greenfire.notice.enums;

import lombok.Getter;

@Getter
public enum NoticeStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    DELETED("삭제됨");

    private final String description;

    // Enum은 명시적으로 생성자를 만들어야 함
    NoticeStatus(String description) {
        this.description = description;
    }
}