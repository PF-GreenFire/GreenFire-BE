package sisosolsol.greenfire.report.enums;

import lombok.Getter;

@Getter
public enum ReportCategory {
    SPAM("스팸"),
    INAPPROPRIATE("부적절한 콘텐츠"),
    HARASSMENT("괴롭힘/혐오"),
    OTHER("기타");

    private final String description;

    ReportCategory(String description) {
        this.description = description;
    }
}
