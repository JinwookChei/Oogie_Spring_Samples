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

### M6. Interceptor 🔍 · 약 1시간 (시간 빠듯 → 핵심만) — ⏸️ 보류 (2026-11-22, 개념 설명까지 완료)
교안: Interceptor (Notion) · 샘플: 없음 (교안 TODO 06·07 기준)

- [ ] 🔍 `LoginCheckInterceptor implements HandlerInterceptor`: `preHandle`에서 세션 `loginUser` 확인 → 없으면 `sendRedirect` + `return false`
- [ ] 🔍 `WebConfig.addInterceptors`에 등록, `addPathPatterns`로 리뷰 작성·수정·삭제만
- [ ] 🔍 Controller 5곳의 로그인 검사 제거 → 전체 테스트 통과 확인 (`ReviewControllerTest` ② 비로그인 작성 테스트가 그대로 통과해야 함)
- 👀 (설명만) `postHandle`·`afterCompletion`, 여러 Interceptor 순서, `addViewControllers`, Filter vs Interceptor vs AOP

**셀프 체크**: `preHandle`이 false를 반환하면 무엇을 직접 해 줘야 할까?

**✅ 완료 기준**: Controller에 로그인 검사 코드가 없는데, 비로그인으로 작성·수정·삭제에 접근하면 로그인 화면으로 간다. 전체 테스트 통과.

---

### M7. REST API · 약 2시간 (시간 빠듯 → 핵심만)
교안: REST API (Notion) · 샘플: 없음 (교안 TODO 02·03 기준)

- [ ] 🔍 `ReviewRestController`(`@RestController`, `/api/reviews`): 상세 `GET /{rno}` + 작성 `POST`(`@RequestBody`) → `ResponseEntity`(작성은 201 Created)
- [ ] 🔍 `@RestControllerAdvice`(API 패키지만) + `ProblemDetail`: 없는 리뷰 → 404 JSON
- [ ] 🔍 MockMvc `jsonPath` 테스트 1~2개
- 👀 (설명만) REST URL 설계 규칙, PUT vs PATCH, API Versioning, Swagger, CORS·SOP, `RestClient`

**셀프 체크**: 화면용 `@ControllerAdvice`와 API용 `@RestControllerAdvice`를 왜 나눌까?

**✅ 완료 기준**: `/api/reviews`로 리뷰를 JSON으로 조회·작성할 수 있고, 없는 리뷰는 JSON(`ProblemDetail`) 404. `jsonPath` 테스트 통과.

---

## 범위 밖 (큰 그림만)

Legacy 초기화(web.xml, Root/Servlet 컨텍스트), FactoryBean·Auto Configuration 직접 만들기, Filter 구현, REQUIRED 외의 전파 속성·isolation, `@Async`·`@Cacheable`, Resilience(`@ConcurrencyLimit`, `@Retryable`), Base64·Ajax 업로드, 페이징, Swagger·CORS·RestClient 직접 구성(M7에서 👀), Spring Security

## 의도적으로 단순화한 것

- **비밀번호 평문 저장**: Spring Security를 배우기 전이라 평문으로 둔다. 실무에서는 절대 금지(BCrypt 등으로 해싱).
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
