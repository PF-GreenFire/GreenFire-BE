package sisosolsol.greenfire.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tbl_notice_view",
        uniqueConstraints = @UniqueConstraint(columnNames = {"notice_code", "user_code"}))
public class NoticeView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_code")
    private Integer viewCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_code", nullable = false)
    private Notice notice;

    @Column(name = "user_code", nullable = false, columnDefinition = "uuid")
    private UUID userCode;

    @CreationTimestamp
    @Column(name = "viewed_at", updatable = false)
    private LocalDateTime viewedAt;

    @Builder
    public NoticeView(Notice notice, UUID userCode) {
        this.notice = notice;
        this.userCode = userCode;
    }
}