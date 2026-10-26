-- 관리자 추천(featured) 피드 토글용 컬럼
-- 메인/피드 상단 추천 영역은 featured 우선 → 최신순으로 채운다.
ALTER TABLE post
    ADD COLUMN IF NOT EXISTS featured BOOLEAN NOT NULL DEFAULT FALSE;
