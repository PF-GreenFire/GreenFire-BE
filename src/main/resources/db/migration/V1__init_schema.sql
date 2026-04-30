-- ============================================
-- GreenFire Database Schema
-- Version 1: Initial Schema Setup
-- ============================================

-- 1. (구) user 테이블은 제거. JPA UserAccount entity가 만드는 "users" 테이블로 통합.
--    MyBatis 매퍼들은 모두 users(user_code)를 참조한다.

-- 2. area (지역)
CREATE TABLE IF NOT EXISTS area (
    area_code SERIAL PRIMARY KEY,
    area_name VARCHAR(100) NOT NULL
);

-- 3. location (위치)
CREATE TABLE IF NOT EXISTS location (
    location_code SERIAL PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    area_code INTEGER,
    FOREIGN KEY (area_code) REFERENCES area(area_code)
);

-- 4. challenge_category (챌린지 카테고리)
CREATE TABLE IF NOT EXISTS challenge_category (
    challenge_category_code SERIAL PRIMARY KEY,
    challenge_category_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- 5. store_category (가게 카테고리)
CREATE TABLE IF NOT EXISTS store_category (
    store_category_code SERIAL PRIMARY KEY,
    store_category_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- 6. challenge (챌린지)
CREATE TABLE IF NOT EXISTS challenge (
    challenge_code INTEGER PRIMARY KEY,
    challenge_title VARCHAR(200) NOT NULL,
    challenge_content TEXT NOT NULL,
    recruitment_num INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    host_user UUID NOT NULL,
    xp INTEGER DEFAULT 0,
    thumbnail_url VARCHAR(500),
    challenge_category_code INTEGER,
    challenge_status VARCHAR(20) DEFAULT 'RECRUITING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (host_user) REFERENCES users(user_code),
    FOREIGN KEY (challenge_category_code) REFERENCES challenge_category(challenge_category_code)
);

-- 7. challenge_part (챌린지 참여)
CREATE TABLE IF NOT EXISTS challenge_part (
    challenge_part_code SERIAL PRIMARY KEY,
    challenge_code INTEGER NOT NULL,
    user_code UUID NOT NULL,
    is_host BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (challenge_code) REFERENCES challenge(challenge_code),
    FOREIGN KEY (user_code) REFERENCES users(user_code)
);

-- 8. store (가게)
CREATE TABLE IF NOT EXISTS store (
    store_code SERIAL PRIMARY KEY,
    store_name VARCHAR(200) NOT NULL,
    store_status VARCHAR(20) DEFAULT 'WAITING',
    store_phone VARCHAR(20),
    store_link VARCHAR(500),
    store_business_hours VARCHAR(200),
    location_code INTEGER,
    store_category_code INTEGER,
    store_food_type VARCHAR(50),
    detail_address VARCHAR(255),
    description TEXT,
    store_breaktime_hours VARCHAR(200),
    user_code UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (location_code) REFERENCES location(location_code),
    FOREIGN KEY (store_category_code) REFERENCES store_category(store_category_code),
    FOREIGN KEY (user_code) REFERENCES users(user_code)
);

-- 9. post (게시글)
CREATE TABLE IF NOT EXISTS post (
    post_code SERIAL PRIMARY KEY,
    user_code UUID NOT NULL,
    post_content TEXT NOT NULL,
    post_type VARCHAR(20) DEFAULT 'DEFAULT',
    store_code INTEGER,
    challenge_code INTEGER,
    thumbnail VARCHAR(500),
    "like" INTEGER DEFAULT 0,
    post_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_code) REFERENCES users(user_code),
    FOREIGN KEY (store_code) REFERENCES store(store_code),
    FOREIGN KEY (challenge_code) REFERENCES challenge(challenge_code)
);

-- 10. image (이미지)
CREATE TABLE IF NOT EXISTS image (
    image_code SERIAL PRIMARY KEY,
    post_code INTEGER,
    store_code INTEGER,
    path VARCHAR(500) NOT NULL,
    origin_name VARCHAR(255),
    file_name VARCHAR(255) NOT NULL,
    "order" INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_code) REFERENCES post(post_code),
    FOREIGN KEY (store_code) REFERENCES store(store_code)
);

-- ============================================
-- Indexes
-- ============================================

-- 챌린지 목록 조회 시 사용 (ORDER BY created_at DESC)
CREATE INDEX IF NOT EXISTS idx_challenge_created_at ON challenge(created_at DESC);

-- 챌린지 카테고리 필터링 시 사용
CREATE INDEX IF NOT EXISTS idx_challenge_category ON challenge(challenge_category_code);

-- 챌린지 참여 확인 (challenge_code, user_code 복합 조회)
CREATE INDEX IF NOT EXISTS idx_challenge_part_composite ON challenge_part(challenge_code, user_code);

-- 챌린지 참여자 수 카운트 (challenge_code로 그룹핑)
CREATE INDEX IF NOT EXISTS idx_challenge_part_challenge ON challenge_part(challenge_code);

-- 게시글 목록 조회 (post_status 필터링 + created_at 정렬)
CREATE INDEX IF NOT EXISTS idx_post_status_created ON post(post_status, created_at DESC);

-- 챌린지별 게시글 조회 (challenge_code 필터링)
CREATE INDEX IF NOT EXISTS idx_post_challenge ON post(challenge_code);

-- 가게 상태별 조회 (store_status 필터링 + created_at 정렬)
CREATE INDEX IF NOT EXISTS idx_store_status_created ON store(store_status, created_at DESC);

-- 사용자별 신청한 가게 목록 조회
CREATE INDEX IF NOT EXISTS idx_store_user ON store(user_code);

-- (사용자 이메일 인덱스는 users 테이블의 자체 unique constraint로 대체)

