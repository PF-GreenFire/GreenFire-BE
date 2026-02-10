package sisosolsol.greenfire.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.report.enums.ReportCategory;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {

    @NotNull(message = "리소스 타입은 필수입니다.")
    private ResourceType resourceType;

    @NotBlank(message = "리소스 ID는 필수입니다.")
    private String resourceId;

    @NotNull(message = "신고 유형은 필수입니다.")
    private ReportCategory category;

    @NotBlank(message = "신고 사유는 필수입니다.")
    @Size(min = 10, max = 500, message = "신고 사유는 10~500자 사이여야 합니다.")
    private String reason;
}
