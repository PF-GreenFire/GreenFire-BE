# GreenFire Backend

친환경 활동 및 챌린지 플랫폼 백엔드 서비스

## 기술 스택

- Java 17
- Spring Boot 3.3.4
- PostgreSQL
- MyBatis
- Spring Security + JWT

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

### 3. 테이블 및 초기 데이터 생성

생성된 greenfire_dev 사용자로 init_db.sql 실행:

```bash
psql -U greenfire_dev -d greenfire_db_dev -f init_db.sql
```

이 스크립트는 다음을 수행합니다:
- 모든 테이블 생성
- 필요한 인덱스 생성
- 초기 카테고리 데이터 삽입 (지역, 챌린지 카테고리, 가게 카테고리)

### 4. 설정 파일 생성

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

### 5. 실행

```bash
./gradlew bootRun
```

### 데이터베이스 재설정 (필요 시)

전체 데이터베이스를 재설정하려면:

```bash
# 1. 데이터베이스 및 사용자 재생성
psql -U postgres -f setup_db.sql

# 2. 테이블 재생성
psql -U greenfire_dev -d greenfire_db_dev -f init_db.sql
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