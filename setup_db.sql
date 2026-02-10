-- ============================================
-- GreenFire Database Setup Script
-- PostgreSQL 데이터베이스 및 사용자 생성 스크립트
-- ============================================
-- 
-- 실행 방법:
-- 1. PostgreSQL 슈퍼유저(postgres)로 실행
--    psql -U postgres -f setup_db.sql
--
-- 2. 또는 psql에 접속 후 실행
--    psql -U postgres
--    \i setup_db.sql
-- ============================================

-- 기존 연결 끊기 (데이터베이스 삭제를 위해)
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'greenfire_db_dev' AND pid <> pg_backend_pid();

-- 기존 데이터베이스 삭제 (재설정 시)
DROP DATABASE IF EXISTS greenfire_db;

-- 기존 사용자 삭제 (재설정 시)
DROP USER IF EXISTS greenfire_dev;

-- 새 사용자 생성
CREATE USER greenfire_dev WITH PASSWORD 'dev1234!';

-- 새 데이터베이스 생성
CREATE DATABASE greenfire_db_dev
    WITH 
    OWNER = greenfire_dev
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- greenfire_db에 연결
\c greenfire_db_dev

-- 스키마 권한 부여
GRANT ALL ON SCHEMA public TO greenfire_dev;

-- 완료 메시지
\echo '===================================='
\echo 'GreenFire 데이터베이스 설정 완료!'
\echo '===================================='
\echo '데이터베이스: greenfire_db_dev'
\echo '사용자: greenfire_dev'
\echo '비밀번호: dev1234!'
\echo ''
\echo '다음 단계: init_db.sql 실행'
\echo 'psql -U greenfire_dev -d greenfire_db_dev -f init_db.sql'
\echo '===================================='
