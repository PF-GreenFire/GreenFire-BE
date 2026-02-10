package sisosolsol.greenfire.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sisosolsol.greenfire.notice.enums.NoticeCategory;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String noticeTitle;

    @NotBlank(message = "내용은 필수입니다.")
    private String noticeContent;

    private NoticeCategory noticeCategory;

    private Boolean isImportant;

    private String thumbnailUrl;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}