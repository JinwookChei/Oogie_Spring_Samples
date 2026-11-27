# PLAN.md — 실습 프로젝트 「맛집 노트」

회원이 가입해서 맛집 리뷰(사진 1장 + 별점 + 내용)를 쓰고, 검색하고, 탈퇴하는 작은 웹 서비스.

**목표**: 시간을 적게 들이면서 Spring 전체 그림을 이해하고, 실무 뼈대(요청 → 바인딩 → 서비스 → 트랜잭션 → SQL)는 직접 손으로 짜 본다.

**예상 소요**: 약 15~25시간 (순수 작업 13~20시간 + 환경 문제 여유분)

---

## 진행 규칙

| 표시 | 의미 | 누가 작성 |
|---|---|---|
| 🔍 | 세세하게 | 교육생이 직접 작성 → 튜터가 한 줄씩 리뷰 |
| 👀 | 큰 그림 | 개념 위주로 짧게 가이드 → 교육생이 직접 작성 → 가볍게 확인 |

- **튜터는 코드를 주지 않고 가이드만 한다.** 설정 파일, SQL, JSP를 포함한 모든 파일을 교육생이 직접 작성한다. (교육생이 "코드 보여 줘"라고 요청할 때만 예외)
- **JSP 화면은 최소한으로** 만든다. 디자인 없이 폼과 출력만 두고, 샘플 FW_05의 JSP를 참고해 빠르게 작성한다. 시간은 Java 코드(Controller · Service · DAO · mapper · 테스트)에 집중한다.
- 마일스톤은 **완료 기준**을 통과해야 끝난다. 끝나면 체크박스와 진행 기록을 갱신한다.
- 순서는 **기능 한 줄 관통**이다. 회원가입 하나를 DB부터 화면까지 먼저 완성하고 나서 리뷰 기능으로 넓힌다.
- 참고 샘플: `SampleDemo/` (FW_05 = MVC, FW_07 = MyBatis·Transaction). 구조와 이름은 샘플 관례를 따른다.

---

## 프로젝트 정보

| 항목 | 값 |
|---|---|
| 폴더 | `MatzipNote/` |
| Group / Artifact | `com.study` / `matzip-note` |
| 기본 패키지 | `com.study.matzip` |
| IDE | STS 5.0.1.RELEASE |
| 빌드 | Maven, Java 21, Spring Boot 4.x |
| DB | MySQL, 스키마 `matzip` |
| View | JSP (`/WEB-INF/views/`) |
| 실행 | STS `Run As > Spring Boot App` (JSP는 실행 jar에서 제대로 동작하지 않으므로 jar로 배포해 실행하지 않음) |

### 패키지 구조

```
com.study.matzip
 ├─ controller     @Controller
 ├─ model
 │   ├─ dto        Member, Review, ReviewSearch
 │   ├─ dao        MemberDao, ReviewDao (MyBatis interface)
 │   └─ service    MemberService, ReviewService (interface + impl)
 ├─ storage        ImageStorage (interface), LocalImageStorage, FakeImageStorage
 ├─ aspect         실행 시간 측정 Aspect
 ├─ exception      커스텀 예외, @ControllerAdvice
 └─ config         @MapperScan, WebMvcConfigurer
resources
 ├─ mappers/       member.xml, review.xml
 ├─ scripts/       schema.sql
 └─ application.properties
webapp/WEB-INF/views/   JSP (튜터 제공)
```

### 테이블

```sql
member (mno PK AI, email UNIQUE, password, name, created_at)
review (rno PK AI, mno FK → member, title, content, rating 1~5, image_name NULL, created_at)
```

### 비즈니스 규칙

1. 로그인한 회원만 리뷰를 쓸 수 있다.
2. **리뷰 수정·삭제는 작성자 본인만** 할 수 있다. 이 검사는 **Service**에서 하고, 위반하면 커스텀 예외를 던진다.
3. 회원 탈퇴 시 그 회원의 리뷰를 모두 지우고 회원을 지운다. 두 작업은 **하나의 트랜잭션**이다.
4. 별점은 1~5. 검증은 Service의 간단한 if 문과 DB 제약으로 한다.

---

## 마일스톤

### M1. 프로젝트 세팅 👀 · 1~2시간 — ✅ 완료 (2026-11-14)
교안: SpringBoot, Logging · 샘플: FW_05 `pom.xml`, FW_07 `application.properties`

- [x] STS `File > New > Spring Starter Project`로 프로젝트 생성 (Maven, Java 21 / 의존성: Spring Web, MyBatis, MySQL Driver, Lombok, DevTools)
- [x] JSP 의존성 수동 추가 (`tomcat-embed-jasper`, JSTL 2개) + `spring.mvc.view.prefix/suffix`
- [x] `application.properties`: DB 접속, MyBatis, JSP, 로깅 레벨 (HikariCP는 기본값 사용)
- [x] `schema.sql`로 DB와 테이블 생성 (테스트 데이터 포함, rating CHECK 실험 완료)
- [x] 메인 화면 Controller 하나로 `index.jsp` 확인 + `ConfigTest` 통과

**알고 넘어갈 것**: Starter, Auto Configuration, 내장 Tomcat, `@SpringBootApplication` = 설정 + 자동 설정 + 컴포넌트 스캔

**✅ 완료 기준**: 앱을 실행해 브라우저에서 `index.jsp`가 보이고, `DataSource` 주입 테스트(FW_07 `ConfigTest` 방식)가 통과한다.

---

### M2. 회원가입 관통 + MyBatis CRUD 🔍 · 4~6시간 — ✅ 완료 (2026-11-17, 일부 미룸)
교안: Framework, DI, Bean 등록, Spring & MyBatis · 샘플: FW_07 `member.xml`, `address.xml`

**2-1. 회원가입 한 줄 관통**
- [x] `Member` DTO
- [x] `MemberDao` + `member.xml`: insert(PK 받아오기), selectByEmail
- [x] `MemberService` (interface + impl), 생성자 주입 (`final` + `@RequiredArgsConstructor`)
- [x] `MemberController`: 가입 폼 → 가입 처리 → redirect (JSP는 튜터 제공)
- [x] 브라우저에서 가입 → DB에 행이 생긴 것 확인 (redirect·에러 메시지·F5 PRG까지 확인 ✅)

**2-2. 리뷰 CRUD**
- [x] `Review` DTO, `ReviewDao` + `review.xml`: insert / select / delete / update
- [x] 리뷰 상세: review + 작성자 join → `resultMap` + `<association>` (XML은 튜터 작성)
- [x] 리뷰 검색: 키워드, 최소 별점 → `<where>` + `<if>` (괄호 회귀 테스트 포함)
- [ ] `@MybatisTest`로 DAO 테스트 (`@Transactional` 롤백 확인) — ⏸️ `@SpringBootTest`로 insert·검색만 통과, join 상세·update·delete·`@MybatisTest`는 M3로 미룸

