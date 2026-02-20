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
@Table(name = "notice_view",
        indexes = {
                @Index(name = "idx_notice_view_notice_user", columnList = "notice_code, user_code"),
                @Index(name = "idx_notice_view_notice_ip", columnList = "notice_code, ip_address"),
                @Index(name = "idx_notice_view_viewed_at", columnList = "viewed_at")
        })
public class NoticeView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_code")
    private Long viewCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_code", nullable = false)
    private Notice notice;

    @Column(name = "user_code", columnDefinition = "uuid")
    private UUID userCode;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "viewed_at", updatable = false)
    private LocalDateTime viewedAt;

    @Builder
    public NoticeView(Notice notice, UUID userCode, String ipAddress) {
        this.notice = notice;
        this.userCode = userCode;
        this.ipAddress = ipAddress;
    }

    /**
     * 로그인 사용자 조회 기록 생성
     */
    public static NoticeView ofUser(Notice notice, UUID userCode, String ipAddress) {
        return NoticeView.builder()
                .notice(notice)
                .userCode(userCode)
                .ipAddress(ipAddress)
                .build();
    }

    /**
     * 비로그인 사용자 조회 기록 생성 (IP 기반)
     */
    public static NoticeView ofGuest(Notice notice, String ipAddress) {
        return NoticeView.builder()
                .notice(notice)
                .ipAddress(ipAddress)
                .build();
    }
}
