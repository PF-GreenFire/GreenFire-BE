-- 비밀번호 재설정 코드 입력 실패 횟수 (5회 초과 시 코드 무효화)
-- password_reset_codes는 JPA 엔티티 테이블이라 신규 DB에서는 아직 없을 수 있음 → IF EXISTS
ALTER TABLE IF EXISTS password_reset_codes
    ADD COLUMN IF NOT EXISTS attempt_count INTEGER NOT NULL DEFAULT 0;
