# PLAN.md — 실습 프로젝트 「맛집 노트」

회원이 가입해서 맛집 리뷰(사진 1장 + 별점 + 내용)를 쓰고, 검색하고, 탈퇴하는 작은 웹 서비스.

**목표**: 시간을 적게 들이면서 Spring 전체 그림을 이해하고, 실무 뼈대(요청 → 바인딩 → 서비스 → 트랜잭션 → SQL)는 직접 손으로 짜 본다.

**예상 소요**: 약 15~25시간 (순수 작업 13~20시간 + 환경 문제 여유분)

---

## 진행 규칙

| 표시 | 의미 | 누가 작성 |
|---|---|---|
| 🔍 | 세세하게 | 교육생이 직접 작성 → 튜터가 한 줄씩 리뷰 |
| 👀 | 큰 그림 | 개념만 짚고, 뼈대는 튜터가 제공 |

- **JSP 화면은 전부 튜터가 뼈대를 제공**한다. JSP 작성은 Spring 학습이 아니므로 시간을 쓰지 않는다. 교육생은 Java 코드(Controller · Service · DAO · mapper · 테스트)에 집중한다.
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

### M1. 프로젝트 세팅 👀 · 1~2시간
교안: SpringBoot, Logging · 샘플: FW_05 `pom.xml`, FW_07 `application.properties`

- [ ] STS `File > New > Spring Starter Project`로 프로젝트 생성 (Maven, Java 21 / 의존성: Spring Web, MyBatis, MySQL Driver, Lombok, DevTools)
- [ ] JSP 의존성 수동 추가 (`tomcat-embed-jasper`, JSTL 2개) + `spring.mvc.view.prefix/suffix`
- [ ] `application.properties`: DB 접속, HikariCP, MyBatis, 로깅 레벨
- [ ] `schema.sql`로 DB와 테이블 생성
- [ ] 메인 화면 Controller 하나로 `index.jsp` 확인

**알고 넘어갈 것**: Starter, Auto Configuration, 내장 Tomcat, `@SpringBootApplication` = 설정 + 자동 설정 + 컴포넌트 스캔

**✅ 완료 기준**: 앱을 실행해 브라우저에서 `index.jsp`가 보이고, `DataSource` 주입 테스트(FW_07 `ConfigTest` 방식)가 통과한다.

---

### M2. 회원가입 관통 + MyBatis CRUD 🔍 · 4~6시간
교안: Framework, DI, Bean 등록, Spring & MyBatis · 샘플: FW_07 `member.xml`, `address.xml`

**2-1. 회원가입 한 줄 관통**
- [ ] `Member` DTO
- [ ] `MemberDao` + `member.xml`: insert(PK 받아오기), selectByEmail
- [ ] `MemberService` (interface + impl), 생성자 주입 (`final` + `@RequiredArgsConstructor`)
- [ ] `MemberController`: 가입 폼 → 가입 처리 → redirect (JSP는 튜터 제공)
- [ ] 브라우저에서 가입 → DB에 행이 생긴 것 확인

**2-2. 리뷰 CRUD**
- [ ] `Review` DTO, `ReviewDao` + `review.xml`: insert / select / delete / update
- [ ] 리뷰 상세: review + 작성자 join → `resultMap` + `<association>`
- [ ] 리뷰 검색: 키워드, 최소 별점 → `<where>` + `<if>`
- [ ] `@MybatisTest`로 DAO 테스트 (`@Transactional` 롤백 확인)

**셀프 체크**: `@Component`만 붙이면 빈이 될까? `@Mapper` interface는 구현 클래스가 없는데 왜 주입될까? `#{}` 대신 `${}`를 쓰면 무슨 일이 생길까?

**알고 넘어갈 것**: IoC, 싱글톤 빈은 상태를 갖지 않는다, 스테레오타입(`@Controller` / `@Service` / `@Mapper`), namespace + id 연결, DAO는 interface만

**✅ 완료 기준**: 브라우저에서 회원가입이 되고, `ReviewDaoTest`(insert · join 상세 · 동적 검색 · delete)가 전부 통과한다.

---

### M3. MVC 요청 처리 🔍 · 3~5시간
교안: Spring MVC pt.1, pt.2 (Part 1·2) · 샘플: FW_05 `SimpleController`

- [ ] 로그인·로그아웃 (세션에 `loginUser`)
- [ ] 리뷰 목록(검색 포함)·상세·작성·수정·삭제 Controller
- [ ] `@RequestParam` / `@ModelAttribute` / `@PathVariable` 바인딩
- [ ] 등록·수정·삭제 후 **PRG** + `addFlashAttribute`로 "완료" 메시지
- [ ] 작성자 본인 검사를 **Service**에 구현 → 위반 시 커스텀 예외
- [ ] 로그인 검사를 **각 Controller 메서드에 직접** 넣어 보기 (중복이 생기는 걸 일부러 체감 → M4 AOP의 동기)