**셀프 체크**: `@Component`만 붙이면 빈이 될까? `@Mapper` interface는 구현 클래스가 없는데 왜 주입될까? `#{}` 대신 `${}`를 쓰면 무슨 일이 생길까?

**알고 넘어갈 것**: IoC, 싱글톤 빈은 상태를 갖지 않는다, 스테레오타입(`@Controller` / `@Service` / `@Mapper`), namespace + id 연결, DAO는 interface만

**✅ 완료 기준**: 브라우저에서 회원가입이 되고, `ReviewDaoTest`(insert · join 상세 · 동적 검색 · delete)가 전부 통과한다.

---

### M3. MVC 요청 처리 🔍 · 3~5시간 — ✅ 완료 (2026-11-22)
교안: Spring MVC pt.1, pt.2 (Part 1·2) · 샘플: FW_05 `SimpleController`

- [x] 로그인·로그아웃 (세션에 `loginUser`) ✅ (2026-11-17)
- [x] 리뷰 목록(검색 포함)·상세·작성·수정·삭제 Controller ✅ (2026-11-22)
- [x] `@RequestParam` / `@ModelAttribute` / `@PathVariable` 바인딩 ✅
- [x] 등록·수정·삭제 후 **PRG** + `addFlashAttribute`로 "완료" 메시지 ✅ (2026-11-22)
- [x] 작성자 본인 검사를 **Service**에 구현 → 위반 시 커스텀 예외 ✅ (2026-11-19, `ForbiddenException` + `checkReviewMno`)
- [x] 로그인 검사를 **각 Controller 메서드에 직접** 넣어 보기 ✅ (중복이 생기는 걸 일부러 체감 → M4 AOP의 동기)

**셀프 체크**: 왜 등록 후에 forward가 아니라 redirect일까? redirect 후 `Model`에 넣은 값은 왜 사라질까? 권한 검사는 왜 Controller가 아니라 Service에 둘까?

**알고 넘어갈 것**: DispatcherServlet → HandlerMapping → HandlerAdapter → ViewResolver 흐름, forward vs redirect, 400·404·405가 나는 이유

**✅ 완료 기준**: 브라우저에서 로그인 → 리뷰 작성 → 검색 → 수정 → 삭제가 되고, 다른 회원의 리뷰는 삭제가 막힌다.

---

### M4. 트랜잭션 + AOP 🔍 · 2~3시간 — ✅ 완료 (2026-11-22)
교안: Spring Transaction, 스프링 AOP · 샘플: FW_07 `BasicMemberService.delete`, FW_04 `LoggingAspect`

- [x] 회원 탈퇴 = 리뷰 전체 삭제 + 회원 삭제. 중간에 예외를 일부러 넣어 **트랜잭션 없이 실패하는 것 재현** ✅ (`MemberWithdrawTest`, 리뷰만 지워짐 🔴)
- [x] `@Transactional`로 해결하고 테스트로 확인 ✅ (같은 테스트 🟢, 실험 테스트는 `@Disabled`로 보존 + 정상 탈퇴 테스트)
- [x] Service 계층 실행 시간 측정 `@Around` Aspect ✅ (`ExecutionTimeAspect`, try/finally로 예외 호출도 측정)
- [x] M3에서 중복된 로그인 검사를 보며 "한 곳으로 모으는 방법" 토론 (Interceptor는 👀 설명만) ✅

**셀프 체크**: 같은 클래스 안에서 호출하면 왜 `@Transactional`이 안 먹을까? checked 예외가 나면 롤백될까?

**알고 넘어갈 것**: 프록시, RuntimeException만 롤백, 전파 기본값 REQUIRED, Pointcut 표현식 읽기

**✅ 완료 기준**: 탈퇴 테스트가 `@Transactional` 없이는 실패(리뷰만 지워짐)하고, 붙이면 통과한다. 로그에 Service 메서드 실행 시간이 찍힌다.

---

### M5. 마무리 · 3~4시간 — ✅ 완료 (2026-11-22)
교안: Spring MVC pt.2 (Part 1·3·4), DI · 샘플: FW_05 `MvcConfigurer`, `MvcExceptionHandler`, `ControllerTest`

- [x] 🔍 **`ImageStorage` 인터페이스로 DI 체감** ✅ (`@Profile("local"/"fake")`, `ImageStorageTest`, 작성·수정 시 업로드 + `/upload/**` 표시, 수정 시 사진 유지)
  - `LocalImageStorage`(디스크 저장, UUID 파일명)와 `FakeImageStorage`(저장하지 않고 이름만 반환) 두 구현체
  - 같은 타입 빈 2개 → 에러 확인 → `@Primary` 또는 `@Profile`로 교체
  - 리뷰 작성 시 사진 업로드 (`MultipartFile`), `addResourceHandlers`로 화면에 표시
- [x] 👀 `@ControllerAdvice` + `@ExceptionHandler`: 권한 예외 → 403 페이지, 그 외 → 500 페이지, `error/404.jsp` ✅ (`config/MvcExceptionHandler`)
- [x] 👀 `@WebMvcTest` + `MockMvc` + `@MockitoBean`으로 리뷰 Controller 테스트 1~2개 ✅ (`ReviewControllerTest` 2개)

**셀프 체크**: `ReviewService`는 `ImageStorage` 구현체가 바뀌어도 왜 코드를 고칠 필요가 없을까? 404는 왜 `@ControllerAdvice`가 아니라 BasicErrorController가 처리할까?

**알고 넘어갈 것**: DIP·OCP와 DI의 관계, PSA, ControllerAdvice(Controller 예외) vs BasicErrorController(404·405), Mock 환경이 필요한 이유

**✅ 완료 기준**: 사진이 있는 리뷰가 화면에 보이고, `@Profile`을 바꾸면 `FakeImageStorage`로 동작한다. 남의 리뷰 삭제 시 403 페이지가 뜨고, MockMvc 테스트가 통과한다.

---

### M6. Interceptor 🔍 · 약 1시간 (시간 빠듯 → 핵심만) — ✅ 완료 (2026-11-24)
교안: Interceptor (Notion) · 샘플: 없음 (교안 TODO 06·07 기준)

- [x] 🔍 `LoginCheckInterceptor implements HandlerInterceptor`: `preHandle`에서 세션 `loginUser` 확인 → 없으면 `sendRedirect` + `return false`
- [x] 🔍 `WebConfig.addInterceptors`에 등록, `addPathPatterns`로 리뷰 작성·수정·삭제만
- [x] 🔍 Controller 5곳의 로그인 검사 제거 → 전체 테스트 통과 확인 (`ReviewControllerTest` ② 비로그인 작성 테스트가 그대로 통과해야 함)
- 👀 (설명만) `postHandle`·`afterCompletion`, 여러 Interceptor 순서, `addViewControllers`, Filter vs Interceptor vs AOP

