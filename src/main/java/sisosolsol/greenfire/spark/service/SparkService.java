package sisosolsol.greenfire.spark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.common.enums.spark.Tier;
import sisosolsol.greenfire.spark.model.dao.SparkMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SparkService {

    private final SparkMapper sparkMapper;

    public static final int LIKE_PER_REWARD = 10;   // 좋아요 N개 단위로 +1
    public static final int LIKE_REWARD_CAP = 10;   // 게시물당 LIKE_RECEIVED 적립 횟수 상한

    /**
     * 점수 적립의 단일 진입점.
     *
     * @param userCode    적립 대상
     * @param amount      적립량 (양수)
     * @param action      POST_CREATE / STORE_APPROVED / LIKE_RECEIVED / CHALLENGE_COMPLETE / DAILY_FIRST 등
     * @param sourceType  POST / STORE / CHALLENGE 등 (NULL 가능)
     * @param sourceCode  해당 도메인 PK (NULL 가능)
     * @return            갱신된 누적 spark
     */
    @Transactional
    public int award(UUID userCode, int amount, String action, String sourceType, Integer sourceCode) {
        if (userCode == null || amount <= 0) return 0;
        try {
            int total = sparkMapper.addSpark(userCode, amount);
            sparkMapper.insertHistory(userCode, action, amount, sourceType, sourceCode);
            return total;
        } catch (Exception e) {
            // 보상 적립 실패는 본 트랜잭션을 깨지 않게 로그만 남김 (호출처에서 propagation 결정 가능)
            log.warn("spark award failed: user={} action={} amount={} ({})",
                    userCode, action, amount, e.getMessage());
            return 0;
        }
    }

    public int getTotalSpark(UUID userCode) {
        if (userCode == null) return 0;
        return sparkMapper.getTotalSpark(userCode);
    }

    public Tier getTier(UUID userCode) {
        return Tier.of(getTotalSpark(userCode));
    }

    /** 좋아요 수신에 의한 적립이 게시물당 캡 도달했는지 */
    public boolean isLikeRewardCapped(UUID userCode, int postCode) {
        return sparkMapper.countHistory(userCode, "LIKE_RECEIVED", "POST", postCode) >= LIKE_REWARD_CAP;
    }
}
