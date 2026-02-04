package sisosolsol.greenfire.common.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sisosolsol.greenfire.common.audit.entity.ActionType;
import sisosolsol.greenfire.common.audit.entity.ActivityLog;
import sisosolsol.greenfire.common.audit.entity.ResourceType;
import sisosolsol.greenfire.common.audit.repository.ActivityLogRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    /**
     * 활동 로그 비동기 저장.
     * <p>
     * 새 모듈에서 사용 예시:
     * <pre>
     * // 피드 게시글 작성
     * activityLogService.log(userId, CREATE, POST, postId.toString(), postBody, ip);
     *
     * // 게시글 조회 (인기도 집계용, content 불필요)
     * activityLogService.log(userId, VIEW, POST, postId.toString(), null, ip);
     *
     * // 댓글 작성 (신고 증거 보존)
     * activityLogService.log(userId, CREATE, COMMENT, commentId.toString(), commentBody, ip);
     *
     * // 챌린지 참여
     * activityLogService.log(userId, JOIN, CHALLENGE, challengeId.toString(), null, ip);
     *
     * // 유저 팔로우
     * activityLogService.log(userId, FOLLOW, USER, targetUserId.toString(), null, ip);
     * </pre>
     *
     * @param userId       행위자 (비로그인 시 null)
     * @param actionType   행위 (CREATE, VIEW, LIKE, LOGIN ...)
     * @param resourceType 대상 종류 (POST, COMMENT, CHALLENGE, AUTH ...)
     * @param resourceId   대상 PK 문자열 (인증 이벤트는 null)
     * @param content      행위 시점의 원문 스냅샷 (삭제/수정되어도 증거 보존, 선택)
     * @param ipAddress    클라이언트 IP
     */
    @Async
    public void log(UUID userId, ActionType actionType, ResourceType resourceType,
                    String resourceId, String content, String ipAddress) {
        try {
            activityLogRepository.save(
                    new ActivityLog(userId, actionType, resourceType, resourceId, content, ipAddress)
            );
        } catch (Exception e) {
            log.warn("활동 로그 저장 실패: action={}, resource={}/{}, userId={}",
                    actionType, resourceType, resourceId, userId, e);
        }
    }
}