**셀프 체크**: `preHandle`이 false를 반환하면 무엇을 직접 해 줘야 할까?

**✅ 완료 기준**: Controller에 로그인 검사 코드가 없는데, 비로그인으로 작성·수정·삭제에 접근하면 로그인 화면으로 간다. 전체 테스트 통과.

---

### M7. REST API · 약 2시간 (시간 빠듯 → 핵심만) — ✅ 완료 (2026-11-22)
교안: REST API (Notion) · 샘플: 없음 (교안 TODO 02·03 기준)

- [x] 🔍 `ReviewRestController`(`@RestController`, `/api/reviews`) ✅ (실제 경로는 `/api/review`): 상세 `GET /{rno}` + 작성 `POST`(`@RequestBody`) → `ResponseEntity`(작성은 201 Created)
- [x] 🔍 `@RestControllerAdvice`(API 패키지만) + `ProblemDetail`: 없는 리뷰 → 404 JSON ✅ (`config/RestExceptionHandler`)
- [x] 🔍 MockMvc `jsonPath` 테스트 1~2개 ✅ (`ReviewRestControllerTest` 4개)
- 👀 (설명만) REST URL 설계 규칙, PUT vs PATCH, API Versioning, Swagger, CORS·SOP, `RestClient`

**셀프 체크**: 화면용 `@ControllerAdvice`와 API용 `@RestControllerAdvice`를 왜 나눌까?

**✅ 완료 기준**: `/api/reviews`로 리뷰를 JSON으로 조회·작성할 수 있고, 없는 리뷰는 JSON(`ProblemDetail`) 404. `jsonPath` 테스트 통과.

---

### M8. 예외 처리 다시 보기 🔍 · 약 2~3시간 — ✅ 완료 (2026-11-27)
교안: **Spring MVC pt.2 Part 3** (Notion 13~19장), **REST API** 12~14장 → 맛집 노트용 실습 안내서 **`guides/M8_예외처리.md`** · 샘플: FW_05·FW_10 `MvcExceptionHandler`·`RestExceptionHandler`·`RecordNotFoundException`

M5·M7에서 튜터가 대부분 써 준 예외 처리를 **비워 놓고 다시 직접** 쓴다. (교육생 요청, 2026-11-24)
튜터가 세팅해 둔 것: Advice 2개를 TODO 뼈대로 비움, Service·Controller에 `TODO: 8-x` 주석, **목표 테스트 5개**(MVC ③④, REST ⑤⑥ + 기존 ①②가 빨간 불)

- [x] 👀+🔍 8-1 관찰: Advice 없이 7가지 요청(남의 글 삭제, 없는 리뷰, `/review/abc`, 없는 URL, 405, API를 브라우저로·curl로) → 상태 코드·화면·처리 담당 표 채우기
- [x] 🔍 8-2 BasicErrorController: `error/4xx.jsp`, `server.error.include-message` 실험 후 되돌리기, 👀 `ErrorAttributes`
- [x] 🔍 8-3 `MvcExceptionHandler`: 범위 지정, 403·500(고정 문구 + 스택 트레이스 로그), `@ResponseStatus` 빼기 실험, `/review/abc`가 몇 번이 되는지 설명
- [x] 🔍 8-4 커스텀 예외 `RecordNotFoundException`(샘플 이름) → Service 2곳, 화면 404, `error/404.jsp`에 msg, 테스트 ⑤ 작성 (`IllegalArgumentException`→404 매핑의 문제를 설명할 수 있어야 함)
- [x] 🔍 8-5 `ReviewController` try-catch 정리: "사용자가 그 자리에서 고칠 수 있나?" 기준으로 남김/삭제 판단 기록 (M5 ⏭️ Forbidden catch 정리 포함)
- [x] 🔍 8-6 `RestExceptionHandler`: 404·403·500·**415**(Exception 핸들러가 Spring 표준 예외를 삼키는 것을 테스트 ⑥으로 재현 후 해결), 테스트 ⑦ 작성, (선택) `code` 필드
- [x] 👀 8-7 정리: 전/후 표 비교, git의 M5·M7 코드와 비교, Advice가 잡는/못 잡는 예외(Filter → M13 JWT 연결) — 셀프 체크 8~14번 남음, `/review/abc` 400→500은 ⏸️ 알려진 한계

**셀프 체크**: 안내서 6장 14문제 (교안 Part 3 Q + M5·M7에서 "모르겠어"였던 문제 재도전)

**✅ 완료 기준**: 전체 테스트 초록(목표 테스트 + 교육생 작성 ⑤⑦). 남의 글 403, 없는 리뷰 404, 예상 못 한 오류 500이 화면은 에러 JSP로, API는 `ProblemDetail`로 나가고 내부 정보가 노출되지 않는다. Controller에는 "폼으로 돌아가는" catch만 남는다.

---

### M9. Interceptor 심화 🔍 · 약 1~1.5시간
교안: Interceptor (Notion) 2~4·6·7장 · 샘플: **FW_09** (`LoggingInterceptor`, `MvcConfigurer`, TODO 06·08)

M6에서 👀로 넘긴 `postHandle`·`afterCompletion`과 여러 Interceptor 순서를 **직접 만들어서 콘솔로 확인**한다.

- [ ] 🔍 9-1 `LoggingInterceptor` (교안 TODO 06): `preHandle`에서 요청 URI·메서드 로그 + 시작 시간을 **request 속성**에 저장(필드 X, 싱글톤) → `afterCompletion`에서 처리 시간(ms) + `ex`가 있으면 에러 로그
- [ ] 🔍 9-2 `handler` 활용: `instanceof HandlerMethod`로 확인한 뒤 "Controller 이름#메서드 이름" 로그 (정적 리소스면 `ResourceHttpRequestHandler`가 온다 → 확인 없이 형변환하면 `ClassCastException`)
- [ ] 🔍 9-3 `WebConfig` 등록: `LoggingInterceptor`는 전체 요청 + `excludePathPatterns`로 정적 리소스(`/upload/**` 등) 제외. `LoginCheckInterceptor`와의 순서를 `.order()`로 정함
- [ ] 🔍 9-4 순서 실험: 비로그인으로 `/review/write` → 콘솔에서 `preHandle` 순서, `afterCompletion` **역순**, false 이후 누구의 `afterCompletion`이 불리는지 관찰 (교안 셀프 체크 Q1을 실제로 확인)
- [ ] 👀 9-5 `addViewControllers` (교안 TODO 08): `MainController.home()`은 View 이름만 반환 → `addViewControllers` 한 줄로 대체할 수 있는지 판단해 보기 (대체하면 `MainController` 삭제. 로그 줄도 사라짐)
- 👀 (설명만) `postHandle`의 `ModelAndView`(REST면 null), Filter로 할 일 vs Interceptor로 할 일, preflight(OPTIONS) 통과 (M10 CORS와 연결)

