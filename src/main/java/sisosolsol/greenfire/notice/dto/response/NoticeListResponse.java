package sisosolsol.greenfire.notice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sisosolsol.greenfire.notice.entity.Notice;
import sisosolsol.greenfire.notice.enums.NoticeCategory;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NoticeListResponse {

    private Integer noticeCode;
    private String noticeTitle;
    private NoticeCategory noticeCategory;
    private Boolean isImportant;
    private Integer viewCount;
    private Boolean hasImages;  // ⭐ hasAttachments → hasImages로 변경
    private Boolean isViewed;
    private LocalDateTime createdAt;

    public static NoticeListResponse from(Notice notice, Boolean isViewed, Boolean hasImages) {
        return NoticeListResponse.builder()
                .noticeCode(notice.getNoticeCode())
                .noticeTitle(notice.getNoticeTitle())
                .noticeCategory(notice.getNoticeCategory())
                .isImportant(notice.getIsImportant())
                .viewCount(notice.getViewCount())
                .hasImages(hasImages)  // ⭐ 파라미터로 받음
                .isViewed(isViewed)
                .createdAt(notice.getCreatedAt())
                .build();
    }
}