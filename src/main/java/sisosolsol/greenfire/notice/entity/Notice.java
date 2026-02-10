package sisosolsol.greenfire.notice.entity;

import jakarta.persistence.*;
import lombok.*;
import sisosolsol.greenfire.notice.enums.NoticeCategory;
import sisosolsol.greenfire.notice.enums.NoticeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_code")
    private Integer noticeCode;

    @Column(name = "notice_title", nullable = false, length = 200)
    private String noticeTitle;

    @Column(name = "notice_content", nullable = false, columnDefinition = "TEXT")
    private String noticeContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_category", length = 20)
    @Builder.Default
    private NoticeCategory noticeCategory = NoticeCategory.NOTICE;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_status", length = 20)
    @Builder.Default
    private NoticeStatus noticeStatus = NoticeStatus.ACTIVE;

    @Column(name = "is_important")
    @Builder.Default
    private Boolean isImportant = false;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "author_user_code")
    private UUID authorUserCode;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 비즈니스 메서드 =====

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updateNotice(String noticeTitle, String noticeContent, NoticeCategory noticeCategory,
                             Boolean isImportant, String thumbnailUrl, LocalDateTime startDate, LocalDateTime endDate) {
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.noticeCategory = noticeCategory;
        this.isImportant = isImportant;
        this.thumbnailUrl = thumbnailUrl;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void delete() {
        this.noticeStatus = NoticeStatus.DELETED;
    }
}