**셀프 체크**: 교안 9장 Q1~Q5 (특히 Q3 "시작 시간을 필드에 저장하면?", Q4 "`@ControllerAdvice`가 처리한 예외의 `ex`는?")

**✅ 완료 기준**: 요청마다 콘솔에 "Controller#메서드 + 처리 시간"이 찍히고, 정적 리소스 요청에는 찍히지 않는다. 비로그인 차단 요청에서 Interceptor 호출 순서를 설명할 수 있다. 전체 테스트 통과.

---

### M10. REST API 심화 🔍 · 약 3~4시간
교안: REST API (Notion) 6·11·14·17·19~35장 · 샘플: **FW_10** (`MvcConfigurer`, `RestExceptionHandler`, `SwaggerConfig`, `EtcRestController`, `BingClient`)

M7에서 만든 `ReviewRestController`(상세·작성)를 **CRUD 전체**로 넓히고, 교안 후반부(Swagger·CORS·RestClient)를 맛집 노트에 붙인다.

**10-1. CRUD 완성 (교안 TODO 04~07)** 🔍
- [ ] 목록·검색 `GET` → `@ModelAttribute ReviewSearch`(쿼리스트링, body 없음) → 200
- [ ] 수정 `PUT /{rno}` → `@RequestBody` + 세션 mno → 200 + 수정된 리뷰 (교안 ⚠️: URL에 대상 번호를 넣는다)
- [ ] 삭제 `DELETE /{rno}` → **204 No Content**
- [ ] 남의 글 수정·삭제 → **403** `ProblemDetail` 확인 (핸들러는 M8 8-6에서 이미 만듦 → 테스트만 추가)
- [ ] `ReviewRestControllerTest`에 목록·수정·삭제·403 테스트 추가 (`jsonPath`, `status().isNoContent()`)

**10-2. URL 정리 + API Versioning (교안 6장, TODO 01)** 🔍 짧게
- [ ] 👀 Spring 7 `configureApiVersioning()` + `usePathSegment(1, ...)` 개념 (교안: "소개에 의의")
- [ ] 🔍 URL을 교안·샘플 관례대로 `/api/v1/reviews`로 바꾸기 (복수형 + 버전). 테스트의 URL도 같이 수정

**10-3. Swagger (교안 19~24장, TODO 08)** 🔍 설정만
- [ ] `springdoc-openapi-starter-webmvc-ui` 의존성 추가 (Boot 4 → **3.x** 버전, FW_10 `pom.xml` 참고)
- [ ] `/swagger-ui/index.html`에서 리뷰 API 목록 확인 → Execute로 실제 호출
- [ ] 👀 `@Tag`·`@Operation`·`@Schema`로 설명 달기 (`ReviewRestController` 한 곳만)

**10-4. CORS (교안 25·26장, TODO 09)** 👀 + 🔍 한 줄
- [ ] 👀 SOP와 CORS 개념, preflight
- [ ] 🔍 `WebConfig.addCorsMappings`로 `/api/v1/**` 허용 (M13에서 Security용 `CorsConfigurationSource`로 옮긴다)

**10-5. RestClient (교안 27~35장, TODO 10·11)** 🔍
- [ ] 🔍 proxy API `GET /api/v1/etc/bingimage`: `RestClient`로 Bing 오늘의 이미지 조회 → JSON 그대로 반환 (API 키 불필요)
- [ ] 👀 HTTP Interface(`@GetExchange`)로 정리하는 방식 (교안 31~34장). 원하면 🔍로 바꿔서 빈 등록까지
- [ ] (선택) 메인 화면(`index.jsp`) 배경에 Bing 이미지 표시 (JSP 최소)

**셀프 체크**: 교안 18·24·35장 ✍️ (+ "GET 검색은 왜 `@RequestBody`가 아닐까?", "남의 서버는 CORS를 내가 못 바꾸는데 어떻게 데이터를 가져올까?")

**✅ 완료 기준**: `/api/v1/reviews`로 리뷰 목록·상세·작성·수정·삭제가 되고(200/201/204), 없는 리뷰 404·남의 글 403이 `ProblemDetail`로 나온다. Swagger UI에서 API를 호출할 수 있고, `/api/v1/etc/bingimage`가 Bing JSON을 돌려준다. 전체 테스트 통과.

---

### M11. Spring Security 기초 🔍 · 약 2~3시간
교안: **Spring Security pt.1** (Notion, Part A 요약 + Part B 5·6장) · 샘플: **FW_11** (`CustomSecurityConfig`, TODO 02~06)

직접 만든 "세션 + Interceptor 로그인 검사"를 **Security의 필터**가 대신하게 바꾸는 첫 단계. 이번에는 교안대로 **메모리 사용자**로 규칙만 확인하고, DB 회원 연결은 M12에서 한다.

> ⚠️ M11이 끝난 시점에는 앱의 로그인이 **반쯤 바뀐 상태**다 (Security 로그인은 메모리 사용자, Controller는 아직 세션 `loginUser`를 읽음). 브라우저 확인은 M12에서 하고, **M11 → M12는 이어서 진행**한다. 커밋은 M11 단위로 해도 된다(테스트는 통과시킨다).

- [ ] 👀 11-1 개념 (Part A 1~4장): 인증 vs 인가, `SecurityFilterChain`이 DispatcherServlet **앞**(Filter)에 있다는 것, 인증 흐름(`Filter → AuthenticationManager → Provider → UserDetailsService + PasswordEncoder`), `SecurityContext`. Part B 2장(`DelegatingFilterProxy`·`FilterChainProxy`)은 그림만
- [ ] 🔍 11-2 의존성 (TODO 02): `spring-boot-starter-security`, `spring-security-test`(test), `spring-security-taglibs` → **아무 설정 없이 실행**해서 모든 주소가 로그인 화면으로 가는 것, 콘솔의 `Using generated security password` 확인
- [ ] 🔍 11-3 `security/config/SecurityConfig` (TODO 03·04): `@EnableWebSecurity` + `PasswordEncoder` 빈(`createDelegatingPasswordEncoder`, `{bcrypt}` 접두사 확인) + 👀 `RoleHierarchy`(`ADMIN > USER`)
- [ ] 🔍 11-4 메모리 사용자 (TODO 05): `InMemoryUserDetailsManager`로 USER 1명·ADMIN 1명 → `UserDetailsService`로 조회하고 `PasswordEncoder.matches`로 비교하는 테스트 (교안 5-7-3)
- [ ] 🔍 11-5 `SecurityFilterChain` (TODO 06): 맛집 노트 규칙으로 바꿔 쓰기
  - 리뷰 작성·수정·삭제(`/review/write`, `/review/*/edit`, `/review/*/delete`) → `hasRole("USER")`
  - 나머지 → `permitAll()` (`anyRequest()`는 **맨 마지막**, 교안 6-6 순서 주의)
  - `formLogin`·`logout`을 `Customizer.withDefaults()`로 **다시 켜기** (안 켜면 로그인 화면이 사라진다 — 교안 실수 ②)
