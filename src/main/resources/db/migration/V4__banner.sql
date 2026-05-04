-- V4: 배너 도메인
-- 로컬 적용: psql -U <user> -d <db> -f V4__banner.sql

CREATE TABLE IF NOT EXISTS banner (
    banner_code   SERIAL PRIMARY KEY,
    banner_title  VARCHAR(200) NOT NULL,
    link_url      VARCHAR(500),
    image_url     VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_banner_active_order ON banner(is_active, display_order);
