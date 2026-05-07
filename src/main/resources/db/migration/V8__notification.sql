-- V8: 알림 도메인
-- 단일 테이블 + type 분기. type/actor/resource는 자유 문자열 (도메인별 매핑은 코드).

CREATE TABLE IF NOT EXISTS notification (
    notification_code SERIAL PRIMARY KEY,
    recipient_code    UUID NOT NULL,                  -- 받는 사람
    type              VARCHAR(40) NOT NULL,           -- POST_LIKED / POST_COMMENTED / FOLLOWED / CHALLENGE_REWARDED / TIER_REACHED / BADGE_EARNED
    title             VARCHAR(200),                   -- 화면용 메시지 ("jaehyun님이 좋아합니다")
    actor_code        UUID,                           -- 액션 발생자 (시스템 알림은 NULL)
    resource_type     VARCHAR(20),                    -- POST / CHALLENGE / STORE / USER / BADGE
    resource_code     VARCHAR(50),                    -- UUID나 Integer 모두 문자열로
    is_read           BOOLEAN DEFAULT FALSE,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_code) REFERENCES users(user_code) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_noti_recipient
    ON notification(recipient_code, is_read, created_at DESC);
