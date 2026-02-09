# 🚀 Flyway DB 마이그레이션 가이드

## 📌 Flyway란?

Flyway는 데이터베이스 스키마 버전 관리 도구입니다.
- Git처럼 DB 스키마를 버전 관리
- 팀원 간 DB 동기화 자동화
- 안전한 배포 및 롤백

---

## 🎯 설정 완료 사항

### 1. 의존성 추가 (build.gradle)
```gradle
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
```

### 2. Flyway 설정 (application.yml)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    sql-migration-suffixes: .sql
    encoding: UTF-8
    validate-on-migrate: true
```

### 3. 마이그레이션 파일 위치
```
src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__add_something.sql (향후 추가)
└── V3__update_something.sql (향후 추가)
```

---

## 🔧 사용 방법

### ✅ 최초 설정 (신규 팀원)

1. **PostgreSQL 설치 확인**
```bash
psql --version
```

2. **데이터베이스 및 사용자 생성**
```bash
psql -U postgres -f setup_db.sql
```

3. **애플리케이션 실행**
```bash
./gradlew bootRun
```

**✨ 이제 Flyway가 자동으로 마이그레이션을 실행합니다!**

애플리케이션 시작 시 자동으로:
- `flyway_schema_history` 테이블 생성
- 미실행된 마이그레이션 파일 실행
- 실행 이력 기록

---

## 📝 스키마 변경 시 (새로운 마이그레이션 추가)

### 1. 마이그레이션 파일 생성

**파일명 규칙**: `V{버전}__{설명}.sql`

```bash
# 예시
V2__add_user_profile_table.sql
V3__add_post_views_column.sql
V4__create_comment_table.sql
```

**⚠️ 주의사항**
- 버전 번호는 순차적으로 증가 (V1, V2, V3...)
- 언더스코어 2개(`__`)로 버전과 설명 구분
- 한번 실행된 파일은 절대 수정하지 말 것!

### 2. 마이그레이션 파일 예시

**예시 1: 테이블 추가**
```sql
-- V2__add_comment_table.sql
CREATE TABLE tbl_comment (
    comment_code SERIAL PRIMARY KEY,
    post_code INTEGER NOT NULL,
    user_code UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_code) REFERENCES tbl_post(post_code),
    FOREIGN KEY (user_code) REFERENCES tbl_user(user_code)
);

CREATE INDEX idx_comment_post ON tbl_comment(post_code);
```

**예시 2: 컬럼 추가**
```sql
-- V3__add_user_profile_image.sql
ALTER TABLE tbl_user 
ADD COLUMN profile_image_url VARCHAR(500);

CREATE INDEX idx_user_status ON tbl_user(status);
```

**예시 3: 데이터 추가**
```sql
-- V4__insert_initial_categories.sql
INSERT INTO tbl_challenge_category (challenge_category_name, status) VALUES
('제로웨이스트', 'ACTIVE'),
('비건', 'ACTIVE'),
('친환경 소비', 'ACTIVE');

INSERT INTO tbl_store_category (store_category_name, status) VALUES
('친환경 카페', 'ACTIVE'),
('비건 식당', 'ACTIVE'),
('제로웨이스트 샵', 'ACTIVE');
```

### 3. Git에 커밋 및 푸시

```bash
git add src/main/resources/db/migration/V2__add_comment_table.sql
git commit -m "feat: 댓글 테이블 추가"
git push origin main
```

### 4. 팀원이 Pull 받은 후

```bash
git pull
./gradlew bootRun  # 또는 IDE에서 실행
```

**자동으로 새로운 마이그레이션이 실행됩니다!** 🎉

---

## 🔍 Flyway 상태 확인

### 1. 마이그레이션 이력 확인

```sql
-- PostgreSQL에 접속
psql -U greenfire_dev -d greenfire_db_dev

-- 실행된 마이그레이션 이력 조회
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

결과 예시:
```
installed_rank | version | description  | type | script                    | checksum   | installed_by  | installed_on         | execution_time | success
----------------+---------+--------------+------+---------------------------+------------+---------------+----------------------+----------------+---------
1              | 1       | init schema  | SQL  | V1__init_schema.sql       | 1234567890 | greenfire_dev | 2026-01-14 10:00:00 | 45             | true
2              | 2       | add comment  | SQL  | V2__add_comment_table.sql | 9876543210 | greenfire_dev | 2026-01-14 11:00:00 | 12             | true
```

### 2. Gradle 명령어 (선택 사항)

```bash
# 마이그레이션 정보 확인
./gradlew flywayInfo

# 마이그레이션 수동 실행
./gradlew flywayMigrate

# 마이그레이션 검증
./gradlew flywayValidate
```

---

## ⚠️ 주의사항 및 Best Practices

### 🚫 하면 안 되는 것

1. **실행된 마이그레이션 파일 수정 금지**
   - Flyway는 checksum으로 파일 변경 감지
   - 수정 시 validation error 발생
   - 새로운 마이그레이션 파일로 추가해야 함

