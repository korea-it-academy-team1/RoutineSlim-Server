# 로그인 방식 전환 가이드: 1번(회원 UUID) → 3번(JWT) — 이대수

지금 서버는 **1번 방식**으로 동작한다.

| | 1번 (지금) | 3번 JWT (최종) |
|---|---|---|
| 로그인 응답 | 회원 UUID 문자열 `"019a…"` | `{ accessToken, expiresIn, refreshToken }` |
| 로그인한 회원 구분 | 요청 헤더 `X-Member-Id: <UUID>` | 요청 헤더 `Authorization: Bearer <accessToken>` |
| 보안 | 없음 (헤더만 바꾸면 다른 회원 행세 가능) → 시연용 | 서명·만료 검사, 재발급, 로그아웃 |
| API 접근 | 모두 열림 | `/auth/*` 외에는 로그인 필요 |

JWT 코드는 지우지 않고 **주석으로 남겨 두었다**. 표시는 세 가지다.

| 표시 | 뜻 | 3번으로 바꿀 때 |
|---|---|---|
| `[JWT 로그인 - 이대수]` | JWT 코드 (지금은 주석) | **주석 해제** |
| `[1번 임시 - 이대수]` | 1번 방식 임시 코드 | **주석 처리 또는 삭제** |
| `[로그인 - 이대수]`, `[DB - 이대수]`, `[로컬 개발 - 이대수]`, `[버그 수정 - 이대수]` | 두 방식 모두 쓰는 코드 | 그대로 둔다 |

## 주석 해제 방법 (IntelliJ)

- Java·Gradle: `//` 로 시작하는 줄들을 **설명 줄까지 통째로** 선택 → `Ctrl + /`
  - 설명 줄도 `//    // [JWT 로그인 - 이대수] …` 처럼 한 번 더 주석이 붙어 있어서, 해제하면 일반 주석으로 돌아온다
- 파일 전체가 주석인 파일: `//import` 줄부터 파일 끝까지 선택 → `Ctrl + /` (맨 위 `package` 줄과 `/* … */` 안내는 그대로)
- yml: `#jwt:` 와 그 아래 `#  …` 줄의 **맨 앞 `#` 한 글자만** 지운다 (들여쓰기 2칸 유지. yml 은 들여쓰기가 틀리면 서버가 안 뜬다)

## 바꾸는 순서

### 1. 의존성 — `build.gradle`
- `oauth2-resource-server`, `oauth2-resource-server-test` 두 줄 주석 해제
- IntelliJ 오른쪽 Gradle 탭 → 새로고침(Reload)

### 2. 파일 전체 주석 해제 (10개)
| 파일 | 역할 |
|---|---|
| `global/security/JwtProperties.java` | yml 의 `jwt.*` 설정값 |
| `global/security/JwtConfig.java` | 토큰 서명·검증 도구 |
| `global/security/JwtTokenProvider.java` | access token 발급 |
| `domain/auth/entity/RefreshToken.java` | `refresh_token` 테이블 엔티티 |
| `domain/auth/repository/RefreshTokenRepository.java` | refresh token 조회 |
| `domain/auth/dto/TokenResponseDTO.java` | 로그인·재발급 응답 |
| `domain/auth/dto/RefreshTokenRequestDTO.java` | 재발급·로그아웃 요청 |
| `domain/auth/service/TokenService.java` | 토큰 서비스 인터페이스 |
| `domain/auth/service/TokenServiceImpl.java` | 발급·재발급(rotation)·로그아웃 |
| `src/test/.../domain/auth/AuthControllerTest.java` | JWT 로그인 흐름 테스트 5개 |

### 3. 일부만 바꾸는 파일
| 파일 | `[1번 임시]` 주석 처리/삭제 | `[JWT 로그인]` 주석 해제 |
|---|---|---|
| `domain/auth/controller/AuthController.java` | UUID 를 돌려주는 `login`, `socialLogin` | import 3줄, `tokenService` 필드, 토큰 `login`·`socialLogin`, `refresh`, `logout` |
| `domain/user/controller/UserController.java` | `X-Member-Id` 헤더로 받는 `me` | import 2줄, `@AuthenticationPrincipal Jwt` 로 받는 `me` |
| `domain/auth/dto/LoginRequestDTO.java` | - | `keepLoggedIn` 필드 |
| `global/security/SecurityConfig.java` | `.anyRequest().permitAll()`, `X-Member-Id` 허용 헤더 | `/auth/refresh`·`/auth/logout` 허용, `.anyRequest().authenticated()`, `oauth2ResourceServer`, `Authorization` 허용 헤더 |
| `src/test/.../domain/auth/MemberIdLoginTest.java` | **파일 삭제** | - |

### 4. 설정 파일 (`#jwt:` 블록 주석 해제)
- `application.yml`: 토큰 수명 3줄
- `application-prod.yml`: `secret: ${JWT_SECRET}` → 서버 환경 변수 `JWT_SECRET`(32자 이상) 설정 필요
- `application-local.yml`(각자 PC, git 에 없음)·`application-local.example.yml`: 로컬용 `secret`
- `src/test/resources/application-test.yml`: 테스트용 `secret`

### 5. 확인
- `Server test` 실행 → `AuthControllerTest` 5개 + `contextLoads` 통과
- `jwt.secret` 이 없거나 32자보다 짧으면 서버가 시작할 때 `jwt.secret(JWT_SECRET) 은 32자 이상이어야 합니다.` 로 멈춘다
- 앱/웹도 같이 바꿔야 한다 (RoutineSlim-App `docs/jwt-switch-guide.md`)

> DB 는 바꿀 것이 없다. `refresh_token` 테이블은 V1 에 이미 있고, admin 계정(V3)은 두 방식 모두 쓴다.
> 이미 적용된 `db/migration/V*.sql` 은 주석 한 글자도 고치지 않는다 (Flyway 체크섬 오류).
