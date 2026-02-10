package sisosolsol.greenfire.report.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {
    PENDING("처리 대기"),
    HANDLED_DELETED("처리완료-삭제"),
    HANDLED_SUSPENDED("처리완료-정지"),
    DISMISSED("기각");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }
}
