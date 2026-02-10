package sisosolsol.greenfire.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sisosolsol.greenfire.notice.entity.NoticeView;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NoticeViewRepository extends JpaRepository<NoticeView, Long> {

    /**
     * 로그인 사용자 - 해당 공지사항을 조회했는지 확인 (전체 기간)
     */
    boolean existsByNotice_NoticeCodeAndUserCode(Integer noticeCode, UUID userCode);

    /**
     * 로그인 사용자 - 특정 시간 이후 조회 기록 존재 여부 (24시간 중복 방지용)
     */
    @Query("SELECT COUNT(v) > 0 FROM NoticeView v " +
            "WHERE v.notice.noticeCode = :noticeCode " +
            "AND v.userCode = :userCode " +
            "AND v.viewedAt >= :since")
    boolean existsByNoticeCodeAndUserCodeSince(
            @Param("noticeCode") Integer noticeCode,
            @Param("userCode") UUID userCode,
            @Param("since") LocalDateTime since);

    /**
     * 비로그인 사용자 - IP 기반 특정 시간 이후 조회 기록 존재 여부 (24시간 중복 방지용)
     */
    @Query("SELECT COUNT(v) > 0 FROM NoticeView v " +
            "WHERE v.notice.noticeCode = :noticeCode " +
            "AND v.ipAddress = :ipAddress " +
            "AND v.userCode IS NULL " +
            "AND v.viewedAt >= :since")
    boolean existsByNoticeCodeAndIpAddressSince(
            @Param("noticeCode") Integer noticeCode,
            @Param("ipAddress") String ipAddress,
            @Param("since") LocalDateTime since);

    /**
     * 특정 공지사항의 조회수 카운트
     */
    long countByNotice_NoticeCode(Integer noticeCode);

    /**
     * 사용자의 조회 기록 삭제 (테스트용)
     */
    void deleteByNotice_NoticeCodeAndUserCode(Integer noticeCode, UUID userCode);
}
