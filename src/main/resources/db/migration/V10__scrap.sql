-- 스크랩 (북마크) 테이블
-- 사용자가 다른 도메인(STORE, CHALLENGE, POST, USER)을 북마크.
-- 같은 사용자가 같은 대상을 두 번 스크랩하지 못하도록 unique 제약.
CREATE TABLE IF NOT EXISTS scrap (
    scrap_code   SERIAL PRIMARY KEY,
    user_code    UUID         NOT NULL,
    target_type  VARCHAR(20)  NOT NULL,
    target_code  VARCHAR(64)  NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_code, target_type, target_code),
    FOREIGN KEY (user_code) REFERENCES users(user_code) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_scrap_user_type
    ON scrap (user_code, target_type);
