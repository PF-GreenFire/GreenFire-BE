package sisosolsol.greenfire.notice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sisosolsol.greenfire.image.model.dto.ImageDTO;
import sisosolsol.greenfire.notice.entity.Notice;
import sisosolsol.greenfire.notice.enums.NoticeCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class NoticeDetailResponse {

    private Integer noticeCode;
    private String noticeTitle;
    private String noticeContent;
    private NoticeCategory noticeCategory;
    private Boolean isImportant;
    private Integer viewCount;
    private UUID authorUserCode;
    private String authorName;
    private String thumbnailUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageDTO> images;
    private Boolean isViewedByCurrentUser;
    private SimplifiedNotice prevNotice;
    private SimplifiedNotice nextNotice;

    public static NoticeDetailResponse from(Notice notice, String authorName, Boolean isViewedByCurrentUser,
                                            List<ImageDTO> images, Notice prevNotice, Notice nextNotice) {
        return NoticeDetailResponse.builder()
                .noticeCode(notice.getNoticeCode())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticeCategory(notice.getNoticeCategory())
                .isImportant(notice.getIsImportant())
                .viewCount(notice.getViewCount())
                .authorUserCode(notice.getAuthorUserCode())
                .authorName(authorName)
                .thumbnailUrl(notice.getThumbnailUrl())
                .startDate(notice.getStartDate())
                .endDate(notice.getEndDate())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .images(images)  // ⭐ ImageDTO 리스트
                .isViewedByCurrentUser(isViewedByCurrentUser)
                .prevNotice(prevNotice != null ? SimplifiedNotice.from(prevNotice) : null)
                .nextNotice(nextNotice != null ? SimplifiedNotice.from(nextNotice) : null)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SimplifiedNotice {
        private Integer noticeCode;
        private String noticeTitle;

        public static SimplifiedNotice from(Notice notice) {
            return SimplifiedNotice.builder()
                    .noticeCode(notice.getNoticeCode())
                    .noticeTitle(notice.getNoticeTitle())
                    .build();
        }
    }
}