**셀프 체크**: 왜 등록 후에 forward가 아니라 redirect일까? redirect 후 `Model`에 넣은 값은 왜 사라질까? 권한 검사는 왜 Controller가 아니라 Service에 둘까?

**알고 넘어갈 것**: DispatcherServlet → HandlerMapping → HandlerAdapter → ViewResolver 흐름, forward vs redirect, 400·404·405가 나는 이유

**✅ 완료 기준**: 브라우저에서 로그인 → 리뷰 작성 → 검색 → 수정 → 삭제가 되고, 다른 회원의 리뷰는 삭제가 막힌다.

---

### M4. 트랜잭션 + AOP 🔍 · 2~3시간
교안: Spring Transaction, 스프링 AOP · 샘플: FW_07 `BasicMemberService.delete`, FW_04 `LoggingAspect`

- [ ] 회원 탈퇴 = 리뷰 전체 삭제 + 회원 삭제. 중간에 예외를 일부러 넣어 **트랜잭션 없이 실패하는 것 재현**
- [ ] `@Transactional`로 해결하고 테스트로 확인
- [ ] Service 계층 실행 시간 측정 `@Around` Aspect
- [ ] M3에서 중복된 로그인 검사를 보며 "한 곳으로 모으는 방법" 토론 (Interceptor는 👀 설명만)

**셀프 체크**: 같은 클래스 안에서 호출하면 왜 `@Transactional`이 안 먹을까? checked 예외가 나면 롤백될까?

**알고 넘어갈 것**: 프록시, RuntimeException만 롤백, 전파 기본값 REQUIRED, Pointcut 표현식 읽기

**✅ 완료 기준**: 탈퇴 테스트가 `@Transactional` 없이는 실패(리뷰만 지워짐)하고, 붙이면 통과한다. 로그에 Service 메서드 실행 시간이 찍힌다.

---

### M5. 마무리 · 3~4시간
교안: Spring MVC pt.2 (Part 1·3·4), DI · 샘플: FW_05 `MvcConfigurer`, `MvcExceptionHandler`, `ControllerTest`

- [ ] 🔍 **`ImageStorage` 인터페이스로 DI 체감**
  - `LocalImageStorage`(디스크 저장, UUID 파일명)와 `FakeImageStorage`(저장하지 않고 이름만 반환) 두 구현체
  - 같은 타입 빈 2개 → 에러 확인 → `@Primary` 또는 `@Profile`로 교체
  - 리뷰 작성 시 사진 업로드 (`MultipartFile`), `addResourceHandlers`로 화면에 표시
- [ ] 👀 `@ControllerAdvice` + `@ExceptionHandler`: 권한 예외 → 403 페이지, 그 외 → 500 페이지, `error/404.jsp`
- [ ] 👀 `@WebMvcTest` + `MockMvc` + `@MockitoBean`으로 리뷰 Controller 테스트 1~2개

**셀프 체크**: `ReviewService`는 `ImageStorage` 구현체가 바뀌어도 왜 코드를 고칠 필요가 없을까? 404는 왜 `@ControllerAdvice`가 아니라 BasicErrorController가 처리할까?

**알고 넘어갈 것**: DIP·OCP와 DI의 관계, PSA, ControllerAdvice(Controller 예외) vs BasicErrorController(404·405), Mock 환경이 필요한 이유

**✅ 완료 기준**: 사진이 있는 리뷰가 화면에 보이고, `@Profile`을 바꾸면 `FakeImageStorage`로 동작한다. 남의 리뷰 삭제 시 403 페이지가 뜨고, MockMvc 테스트가 통과한다.

---

## 범위 밖 (큰 그림만)

Legacy 초기화(web.xml, Root/Servlet 컨텍스트), FactoryBean·Auto Configuration 직접 만들기, Interceptor·Filter 구현, REQUIRED 외의 전파 속성·isolation, `@Async`·`@Cacheable`, Resilience(`@ConcurrencyLimit`, `@Retryable`), Base64·Ajax 업로드, `@RestControllerAdvice`·ProblemDetail, 페이징

## 의도적으로 단순화한 것

- **비밀번호 평문 저장**: Spring Security를 배우기 전이라 평문으로 둔다. 실무에서는 절대 금지(BCrypt 등으로 해싱).
- **입력 검증**: Bean Validation은 교안 밖이므로 Service의 if 문과 DB 제약으로만 처리한다.
- **파일 저장**: 로컬 디스크에 저장한다. 실무에서는 S3 같은 스토리지를 쓰는 경우가 많다.

---

## 진행 기록

| 날짜 | 마일스톤 | 메모 |
|---|---|---|
| 2026-11-12 | 계획 수립 | 교안·샘플 확인, 프로젝트 주제 확정, 튜터 검토 반영(M2 통합, JSP 튜터 제공, 완료 기준, ImageStorage DI 과제, 작성자 권한 규칙) |
