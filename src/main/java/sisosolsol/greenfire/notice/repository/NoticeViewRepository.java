package sisosolsol.greenfire.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sisosolsol.greenfire.notice.entity.NoticeView;

import java.util.UUID;

public interface NoticeViewRepository extends JpaRepository<NoticeView, Integer> {

    // 사용자가 해당 공지사항을 조회했는지 확인
    boolean existsByNotice_NoticeCodeAndUserCode(Integer noticeCode, UUID userCode);

    // 사용자의 조회 기록 삭제 (테스트용)
    void deleteByNotice_NoticeCodeAndUserCode(Integer noticeCode, UUID userCode);
}