# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Test (all)
./gradlew test

# Test (single class)
./gradlew test --tests "sisosolsol.greenfire.auth.AuthFlowIntegrationTest"
```

### Local Run (laptop without team DB access)

`application-local.yml` (in the `GreenFire-key` submodule) targets a specific dev DB. To run against a local Postgres without modifying the submodule, override the datasource via env vars:

```bash
# Prereq (one-time): create local DB & role, e.g.
# createdb -U postgres greenfire_local
# psql -U postgres -c "CREATE ROLE greenfire_app WITH LOGIN PASSWORD 'Greenfire!2025Local';"
# psql -U postgres -c "GRANT ALL ON DATABASE greenfire_local TO greenfire_app;"

JAVA_HOME="$(/usr/libexec/java_home -v17)" \
SPRING_PROFILES_ACTIVE=local \
SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/greenfire_local" \
SPRING_DATASOURCE_USERNAME=greenfire_app \
SPRING_DATASOURCE_PASSWORD='Greenfire!2025Local' \
JASYPT_ENCRYPTOR_PASSWORD=dummy \
./gradlew bootRun
```

`SPRING_DATASOURCE_*` env vars take precedence over yml. `JASYPT_ENCRYPTOR_PASSWORD=dummy` is required because the main `application.yml` declares Jasypt-encrypted values that must decrypt at startup even when unused under the `local` profile. `JAVA_HOME` ensures Java 17 toolchain detection on machines that default to a different JDK.

When the team DB is reachable again, drop the env vars and run plain `./gradlew bootRun`.

## Tech Stack

- **Java 17**, Spring Boot 3.3.4
- **PostgreSQL 15** — primary database
- **MyBatis** — used for most domains (XML mapper files)
- **Spring Data JPA** — used only for `auth`, `user`, `notice`, `report` domains
- **Spring Security + JJWT 0.12.3** — JWT-based stateless auth
- **Flyway** — DB migrations under `src/main/resources/db/migration/`
- **Jasypt** — encrypts `application.yml` secrets (datasource credentials, etc.)

## Architecture

### Domain Structure

Each domain follows one of two patterns depending on whether it uses MyBatis or JPA:

**MyBatis domains** (`challenge`, `post`, `location`, `store`, `category`, `image`):
```
{domain}/
  controller/
  service/
  model/
    dao/        ← @Mapper interface
    dto/
```
XML mappers live at `src/main/resources/mappers/{domain}/{DomainMapper}.xml`.

**JPA domains** (`auth`, `user`, `notice`, `report`):
```
{domain}/
  controller/
  service/
  dto/
  entity/
  repository/
```

### Authentication

`JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`. On success it puts an `AuthUser` record into `SecurityContextHolder`.

**Getting the current user in a controller:**
```java
// Option 1 (preferred, used in UserController, LocationController)
@GetMapping("/me")
public ResponseEntity<Void> example(@AuthenticationPrincipal AuthUser loginUser) {
    service.doSomething(loginUser.userId()); // UUID
}

// Option 2 (used in PostController, AdminController)
public ResponseEntity<Void> example(Authentication authentication) {
    AuthUser user = (AuthUser) authentication.getPrincipal();
    service.doSomething(user.userId());
}
```

`AuthUser` is a record with `UUID userId`, `String email`, `String role`.

Access Token expires in 60 min; Refresh Token (opaque UUID) expires in 14 days and is stored as SHA-256 hash in DB.

### Security Rules (SecurityConfig)

- `/api/auth/login`, `/api/auth/signup`, `/api/auth/refresh`, `/api/auth/logout` → `permitAll`
- `/uploads/**`, `/user/**`, `/location/**`, Swagger, `/api/public/**` → `permitAll`
- Everything else → `authenticated`

### Exception Handling

Use `CustomException` with values from `ExceptionCode` enum:
```java
throw new CustomException(ExceptionCode.CHALLENGE_NOT_FOUND);   // → 404
throw new CustomException(ExceptionCode.CHALLENGE_FULL_CAPACITY); // → 409
```
`GlobalExceptionHandler` catches these and returns structured JSON responses.

### MyBatis Conventions

- `map-underscore-to-camel-case: true` — no need for manual column aliases in most cases
- `UUIDTypeHandler` handles `UUID ↔ varchar` conversion automatically
- Use `@Param` when a mapper method takes more than one parameter
- Mapper method naming: verb + noun (`selectChallengeByCode`, `insertChallengePart`, `countCurrentParticipants`)

### DTO Naming

| Suffix | Purpose |
|--------|---------|
| `DTO` | General read/response |
| `CreateDTO` | POST request body |
| `UpdateDTO` | PATCH/PUT request body |
| `Simple{X}DTO` | Lightweight read response |
| `Request` / `Response` | Used mainly in auth domain |

### Commit Message Convention

`TYPE: 설명` — e.g., `FEAT: 챌린지 참여 인증 통합`, `FIX: 날짜 비교 로직`, `REFACTOR: 내 주변 초록불`

Common types: `FEAT`, `FIX`, `REFACTOR`, `DOCS`, `STYLE`, `TEST`, `CHORE`
