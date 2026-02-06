package sisosolsol.greenfire.report.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sisosolsol.greenfire.report.enums.ReportStatus;

@Getter
@NoArgsConstructor
public class ReportHandleRequest {

    @NotNull(message = "처리 상태는 필수입니다.")
    private ReportStatus status;

    @Size(max = 500, message = "관리자 메모는 500자 이내여야 합니다.")
    private String adminNote;
}
