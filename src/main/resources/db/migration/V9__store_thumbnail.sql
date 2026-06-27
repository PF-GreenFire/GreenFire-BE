-- store 테이블에 썸네일 URL 컬럼 추가
-- 메인 페이지 등에서 가게 카드 이미지를 빠르게 렌더링하기 위한 단일 URL.
-- 상세 페이지의 image 리스트와는 별개로 운영한다.
ALTER TABLE store
    ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(500);
