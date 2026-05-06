-- V7: 팔로우 관계 테이블
-- BE 코드(UserMapper.xml, UserController.followUser/unfollowUser)는 이미 user_follows 테이블을 가정하지만
-- 어떤 V도 이 테이블을 만들지 않아 호출 시 SQL 에러가 발생하던 상태를 해소.

CREATE TABLE IF NOT EXISTS user_follows (
    user_code   UUID NOT NULL,                 -- 팔로우를 한 사람
    target_user UUID NOT NULL,                 -- 팔로우 대상
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_code, target_user),
    FOREIGN KEY (user_code)   REFERENCES users(user_code) ON DELETE CASCADE,
    FOREIGN KEY (target_user) REFERENCES users(user_code) ON DELETE CASCADE,
    CHECK (user_code <> target_user)
);
CREATE INDEX IF NOT EXISTS idx_user_follows_target ON user_follows(target_user);
