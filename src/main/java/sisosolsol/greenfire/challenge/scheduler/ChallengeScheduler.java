package sisosolsol.greenfire.challenge.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sisosolsol.greenfire.challenge.service.ChallengeService;

/**
 * 매일 새벽 1시 5분에 챌린지 상태 전이 + 보상 일괄 처리.
 * - 멱등(매퍼 SQL이 status 가드 + spark_history 중복 체크)이라 재실행해도 안전.
 * - 단일 인스턴스 가정. 멀티 인스턴스에선 분산 락 필요(후속).
 * - 즉시 실행 필요 시 어드민 endpoint 사용 (POST /api/admin/challenges/run-transitions).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private final ChallengeService challengeService;

    @Scheduled(cron = "0 5 1 * * *", zone = "Asia/Seoul")
    public void dailyTransition() {
        log.info("daily challenge transition started");
        challengeService.runStatusTransitions();
    }
}
