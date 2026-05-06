-- V6: 사용자 뱃지 보유 테이블
-- 뱃지 정의는 코드(Badge enum)에 박혀 있어 별도 테이블 없음.

CREATE TABLE IF NOT EXISTS user_badge (
    user_code  UUID NOT NULL,
    badge_code VARCHAR(50) NOT NULL,    -- Badge enum.name() 그대로
    earned_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_viewed  BOOLEAN DEFAULT FALSE,    -- NEW 뱃지 표시 끄기용
    PRIMARY KEY (user_code, badge_code),
    FOREIGN KEY (user_code) REFERENCES users(user_code) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_user_badge_user ON user_badge(user_code);
