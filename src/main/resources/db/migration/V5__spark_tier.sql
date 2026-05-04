-- V5: 초록불씨 + 멸종위기동물 9단계 등급제
-- 로컬 적용: psql -U <user> -d <db> -f V5__spark_tier.sql
-- 데이터 모델 정리(challenge.xp -> challenge.spark_reward 등)는 후속 단계.

-- 1. users 누적 점수
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_spark INTEGER NOT NULL DEFAULT 0;

-- 2. 적립 이력 (어뷰즈 방어 + 통계용)
CREATE TABLE IF NOT EXISTS spark_history (
    history_code SERIAL PRIMARY KEY,
    user_code    UUID NOT NULL,
    action       VARCHAR(40) NOT NULL,   -- POST_CREATE / STORE_APPROVED / LIKE_RECEIVED / CHALLENGE_COMPLETE / DAILY_FIRST 등
    amount       INTEGER NOT NULL,
    source_type  VARCHAR(20),             -- POST / STORE / CHALLENGE 등 (NULL 허용)
    source_code  INTEGER,                 -- 해당 도메인의 PK (NULL 허용)
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_code) REFERENCES users(user_code) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_spark_history_user_created ON spark_history(user_code, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_spark_history_source ON spark_history(source_type, source_code, action);
