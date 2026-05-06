package sisosolsol.greenfire.badge.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.badge.model.dto.UserBadgeRecord;

import java.util.List;
import java.util.UUID;

@Mapper
public interface BadgeMapper {

    /** 사용자가 보유한 모든 뱃지 코드 (Set 변환 용도) */
    List<String> findOwnedCodes(@Param("userCode") UUID userCode);

    /** 사용자 보유 뱃지 목록 (earned_at, is_viewed 포함) */
    List<UserBadgeRecord> findUserBadges(@Param("userCode") UUID userCode);

    /** 뱃지 부여 — ON CONFLICT DO NOTHING으로 멱등 */
    void insert(@Param("userCode") UUID userCode,
                @Param("badgeCode") String badgeCode);

    /** NEW 표시 끄기 */
    void markViewed(@Param("userCode") UUID userCode,
                    @Param("badgeCode") String badgeCode);
}
