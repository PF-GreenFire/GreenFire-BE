-- V3: store_like 테이블 신설 + image 테이블에 reference_type/reference_code 컬럼 추가
-- 로컬 적용: psql -U <user> -d <db> -f V3__store_like_image_reference.sql

-- 1. 매장 좋아요
CREATE TABLE IF NOT EXISTS store_like (
    store_code INTEGER NOT NULL,
    user_code  UUID    NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (store_code, user_code),
    FOREIGN KEY (store_code) REFERENCES store(store_code) ON DELETE CASCADE,
    FOREIGN KEY (user_code)  REFERENCES users(user_code)  ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_store_like_user ON store_like(user_code);

-- 2. image 테이블 다형성 컬럼 추가 (post_code/store_code FK는 호환을 위해 그대로 둠)
ALTER TABLE image ADD COLUMN IF NOT EXISTS reference_type VARCHAR(20);
ALTER TABLE image ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_image_reference ON image(reference_type, reference_code);
