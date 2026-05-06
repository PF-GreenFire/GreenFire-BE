package sisosolsol.greenfire.badge.model;

import lombok.Getter;

/**
 * 도메인이 발행하는 사실(fact). 뱃지 평가의 입력.
 * action 코드는 spark_history.action 컬럼에 기록되는 값과 동기화.
 *
 * 새 뱃지 추가는 enum 한 줄로 가능. 새 outcome 도입 시:
 * 1) 여기 항목 추가
 * 2) 도메인 서비스에서 SparkService.award 호출 시 같은 action 코드 사용
 * 3) Badge enum이 그 outcome을 사용하는 entry 추가
 */
@Getter
public enum Outcome {
    POST_CREATED("POST_CREATED"),
    STORE_APPROVED("STORE_APPROVED"),
    LIKE_RECEIVED("LIKE_RECEIVED"),
    CHALLENGE_COMPLETE("CHALLENGE_COMPLETE"),
    TIER_REACHED("TIER_REACHED");

    private final String action;

    Outcome(String action) {
        this.action = action;
    }
}