- [ ] 🔍 11-6 `LoginCheckInterceptor` 등록 해제 (TODO 06-2, "SessionInterceptor는 이제 안녕~") → `LoggingInterceptor`(M9)는 그대로 둔다
- [ ] 🔍 11-7 테스트 (교안 6-8): `SecurityConfigTest`(MockMvc) — 비로그인 `/` 200·`/review/write` **302**, `@WithMockUser(roles="USER")`로 200. 기존 `@WebMvcTest`(`ReviewControllerTest`·`ReviewRestControllerTest`)가 401/302로 깨지면 원인을 보고 고치기 (MVC pt.2 교안 🧓 메모)

**셀프 체크**: 교안 Part A 핵심 정리 9개 (특히 "302 vs 403", "`SecurityFilterChain`을 직접 등록하면 왜 로그인 화면이 사라질까?", "Interceptor로 하던 일을 왜 Filter에서 하게 됐을까?")

**✅ 완료 기준**: 비로그인으로 리뷰 작성 주소에 가면 Security 로그인 화면으로 가고, 테스트로 302/200/403을 확인한다. `LoginCheckInterceptor`는 등록에서 빠졌다. 전체 테스트 통과.

---

### M12. DB 회원으로 Security 로그인 🔍 · 약 3~4시간
교안: **Spring Security pt.2** (Notion) · 샘플: **FW_11** (`CustomUserDetails`, `CustomUserDetailsService`, TODO 07~13)

M11의 메모리 사용자를 **맛집 노트 `member` 테이블**로 바꾸고, Controller·JSP가 세션 `loginUser` 대신 Security의 로그인 정보를 쓰게 한다. **비밀번호 평문 저장이 여기서 해결된다.**

**12-1. 준비: role 컬럼과 비밀번호 암호화 (TODO 07)** 🔍
- [ ] `member`에 `role` 컬럼 추가(`ALTER TABLE`, 기본값 `'USER'`) + `schema.sql`·`Member` DTO에 반영. 관리자 1명은 SQL로 `ADMIN`
- [ ] `MemberDao`에 전체 조회·비밀번호 수정 메서드 + `member.xml` SQL
- [ ] `PasswordEncodingTest`로 기존 회원 비밀번호를 한 번에 암호화 (⚠️ 실제 DB 변경 → **한 번만**, `@Transactional` 붙이지 않는 이유 생각해 보기)
- [ ] 회원가입(`MemberService`)에서 `pe.encode(...)` 후 저장

**12-2. `CustomUserDetails` + `CustomUserDetailsService` (TODO 07)** 🔍
- [ ] `security/dto/CustomUserDetails`: `Member`를 감싸서 username = 이메일, password, authorities = `"ROLE_" + role`
- [ ] `security/service/CustomUserDetailsService`: `selectByEmail` → 없으면 `UsernameNotFoundException` → `CustomUserDetails` 반환
- [ ] M11의 `InMemoryUserDetailsManager` 빈 **삭제** (`UserDetailsService` 빈이 2개면 안 된다)
- [ ] `MemberService.login`은 deprecate (비밀번호 비교는 Security가 한다) → `MemberServiceTest`의 login 테스트 정리

**12-3. 로그인·로그아웃을 Security로 (TODO 08·09)** 🔍
- [ ] `csrf` 비활성화 (교안대로, 👀 CSRF 개념·토큰은 설명만)
- [ ] `logout`: `logoutUrl("/member/logout")`, `invalidateHttpSession(true)`, `logoutSuccessUrl("/")` → `MemberController`의 로그아웃 메서드 제거
- [ ] `formLogin`: `loginPage("/member/login")`, `loginProcessingUrl("/member/login")`, `usernameParameter("email")`, `failureUrl("/member/login?error")`, `defaultSuccessUrl("/")`, `permitAll()` → `MemberController`의 `@PostMapping` 로그인 제거, GET은 `error` 파라미터로 알림 문구
- [ ] 👀 기존 `login.jsp`의 `name="email"`/`"password"`가 그대로 쓰이는 이유

**12-4. 로그인 정보 활용 (TODO 10·11)** 🔍
- [ ] Controller: `HttpSession` + `loginUser` → **`@AuthenticationPrincipal CustomUserDetails`**에서 mno 꺼내기 (`ReviewController`·`ReviewRestController`·`MemberController` 탈퇴)
- [ ] JSP: `<sec:authorize access="isAuthenticated()">`, `<sec:authentication property="principal.member.name">` (`index.jsp`, `detail.jsp`의 삭제·수정 버튼)
- [ ] 테스트: `sessionAttr("loginUser", ...)` → `SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(...))`로 바꾸기
- [ ] 👀 "화면에서 버튼을 숨기는 것은 보안이 아니다" → 서버 쪽(URL 규칙 + Service 작성자 검사)이 진짜 방어

**12-5. Remember-Me (TODO 12)** 🔍 짧게
- [ ] `login.jsp`에 `name="remember-me"` 체크박스 + `rememberMe(...)`로 60초 → 브라우저 껐다 켜서 유지·만료 확인, 개발자 도구에서 쿠키 2개 확인

**12-6. 메서드 보안 (TODO 13)** 👀 + 선택 🔍
- [ ] 👀 `@EnableMethodSecurity`, `@Secured`(`ROLE_` 전체 이름) vs `@PreAuthorize`(실행 전, `#인수`) vs `@PostAuthorize`(실행 후, `returnObject`)
- [ ] (선택) 관리자만 들어가는 주소 하나에 `@Secured("ROLE_ADMIN")` + 403 테스트

**셀프 체크**: 교안 pt.2 핵심 정리 (특히 "비밀번호 비교는 누가 하나?", "`loginProcessingUrl`을 정하면 왜 `@PostMapping` 로그인이 필요 없나?", "세션에 이제 무엇이 저장되나? (`SPRING_SECURITY_CONTEXT`)")

**✅ 완료 기준**: DB 회원(암호화된 비밀번호)으로 로그인·로그아웃이 되고, 리뷰 작성·수정·삭제·탈퇴가 `@AuthenticationPrincipal`로 동작한다. 세션 `loginUser`를 쓰는 코드가 남아 있지 않다. Remember-Me 동작 확인. 브라우저 확인 + 전체 테스트 통과.

---

### M13. JWT로 REST API 인증 🔍 · 약 4~5시간
교안: **JWT** (Notion) · 샘플: **FW_13** (`JWTUtil`, `JWTVerificationFilter`, `JWTAuthenticationFilter`, `SecurityExceptionHandlingFilter`, `APISecurityConfig`, TODO 01~07)

M10의 `/api/v1/reviews`를 **세션 대신 토큰**으로 인증한다. 화면(JSP)은 M12의 세션 로그인을 그대로 쓰고, **`/api/**`만 따로 JWT 체인**을 둔다.