2. **버전 번호 건너뛰기 금지**
   ```
   ❌ V1, V3, V5 (V2, V4 누락)
   ✅ V1, V2, V3, V4, V5
   ```

3. **동시에 같은 버전 번호 사용 금지**
   - 팀원 A: V5__add_column_a.sql
   - 팀원 B: V5__add_column_b.sql ❌
   - 버전 충돌 발생! 커뮤니케이션 필요

### ✅ Best Practices

1. **마이그레이션은 작고 명확하게**
   - 하나의 마이그레이션 = 하나의 목적
   - 롤백이 필요할 때 대응 쉬움

2. **마이그레이션 파일명은 명확하게**
   ```
   ✅ V2__add_comment_table.sql
   ✅ V3__add_user_profile_image_column.sql
   ❌ V2__update.sql
   ❌ V3__fix.sql
   ```

3. **Pull Request에 마이그레이션 포함**
   - 코드 변경 + 스키마 변경을 함께 리뷰
   - 팀원들이 스키마 변경 사항 인지

4. **롤백용 마이그레이션 준비**
   ```sql
   -- V10__undo_add_column.sql
   ALTER TABLE tbl_user DROP COLUMN profile_image_url;
   ```

---

## 🐛 문제 해결 (Troubleshooting)

### 1. "Validate failed: Migration checksum mismatch"

**원因**: 이미 실행된 마이그레이션 파일이 수정됨

**해결방법**:
```bash
# 방법 1: 마이그레이션 파일을 원래대로 복구

# 방법 2: baseline 재설정 (개발 환경에서만!)
./gradlew flywayRepair
```

### 2. "Schema not empty" 에러

**원인**: 기존에 수동으로 생성된 테이블이 있음

**해결방법**: `baseline-on-migrate: true` 설정이 이미 되어있으므로 자동 해결

### 3. 마이그레이션이 실행되지 않음

**확인사항**:
```yaml
spring:
  flyway:
    enabled: true  # 이 값이 true인지 확인
```

---

## 📚 참고 자료

- [Flyway 공식 문서](https://flywaydb.org/documentation/)
- [Spring Boot + Flyway 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)

---

## 🤝 팀 협업 시나리오

### 시나리오 1: 새로운 기능 추가

1. **개발자 A**가 댓글 기능 개발
   - `V5__add_comment_table.sql` 작성
   - 코드와 함께 PR 생성
   
2. **개발자 B**가 리뷰 후 Merge

3. **개발자 C, D**가 Pull
   - `git pull`
   - 애플리케이션 재시작
   - ✨ 자동으로 V5 마이그레이션 실행!

### 시나리오 2: 동시 개발 시 버전 충돌

1. **개발자 A**: V5__add_comment.sql (먼저 merge)
2. **개발자 B**: V5__add_like.sql (merge 대기 중)

**해결**:
```bash
# 개발자 B가 main pull 후
git pull origin main

# 파일명 변경
mv V5__add_like.sql V6__add_like.sql

# 커밋 수정 후 재푸시
git add .
git commit --amend
git push -f
```

---

## 🎓 요약

| 작업 | 명령어 |
|------|--------|
| 최초 DB 생성 | `psql -U postgres -f setup_db.sql` |
| 앱 실행 (자동 마이그레이션) | `./gradlew bootRun` |
| 새 마이그레이션 추가 | `V{n}__{설명}.sql` 파일 생성 |
| 이력 확인 | `SELECT * FROM flyway_schema_history;` |

**💡 핵심**: 이제 `init_db.sql`을 수동으로 실행할 필요가 없습니다!
Git pull → 앱 실행 → 자동 동기화! 🚀

---

## ❓ FAQ

**Q: 기존 init_db.sql, setup_db.sql은 어떻게 하나요?**

A: 
- `setup_db.sql`: 유지. 최초 DB/사용자 생성용으로 계속 사용
- `init_db.sql`: 참고용으로 보관. 이제 Flyway가 대신 실행함

**Q: 개발 서버/운영 서버는 어떻게 관리하나요?**

A: 
- 동일한 마이그레이션 파일 사용
- 각 환경의 `application.yml`에서 DB 접속 정보만 다르게 설정
- Flyway가 각 환경에 맞게 자동으로 마이그레이션 실행

**Q: 데이터베이스를 완전히 초기화하려면?**

A:
```bash
# PostgreSQL에서 모든 테이블 삭제
psql -U postgres -f setup_db.sql

# 애플리케이션 재시작 (Flyway가 처음부터 실행)
./gradlew bootRun
```

**Q: 팀원이 많으면 버전 충돌이 자주 발생하지 않나요?**

A: 
- PR 전에 main 브랜치 pull 받아서 최신 버전 확인
- 큰 기능은 feature 브랜치에서 개발 → merge 시 버전 번호 조정
- Slack/Discord로 "V10 사용합니다!" 공유

---

**🎉 이제 DB 동기화 걱정 없이 개발에 집중하세요!**

