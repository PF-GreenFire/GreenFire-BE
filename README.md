# GreenFire Backend

친환경 활동 및 챌린지 플랫폼 백엔드 서비스

## 기술 스택

- Java 17
- Spring Boot 3.3.4
- PostgreSQL
- MyBatis
- Spring Security + JWT
- Flyway (DB 마이그레이션)

## 데이터베이스 설정

### 1. PostgreSQL 설치 및 실행

```bash
# macOS
brew install postgresql@15
brew services start postgresql@15

# Ubuntu/Debian
sudo apt update
sudo apt install postgresql-15
sudo systemctl start postgresql

# Windows
# PostgreSQL 공식 사이트에서 설치 프로그램 다운로드
```

### 2. 데이터베이스 및 사용자 생성

PostgreSQL 슈퍼유저로 setup_db.sql 실행:

```bash
psql -U postgres -f setup_db.sql
```

이 스크립트는 다음을 수행합니다:
- 데이터베이스: `greenfire_db_dev` 생성
- 사용자: `greenfire_dev` 생성
- 비밀번호: `dev1234!`

### 3. 설정 파일 생성

```bash
cp application.yml.template src/main/resources/application.yml
```

`application.yml` 파일을 열어서 DB 정보를 입력하세요:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/greenfire_db_dev
    username: greenfire_dev
    password: dev1234!  # setup_db.sql에서 설정한 비밀번호
```

### 4. 실행

```bash
./gradlew bootRun
```

**✨ 애플리케이션 시작 시 Flyway가 자동으로 데이터베이스 스키마를 생성합니다!**

- 더 이상 `init_db.sql`을 수동으로 실행할 필요가 없습니다
- 팀원이 추가한 스키마 변경사항이 자동으로 적용됩니다
- Git pull 후 앱 재시작만 하면 DB가 자동 동기화됩니다

## 📚 DB 마이그레이션 (Flyway)

**중요**: DB 스키마 변경 및 팀 협업에 대한 자세한 내용은 [FLYWAY_GUIDE.md](./FLYWAY_GUIDE.md)를 참고하세요!

### 빠른 시작

1. **신규 팀원**: `setup_db.sql` 실행 → 앱 실행 → 자동 마이그레이션 ✨
2. **스키마 변경**: `src/main/resources/db/migration/V{n}__{설명}.sql` 파일 추가
3. **동기화**: Git pull → 앱 재시작 → 자동 적용

### 마이그레이션 이력 확인

```bash
psql -U greenfire_dev -d greenfire_db_dev
SELECT * FROM flyway_schema_history;
```

### 데이터베이스 재설정 (필요 시)

```bash
# 데이터베이스 및 사용자 재생성
psql -U postgres -f setup_db.sql

# 애플리케이션 재시작 (Flyway가 자동으로 마이그레이션 실행)
./gradlew bootRun
```

## 프로젝트 구조

```
src/main/java/sisosolsol/greenfire/
├── category/      # 카테고리
├── challenge/     # 챌린지
├── post/          # 게시글
├── store/         # 가게
├── user/          # 사용자
├── image/         # 이미지
├── location/      # 위치
└── common/        # 공통 기능
```

## 테이블 구조

- `tbl_user` - 사용자
- `tbl_challenge` - 챌린지
- `tbl_challenge_category` - 챌린지 카테고리
- `tbl_challenge_part` - 챌린지 참여
- `tbl_store` - 가게
- `tbl_store_category` - 가게 카테고리
- `tbl_post` - 게시글
- `tbl_image` - 이미지
- `tbl_location` - 위치
- `tbl_area` - 지역