**13-1. 개념 (교안 1장)** 👀
- [ ] Session(번호표, Stateful) vs JWT(신분증, Stateless), scale-out·MSA에서 세션의 한계, "JWT는 보안이 아니라 편의성·확장성"
- [ ] 토큰 구성 `Header.Payload.Signature`, Base64URL은 **암호화가 아니다** → Payload에 비밀번호 금지. jwt.io로 직접 디코딩

**13-2. `JWTUtil` (TODO 01~03)** 🔍
- [ ] JJWT 의존성 3개(`jjwt-api`, `jjwt-impl`·`jjwt-jackson`은 runtime) + properties: access 1분·refresh 2분(실습용), secret (⚠️ 커밋해도 되는 값인지 생각)
- [ ] `JWTUtil`: `@Value`로 설정 받기 → `Keys.hmacShaKeyFor`로 `SecretKey` → `createAccessToken`(email·name·role claim) / `createRefreshToken`(email만) / `getClaims`(`parseSignedClaims`)
- [ ] `JWTUtilTest`: 만든 토큰을 다시 읽기, 위조(서명 다름) → `SignatureException`, 만료 → `ExpiredJwtException`

**13-3. 필터 3개 (TODO 04·05)** 🔍
- [ ] `JWTVerificationFilter`(`OncePerRequestFilter`): `Authorization: Bearer ` 잘라내기 → 검증 → `SecurityContextHolder`에 저장, 토큰 없으면 그냥 통과
- [ ] `JWTAuthenticationFilter`(`UsernamePasswordAuthenticationFilter` 확장): `/api/v1/auth/login`, 성공 시 `{ "accessToken": ... }` JSON 응답, 실패 시 예외를 바깥으로
- [ ] `SecurityExceptionHandlingFilter`: `JwtException`·`BadCredentialsException` → 401, 그 외 500 JSON (👀 왜 `@RestControllerAdvice`(M7 `RestExceptionHandler`)로는 못 잡을까?)
- [ ] 👀 세 필터에 `@Component`를 붙이지 않는 이유 (이중 등록)

**13-4. `APISecurityConfig` (TODO 06)** 🔍
- [ ] `@Order(1)` + `securityMatcher("/api/**")`, `csrf` 끄기, `SessionCreationPolicy.STATELESS`
- [ ] 규칙: `/api/v1/auth/**`·`/api/v1/etc/**` → `permitAll`, 리뷰 목록·상세(GET) → `permitAll`, 작성·수정·삭제 → `authenticated`
- [ ] `addFilterAt`/`addFilterBefore`로 **예외 → 검증 → 로그인** 순서 등록
- [ ] M12의 웹 체인은 `@Order(2)`로 → 👀 "요청 하나는 체인 하나만 탄다"
- [ ] CORS를 M10 `addCorsMappings`에서 **`CorsConfigurationSource` 빈**으로 옮기기 + `.cors(withDefaults())` (교안: Security가 Filter 단계라 MVC CORS보다 먼저 막을 수 있다)
- [ ] 👀 `ReviewRestController`는 **한 줄도 안 바뀐다** (`@AuthenticationPrincipal`은 인증 방식과 무관) → 바뀌면 설계를 다시 보기

**13-5. 확인 (TODO 07)** 🔍
- [ ] `ReviewRestControllerTest`(또는 `JwtApiTest`): 로그인 → 토큰 받기 → `header("Authorization", "Bearer " + token)`로 작성 201 / 토큰 없이 401 / 위조 토큰 401
- [ ] 👀 + 선택 🔍 교안처럼 `static/api-test.html`에서 `fetch` + `Authorization` 헤더로 직접 호출 (만료 1분 후 `TOKEN_ERROR` 확인)

**셀프 체크**: 교안 JWT 핵심 정리 (특히 "로그인 요청은 DispatcherServlet을 탈까?", "토큰이 틀린 요청과 토큰이 없는 요청은 각각 어디서 막히나?", "JWT 로그아웃은 왜 어려운가?")

**✅ 완료 기준**: `POST /api/v1/auth/login`으로 access token을 받고, 그 토큰으로 리뷰 작성·수정·삭제 API가 동작한다. 토큰 없음·위조·만료는 401 JSON. 화면(JSP) 로그인은 M12 그대로 동작. 전체 테스트 통과.

---

### ⏸️ 보류: Spring AI · JWT 심화 (교안 대기)
- **Spring AI** (샘플 **FW_14**): Notion에 교안이 아직 없다 (2026-11-24 확인). 교안이 올라오면 M14~으로 만든다. 연결점: 리뷰 요약·추천 (SSAFY GMS 키 필요)
- **JWT 심화** (샘플 FW_13 TODO 08~17): refresh token DB 저장·`/api/v1/auth/refresh` 재발급·로그아웃, Swagger에 JWT 붙이기, GitHub OAuth2 소셜 로그인 → JWT 교안은 TODO 07까지라 다루지 않는다. 교안이 추가되면 M13 뒤에 붙인다

---

## 범위 밖 (큰 그림만)

Legacy 초기화(web.xml, Root/Servlet 컨텍스트), FactoryBean·Auto Configuration 직접 만들기, Filter 구현, REQUIRED 외의 전파 속성·isolation, `@Async`·`@Cacheable`, Resilience(`@ConcurrencyLimit`, `@Retryable`), Base64·Ajax 업로드, 페이징, Filter 직접 구현(FW_09 `filter/`), Ajax 화면(교안 TODO 03, 👀), Security의 JDBC 방식(`JdbcUserDetailsManager`)·CSRF 켜고 토큰 처리·Security 필터 상세(Part B 2장), Spring AI·JWT 심화(refresh 재발급·OAuth2, 교안 대기 → 위 ⏸️)

## 의도적으로 단순화한 것

- **비밀번호 평문 저장**: Spring Security를 배우기 전이라 평문으로 둔다. 실무에서는 절대 금지(BCrypt 등으로 해싱). → **M12에서 `PasswordEncoder`로 해결 예정**
- **입력 검증**: Bean Validation은 교안 밖이므로 Service의 if 문과 DB 제약으로만 처리한다.
- **파일 저장**: 로컬 디스크에 저장한다. 실무에서는 S3 같은 스토리지를 쓰는 경우가 많다.

---

## 진행 기록

