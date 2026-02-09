package sisosolsol.greenfire.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sisosolsol.greenfire.notice.entity.Notice;
import sisosolsol.greenfire.notice.enums.NoticeCategory;
import sisosolsol.greenfire.notice.enums.NoticeStatus;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {

    // 공지사항 목록 조회 (페이징, 카테고리 필터, 검색)
    @Query("SELECT n FROM Notice n WHERE n.noticeStatus = :status " +
            "AND (:category IS NULL OR n.noticeCategory = :category) " +
            "AND (:keyword IS NULL OR n.noticeTitle LIKE %:keyword% OR n.noticeContent LIKE %:keyword%) " +
            "ORDER BY n.isImportant DESC, n.createdAt DESC")
    Page<Notice> findNoticesWithFilters(
            @Param("status") NoticeStatus status,
            @Param("category") NoticeCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 공지사항 상세 조회 (이미지는 MyBatis로 별도 조회)
    Optional<Notice> findByNoticeCodeAndNoticeStatus(Integer noticeCode, NoticeStatus status);

    // 관련 공지사항 (같은 카테고리의 최신 공지)
    @Query("SELECT n FROM Notice n WHERE n.noticeStatus = :status " +
            "AND n.noticeCategory = :category " +
            "AND n.noticeCode != :excludeCode " +
            "ORDER BY n.createdAt DESC")
    List<Notice> findRelatedNotices(
            @Param("status") NoticeStatus status,
            @Param("category") NoticeCategory category,
            @Param("excludeCode") Integer excludeCode,
            Pageable pageable
    );

    // 최신 중요 공지사항 (미리보기용)
    Optional<Notice> findFirstByNoticeStatusAndIsImportantOrderByCreatedAtDesc(
            NoticeStatus status,
            Boolean isImportant
    );

    // 이전 공지사항
    @Query("SELECT n FROM Notice n WHERE n.noticeStatus = :status " +
            "AND n.noticeCategory = :category " +
            "AND n.createdAt < :createdAt " +
            "ORDER BY n.createdAt DESC, n.noticeCode DESC")
    List<Notice> findPreviousNotice(
            @Param("status") NoticeStatus status,
            @Param("category") NoticeCategory category,
            @Param("createdAt") java.time.LocalDateTime createdAt,
            Pageable pageable
    );

    // 다음 공지사항
    @Query("SELECT n FROM Notice n WHERE n.noticeStatus = :status " +
            "AND n.noticeCategory = :category " +
            "AND n.createdAt > :createdAt " +
            "ORDER BY n.createdAt ASC, n.noticeCode ASC")
    List<Notice> findNextNotice(
            @Param("status") NoticeStatus status,
            @Param("category") NoticeCategory category,
            @Param("createdAt") java.time.LocalDateTime createdAt,
            Pageable pageable
    );

    long countByNoticeStatus(NoticeStatus status);
}