-- V2: 피드 좋아요 + 댓글 도메인
-- application.yml의 spring.flyway.enabled가 false이므로, 로컬 DB에는 psql로 직접 실행 필요:
--   psql -U <user> -d <db> -f V2__post_like_post_comment.sql
-- (운영 DB도 동일하게 한 번 적용한 뒤 flyway를 켤 때 baseline 처리할 것)

-- 피드 좋아요 (post_code + user_code 복합 PK로 1인 1 좋아요 보장)
CREATE TABLE IF NOT EXISTS post_like (
    post_code INTEGER NOT NULL,
    user_code UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_code, user_code),
    FOREIGN KEY (post_code) REFERENCES post(post_code) ON DELETE CASCADE,
    FOREIGN KEY (user_code) REFERENCES users(user_code) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_post_like_user ON post_like(user_code);

-- 피드 댓글
CREATE TABLE IF NOT EXISTS post_comment (
    comment_code SERIAL PRIMARY KEY,
    post_code INTEGER NOT NULL,
    user_code UUID NOT NULL,
    comment_content TEXT NOT NULL,
    comment_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_code) REFERENCES post(post_code) ON DELETE CASCADE,
    FOREIGN KEY (user_code) REFERENCES users(user_code) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_comment_post ON post_comment(post_code, comment_status, created_at DESC);