| 날짜 | 마일스톤 | 메모 |
|---|---|---|
| 2026-11-12 | 계획 수립 | 교안·샘플 확인, 프로젝트 주제 확정, 튜터 검토 반영(M2 통합, JSP 튜터 제공, 완료 기준, ImageStorage DI 과제, 작성자 권한 규칙) |
| 2026-11-12 | M1 진행 중 | 프로젝트 생성, 의존성·properties 완료, Auto Configuration 실험(A/B/C), 테이블 생성. **다음: 1-3 테스트 데이터 insert부터** (`STUDY_LOG.md` 📌 참고) |
| 2026-11-14 | M1 완료 | DB 권한 문제(ssafy → root로 전환), 테스트 데이터 스크립트화, rating CHECK 실험(3819), `MainController` + `index.jsp` + `ConfigTest`(HikariDataSource) 통과. **다음: M2 2-1 `Member` DTO부터** |
| 2026-11-15 | M2 진행 중 | 2-1: `Member` DTO, `MemberDao` + `member.xml`, `DBConfig`(`@MapperScan`), `MemberService`(중복 검사), DAO·Service 테스트(`@Transactional` rollback), `MemberController` + `signup.jsp` → 브라우저 가입 DB 저장 성공. **다음: 2-1 브라우저 최종 확인 3가지 → 2-2 리뷰 CRUD** (`STUDY_LOG.md` 📌 참고) |
| 2026-11-15 | M2 진행 중 (2차) | **2-1 완료** (브라우저 redirect·에러 메시지·F5 PRG 확인). 2-2: `Review` DTO 1차 통과, `ReviewDao` 선언 작성 중. **다음: `ReviewDao` 리뷰 → `review.xml` SQL 4개** (`STUDY_LOG.md` 📌 참고) |
| 2026-11-15 | M2 진행 중 (3차) | 2-2: `ReviewDao` 완성, `review.xml` CRUD 4개(4차 리뷰 통과), 리뷰 상세 join(`Review.writer` + `reviewDetailMap` + `<association>`, XML은 튜터 작성). **다음: 검색 개념 질문 → `ReviewSearch` + `<where>`/`<if>` → `ReviewDaoTest`** (`STUDY_LOG.md` 📌 참고) |
| 2026-11-16 | M2 진행 중 (4차) | 2-2 검색: 동적 SQL 개념(`<where>`/`<if>`, OGNL test, `&lt;` 엔티티), `ReviewSearch` DTO(`Integer minRating`) + `ReviewDao.search` 통과, `review.xml` search 3차까지(XML 파싱 통과). **다음: OR 괄호 수정 → `ReviewDaoTest`(초안 있음) 리뷰** (`STUDY_LOG.md` 📌 참고) |
| 2026-11-17 | **M2 완료** (일부 미룸) | 검색 괄호 `AND (... OR ...)` + `minRating != null`, `ReviewDaoTest`(튜터 작성: insert·괄호 회귀·조건 없음) 3개 통과, 셀프 퀴즈 답 정리, Notion 업로드. 미룸: join 상세·update·delete 테스트, `@MybatisTest`/`@Mapper` 실험. **다음: M3 로그인·로그아웃** |
| 2026-11-17 | M3 진행 중 | **3-1 로그인·로그아웃 완료**(세션 `loginUser`, `invalidate()`, `MemberServiceTest` 5개), **3-2-① 목록(검색)·② 상세(`@PathVariable`, 없는 번호 처리) 완료**. **다음: 3-2-③ 리뷰 작성 개념 Q(작성자 mno를 폼에서 받으면 안 되는 이유)부터** (`STUDY_LOG.md` 📌 참고) |
| 2026-11-18 | M3 진행 중 | **3-2-③ 리뷰 작성 완료**: 작성자 mno는 세션 `loginUser`에서(hidden 사칭 방지), GET·POST 로그인 검사, insert 후 `getRno()`(반환값은 행 수), PRG(F5 중복 없음), `addFlashAttribute("msg")` 완료 메시지. **다음: 3-2-④ 수정·삭제 + 3-3 작성자 본인 검사 개념 Q부터** |
| 2026-11-19 | M3 진행 중 | **3-3 작성자 검사 + 3-2-④ 삭제 완료**: `ForbiddenException`, Service private `checkReviewMno`(DB 원본으로 비교), `POST /review/{rno}/delete`(예외별 redirect, flash `msg`), 삭제 버튼 작성자에게만(`<c:if>` EL 문법), 브라우저 4가지(남의 글 차단) 통과. **다음: 단계 C 수정 리뷰부터** (교육생 초안 작성 중, `STUDY_LOG.md` 📌 참고) |
| 2026-11-22 | **M3 완료** | 데스크탑 이동 점검(JDK·STS Lombok·MySQL·`mvnw test`), 수정 초안 유실 → 단계 C 리뷰 수정 완료(`getReviewForEdit`, `GET·POST /{rno}/edit`, `edit.jsp`, 수정 링크, 🔴 `updateReview` 이중 호출 수정), 브라우저 5가지 통과, `ReviewDaoTest` selectDetail·update·delete·없는 번호 추가(16개 초록), 셀프 체크, Notion 업로드. 미룸: `@MybatisTest`/`@Mapper` 실험, `image_name` null(M5). **다음: M4 트랜잭션 + AOP** |
| 2026-11-22 | **M4 완료** | 4-1 탈퇴(`deleteByMno`·`delete`·`deleteAccount`, SQL 오타 `DELETE *`·`WHEN` 수정), 4-2 `MemberWithdrawTest`로 리뷰만 지워지는 실패 재현(튜터 수정 후 🔴), 4-3 `@Transactional` 한 줄로 🟢 + 정상 탈퇴 테스트·실험 `@Disabled`, `TransactionInterceptor` 소스 읽기, 4-4 `spring-boot-starter-aspectj` + `ExecutionTimeAspect`(Pointcut 오타로 매칭 0개 → 튜터 수정, try/finally), 4-5 Aspect·Interceptor·Filter 비교, 셀프 체크 2문제 정답, Notion 업로드. 남은 💅: `deleteAccount` TODO 주석. **다음: M5 `ImageStorage` DI** |
| 2026-11-22 | **M5 완료 — 맛집 노트 M1~M5 전체 완료** | M5부터 튜터가 교안을 먼저 설명. 5-1 `ImageStorage` DI(빈 2개 `NoUnique` → `@Profile` → 0개 `NoSuch` 실험, `ImageStorageTest`), 5-2 사진 업로드(multipart 설정, `enctype`, `MultipartFile`, `WebConfig.addResourceHandlers`, 수정 시 DB 원본 이름 유지로 M3 `image_name` null 버그 해결, `InvalidImageException`), 5-3 `MvcExceptionHandler` 403/500 + `error/404.jsp`, 5-4 `ReviewControllerTest`(`@WebMvcTest`) 2개. 전체 테스트 24개(1 Skipped). 셀프 체크는 튜터 설명. Notion 업로드. 남은 선택: `@MybatisTest`/`@Mapper` 실험, Interceptor 구현, 수정 폼·저장의 Forbidden catch 정리 |
| 2026-11-22 | 계획 추가 | Notion `[ Spring Framework ]`에 Interceptor·REST API 교안이 추가됨 → 교육생 결정으로 **M6 Interceptor**, **M7 REST API** 추가, 범위 밖 목록 조정, CLAUDE.md 교안 순서 갱신 (샘플 zip 없음) |
| 2026-11-22 | 계획 축소 | 교육생 시간 빠듯 → M6 약 1시간(Interceptor 등록·로그인 검사 제거만), M7 약 2시간(상세·작성 API + ProblemDetail 404 + jsonPath 테스트만, 나머지는 👀 설명) |
| 2026-11-22 | M6 보류 | Interceptor 개념(검문소 비유, Filter·AOP·ControllerAdvice와 위치 비교) 설명 후 교육생 결정으로 구현은 넘어감. 작성 중이던 `LoginCheckInterceptor`는 패턴이 어느 주소에도 안 걸려 동작 영향 없음. **다음: M7 REST API** |
| 2026-11-22 | **M7 완료 — 맛집 노트 실습 종료** (M6 보류) | `restcontroller/ReviewRestController`(`/api/review` 상세 GET 200·작성 POST 201, 세션 mno), `config/RestExceptionHandler`(`basePackageClasses` + `@Order`, 404·500 `ProblemDetail`), `ReviewRestControllerTest` 4개. 테스트가 Jackson 3 역직렬화 버그(`Cannot map null into type int`, POST 항상 500) 발견 → `spring.jackson.deserialization.fail-on-null-for-primitives=false`. 전체 테스트 28개(1 Skipped). Notion 업로드. 셀프 체크는 튜터 설명 |
| 2026-11-23 | M6 재개 | 교육생 요청으로 Interceptor 다시 시작. 로컬이 원격보다 11커밋 뒤처짐 → M3 초안 stash 후 pull. 개념 설명 다시(흐름 속 위치, 메서드 3개, preHandle false + 직접 응답, 등록·ANT 패턴, Filter·AOP·Service 검사 구분) + 6-1~6-3 명세 제시. `LoginCheckInterceptor` 작성 막 시작(미완성, 컴파일 에러 상태). **다음: 6-1 작성·리뷰** (`STUDY_LOG.md` 📌 참고) |
| 2026-11-24 | **M6 완료 — 맛집 노트 M1~M7 전체 완료** | 6-1 `LoginCheckInterceptor`(`getSession(false)` + `loginUser` 검사, redirect + false), 6-2 `WebConfig` 생성자 주입 + `addPathPatterns` 세 경로, 6-3 Controller 5곳 검사 제거. `MemberDaoTest.selectByEmailTest` DB 의존 → 직접 insert 후 조회로 수정(튜터 작성, 교육생 요청). 전체 테스트 28개(1 Skipped), 브라우저 5가지 통과. 질문: 세션 범위, `WebMvcConfigurer` 등록·여러 개, Interceptor 위치. Notion 업로드 |
| 2026-11-24 | 계획 추가 | 새 샘플 zip 5개 확인(FW_09 = Interceptor, FW_10 = REST, FW_11·13·14 = Security·JWT·AI). Notion 교안 Interceptor·REST API 재확인 → 교안에서 남은 부분으로 **M8 Interceptor 심화**(LoggingInterceptor·순서·ViewController), **M9 REST API 심화**(CRUD·403·`/api/v1/reviews`·Swagger·CORS·RestClient) 추가. Security·JWT·AI는 교안이 없어 ⏸️ 보류 |
| 2026-11-24 | 계획 추가 (2차) | Notion에 **Spring Security pt.1·pt.2, JWT** 교안 추가됨 → 교안·FW_11/FW_13 TODO 번호에 맞춰 **M10 Security 기초**(메모리 사용자·`SecurityFilterChain`·`LoginCheckInterceptor` 해제, TODO 02~06), **M11 DB 회원 Security 로그인**(role 컬럼·비밀번호 암호화·formLogin/logout·`@AuthenticationPrincipal`·sec 태그·Remember-Me, TODO 07~13), **M12 JWT**(`JWTUtil`·필터 3개·`/api/**` 전용 체인·CORS 이동, TODO 01~07) 추가. 보류는 Spring AI와 JWT 심화(TODO 08~17: refresh·OAuth2)만 |
| 2026-11-24 | M8 시작 (예외 처리 다시) | 교육생 요청으로 **M8 예외 처리 다시 보기** 추가, 아직 시작 안 한 마일스톤 번호를 하나씩 밈(Interceptor 심화 M9, REST 심화 M10, Security M11·M12, JWT M13, AI는 M14~). 튜터 세팅: Advice 2개를 TODO 뼈대로 비움, Service·Controller TODO 주석, 목표 테스트 4개 추가(🔴 6 / 🟢 4 확인), 안내서 `guides/M8_예외처리.md`. **다음: 8-1 관찰** |
| 2026-11-24 | 세션 종료 (M8 세팅만) | 커밋 `7f3ab76` 푸시(🔴 6개 포함, 의도됨). 커밋 직전 STS 열린 탭이 `MvcExceptionHandler`를 예전 코드 + `Excep//tion` 오타로 덮어씀 → 뼈대로 복구. `FW_11_sec_sol.zip`은 실제 API 키 때문에 gitignore(로컬만). **다음: 8-1 관찰부터** (`STUDY_LOG.md` M8 📌) |
| 2026-11-25 | M8 8-1 끝, 8-2 진행 중 | 8-1 예상 표 + 피드백. 8-2 `error/4xx.jsp` 작성(400·405 → 4xx.jsp, 404 → 404.jsp 확인). 질문: 상태 코드, BasicErrorController 위치(Boot 4 `spring-boot-webmvc`), EL, `/error` 두 번째 요청. `include-message=always` 실험 줄이 커밋에 남아 있음 → **다음: 4xx.jsp 15행 문구, curl 실험 후 줄 삭제 → 8-3** |
| 2026-11-27 | M8 8-2~8-7 (셀프 체크 7/14) | 8-2 curl 실험(devtools가 message·trace 노출) 후 줄 삭제. 8-3 MVC Advice 403·500, `@ResponseStatus` 빼기 → 200 확인. 8-4 `RecordNotFoundException` + 404. 8-5 catch 정리(InvalidImage만 남김). 8-6 REST Advice 404·403·415·500 (Exception 핸들러가 415를 500으로 삼키는 것 재현 → `HttpMediaTypeNotSupportedException` 핸들러). 테스트 34개 통과. 흐름 시각화 `guides/M8_예외처리_흐름.html`. 일부 코드는 교육생 요청으로 튜터 작성. **다음: 셀프 체크 8~14 → M8 Notion 업로드 → M9** |
| 2026-11-27 | **M8 완료** | 셀프 체크 8~14는 교육생 요청으로 튜터 정리(Accept 헤더, catch 삭제, 화면/API Advice 분리, `basePackageClasses` = 패키지 전체, RuntimeException 상속 이유, 고칠 수 있는 오류는 Controller catch, 415→500을 그림 ②로). 질문: BasicErrorController 자동 설정. Notion 업로드. ⏸️ `/review/abc` 400→500은 알려진 한계로 남김. **다음: M9 Interceptor 심화** |
