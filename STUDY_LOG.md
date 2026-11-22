# STUDY_LOG.md — 맛집 노트 학습 기록

실습하면서 직접 한 작업, 튜터와 대화하며 배운 개념, 내가 답한 질문과 피드백, 겪은 문제와 해결법을 챕터(마일스톤)별로 기록한다.
챕터가 끝나면 이 기록을 Notion에 옮긴다.

---

## M1. 프로젝트 세팅 — ✅ 완료 (2026-11-14) · [Notion](https://app.notion.com/p/4186412b0acd81698c28ef73831807a6)

### ✅ 한 일

- STS 5.0.1에서 `File > New > Spring Starter Project`로 `MatzipNote` 생성 (Maven, Java 21, Boot 4.1.2, 패키지 `com.study.matzip`)
- 의존성: Spring Web, DevTools, MySQL Driver, Lombok → 이후 MyBatis, JSP(Jasper + JSTL 2개), MyBatis Test를 `pom.xml`에 직접 추가
- 실험: MyBatis를 넣고 / 빼고 실행해서 Auto Configuration 동작 확인

### 📚 배운 개념

**1. 내장 Tomcat — Tomcat은 설치하는 게 아니라 의존성이다**
- `spring-boot-starter-webmvc`(Spring Web) 안에 `spring-boot-starter-tomcat`이 들어 있다.
- Boot는 `main()` → `SpringApplication.run()`이 먼저 실행되고, **Spring이 Tomcat을 직접 만들어 켠 뒤** DispatcherServlet을 등록한다.
- 예전(Servlet/JSP 실습): Tomcat이 먼저 뜨고 내 앱을 올림 → **WAS가 주인**. Boot: **Spring이 주인** (주도권 역전).

**2. 의존성 버전 관리 — 왜 MyBatis만 버전을 적을까?**
- `<parent>` = 자바의 `extends`. 내 `pom.xml`엔 안 보여도 부모 설정을 전부 물려받는다.
- 상속 구조: 내 pom → `spring-boot-starter-parent` → `spring-boot-dependencies` (⭐ 서로 맞춰 본 버전 목록, BOM)
- 목록에 있으면 버전 생략 가능 (Lombok 1.18.48, MySQL 9.7.0, Tomcat 11.0.27). MyBatis 스타터는 MyBatis 팀이 따로 배포해서 목록에 없음 → **직접 버전(4.0.1) 지정**.
- STS에서 확인: 부모 이름 `Ctrl + 클릭` / POM 편집기의 **Effective POM** 탭(상속 합친 최종본) / **Dependency Hierarchy** 탭(누가 누구를 데려왔나)

**3. Maven 저장소 — 라이브러리는 어디서 오나**
- `pom.xml`에 좌표(groupId:artifactId:version)를 적고 저장하면 Maven이 알아서:
  내 창고(`C:\Users\SSAFY\.m2\repository`) 확인 → 없으면 **Maven Central**에서 다운로드 → 내 창고에 저장
- 좌표 = 폴더 경로 (`org.mybatis.spring.boot` → `org/mybatis/spring/boot/`)
- `.m2` 창고는 컴퓨터 전체에 하나 → 한 번 받으면 모든 프로젝트가 공유
- 좌표 찾기: mvnrepository.com
- **전이 의존성**: starter 하나가 필요한 라이브러리(autoconfigure, mybatis, HikariCP 등)를 줄줄이 데려온다.
- 다운로드가 실패하면 `.lastUpdated` 실패 기록이 남아 재시도를 안 함 → `Alt + F5` + **Force Update** 또는 해당 폴더 삭제

**4. JSP 의존성**
- 내장 Tomcat은 기본적으로 Servlet만 실행 → JSP를 Servlet으로 변환하는 엔진 **Jasper**(`tomcat-embed-jasper`)를 추가해야 함
- JSTL은 스펙(`jakarta.servlet.jsp.jstl-api`) + 구현체(`org.glassfish.web:jakarta.servlet.jsp.jstl`) 두 개

**5. Auto Configuration — 의존성만 넣었는데 DB를 찾는 이유**

| 실험 | MyBatis | DB url 설정 | 결과 |
|---|---|---|---|
| A | 있음 | 없음 | ❌ `Failed to configure a DataSource: 'url' attribute is not specified` |
| B | 없음 | 없음 | ✅ 정상 실행 |
| C | 있음 | 있음 | ✅ 정상 실행 (`Started MatzipNoteApplicationTests in 1.3s`) — 아직 `matzip` DB가 없어도 뜸: HikariCP는 실제로 연결이 필요할 때까지 접속을 미룸 |

- MyBatis가 HikariCP·JDBC를 데려옴 → Boot가 클래스패스를 보고 "DB 쓰려는구나" → DataSource 빈 자동 생성 시도 → url이 없어서 실패
- 내부 조건: `@ConditionalOnClass(DataSource.class ...)` — 클래스가 있을 때만 설정 동작
- **코드는 그대로인데 `pom.xml` 내용만으로 Spring 동작이 바뀐다** = Auto Configuration

**6. DataSource**
- "DB 연결(Connection)을 달라고 하면 내주는 객체"의 **Java 표준 인터페이스** (`javax.sql.DataSource`)
- 예전 `DBUtil.getConnection()`(매번 새 연결, 느림)을 대신함 → 커넥션 풀에서 **빌리고(getConnection) 반납(close)**
- 구현체: **HikariCP** (`HikariDataSource`, Boot 기본). Boot가 `application.properties`를 읽어 **빈으로 등록**, MyBatis가 주입받아 사용
- 우리 코드는 인터페이스만 앎 → 구현체가 바뀌어도 그대로 (**PSA**)

**7. `application.properties` 주의**
- 값 뒤에 주석 금지: `password=ssafy # 비번` → 비밀번호가 `ssafy # 비번`이 됨. 주석은 윗줄에.
- JDBC url 구조: `jdbc:mysql://서버주소:포트/DB이름?옵션=값&옵션=값` — `?` 뒤는 MySQL 드라이버(Connector/J) 옵션
- `serverTimezone=Asia/Seoul`: MySQL 서버의 시간대를 드라이버에 알려 줘서 날짜·시간을 올바르게 변환. 틀리면 `created_at`이 9시간 어긋나는 버그. 샘플은 `UTC`였지만 우리는 시간을 화면에 보여 주므로 로컬(KST)에 맞춤. 옵션 목록은 Connector/J "Configuration Properties" 문서
  - 왜 따로 알려 주나: 서버가 스스로 말하는 시간대 이름이 애매함(한국어 Windows는 `KST`/`대한민국 표준시` → Java가 해석 못 함, 예전 `'KST' is unrecognized` 에러의 원인). 클라우드면 서버와 앱 시간대가 원래 다름 → `Asia/Seoul` 같은 IANA 표준 이름으로 확정
  - 왜 url에 붙이나: Spring 설정이 아니라 **MySQL 드라이버 전용 옵션**이라서. Spring은 url을 해석하지 않고 드라이버에 그대로 넘김. 다른 방법: `spring.datasource.hikari.data-source-properties.옵션명` (HikariCP를 거쳐 드라이버에 전달)
- DB url의 DB 이름과 `schema.sql`의 `CREATE DATABASE` 이름은 반드시 같아야 함

**8. 설정 키는 어디서 찾나 (강의 자료 없이)**
1. **STS 자동 완성**: `spring.datasource.` 입력 후 `Ctrl + Space` → 키 목록, 마우스를 올리면 설명·기본값. 라이브러리 jar 안의 `META-INF/spring-configuration-metadata.json`을 STS가 읽어서 보여 줌 (MyBatis jar에도 있음)
2. **공식 문서**: Spring Boot 부록 "Common Application Properties" / MyBatis-Spring-Boot-Starter 문서의 Configuration 표 / MySQL Connector/J의 Connection URL Syntax. 프로젝트의 `HELP.md`에 내 버전(4.1.2)에 맞는 문서 링크가 있음
3. **Properties 클래스 직접 열기** (`Ctrl + Shift + T`): `DataSourceProperties`, `MybatisProperties`, `WebMvcProperties`. `@ConfigurationProperties("접두사")` + 필드 이름 = 키 이름 → 가장 정확한 정답지
4. **가이드·검색**: spring.io/guides. 블로그 글은 Boot 버전을 꼭 확인
- 실무 순서: 자동 완성 → 공식 문서 → Properties 클래스

**9. DB 스크립트 위치와 CHECK 제약**
- `schema.sql`은 `src/main/resources/scripts/`에 둔다. `resources/` 바로 아래의 `schema.sql`은 Spring Boot가 시작할 때 **자동 실행할 수 있는** 특별한 이름(SQL 초기화 Auto Configuration, 기본은 내장 DB일 때만) → 직접 실행할 스크립트는 하위 폴더로 분리
- 스크립트는 MySQL Workbench에서 직접 실행. 프로젝트에 두는 이유 = DB 구조를 코드와 함께 기록
- `CHECK (조건식)`: 조건이 참인 값만 허용하는 제약. NULL은 막지 못하므로 `NOT NULL`과 따로 건다. MySQL **8.0.16 미만은 검사하지 않음** (`SELECT VERSION();`으로 확인)
- 검증은 Service(친절한 메시지) + DB 제약(마지막 방어선) 둘 다
- FK에 `ON DELETE CASCADE`를 일부러 붙이지 않음 → M4 트랜잭션 실험을 위해
- Workbench에서 테이블 생성 완료 (MySQL 8.0.46 → CHECK 실제로 동작)

**10. 컬럼 길이 설계**
- MySQL `varchar(n)`의 n은 **글자 수** (utf8mb4에서 한글도 1글자 = 1)
- 첫 설계의 실수: email 20, title 10, content 20, image_name 20, password 10 → 실제 값이 안 들어감
  - 이메일 21자, 제목 15자, 본문 한두 문장, UUID 파일명(약 40자)에서 저장 실패
  - password는 나중에 해싱하면 60자 이상 → 샘플은 100으로 넉넉하게
- 긴 본문은 varchar 대신 긴 글 전용 타입 고려
- 길이를 고칠 땐 `ALTER TABLE`보다 **스크립트를 고치고 다시 실행** → 스크립트 맨 앞에 기존 DB를 지우는 줄이 필요 (재실행 가능한 스크립트)
- 데이터 insert 순서는 FK 때문에 **부모(member) → 자식(review)**
- 2차 수정: email 40, password 100, content → **TEXT**(긴 글 전용 타입), image_name 50 ✅ / title 10은 아직 짧음
- STS 편집기 탭 제목 앞의 `*` = 저장 안 됨. 스크립트 파일이 비어 있으면 DB를 다시 만들 때 수정 내용이 사라짐
- 3차 수정 ✅: `schema.sql` 저장 완료, title 20. 재실행 방식은 샘플(`DROP DATABASE`)과 다르게 직접 설계 → `CREATE DATABASE IF NOT EXISTS` + `DROP TABLE IF EXISTS`를 **자식(review) → 부모(member) 순서**로 (FK 때문에 부모를 먼저 못 지움)
- CHECK는 `BETWEEN` 대신 `1 <= rating AND rating <= 5`로 작성 → 같은 동작
- 테스트 데이터 insert SQL은 **튜터가 작성** (2026-11-14, 내가 요청). 회원 1명 + 리뷰 2개(하나는 `image_name` 없이). 포인트: 컬럼 목록에서 AUTO_INCREMENT·DEFAULT·NULL 허용 컬럼은 뺄 수 있다 / 컬럼 이름을 생략하면 모든 값을 테이블 순서대로 써야 해서 깨지기 쉽다 → 컬럼 이름을 적는 습관
- 재실행 문제: INSERT 부분만 Workbench에서 다시 실행 → 리뷰가 5개로 중복됨 (회원은 email UNIQUE 때문에 중복 안 됨). 해결 = INSERT를 **`schema.sql` 안에 넣고 항상 스크립트 전체를 실행** → DROP → CREATE → INSERT가 매번 같은 결과(회원 1, 리뷰 2). 튜터가 내 요청으로 파일에 추가하고 두 번 연속 실행해 검증함
- 테스트 데이터를 스크립트에 넣을 때: 테이블을 새로 만들면 AUTO_INCREMENT가 1부터 다시 시작 → 리뷰의 `mno` 값 결정에 영향. 일부러 실패시키는 insert(rating 6)는 스크립트에 넣지 않음 (실행이 멈춤)
- **rating 6 실험 결과**: `Error (3819): Check constraint 'review_chk_1' is violated.` → 행은 안 들어감(리뷰 2개 유지)
  - 1064 = 문법 에러, 3819 = 문법은 맞지만 값이 CHECK 규칙 위반
  - 이름을 안 붙이면 MySQL이 `테이블명_chk_번호`로 자동 이름 → 실무에선 `CONSTRAINT chk_review_rating CHECK (...)`처럼 의미 있는 이름
  - Java에선 스프링 예외(`DataIntegrityViolationException` 등)로 올라옴 → 사용자에겐 Service if 문으로 친절하게, DB 제약은 마지막 방어선
- SQL 문자열은 **작은따옴표**가 표준 (큰따옴표는 DB 설정에 따라 컬럼 이름으로 해석됨). 값 목록 키워드는 `VALUES`
- 실험 설계: **확인하고 싶은 조건 하나만** 틀리게 (rating만 6, mno는 실제 있는 1) → FK 에러가 아닌 CHECK 에러만 확인

**11. 로그 레벨 — 언제 무엇을 쓰나** (`trace` < `debug` < `info` < `warn` < `error`)

| 레벨 | 언제 | 맛집 노트 예 |
|---|---|---|
| `trace` | 아주 잘게 흐름 추적 (거의 안 씀) | 반복문 안의 값 하나하나 |
| `debug` | 개발 중 확인용: 들어온 값, 분기, 중간 결과 | "리뷰 검색 조건: keyword=냉면" |
| `info` | 운영에서도 남길 정상 이벤트 | "회원 가입 완료: mno=3", 서버 시작 |
| `warn` | 지금은 괜찮지만 확인이 필요한 상황 | 남의 리뷰 삭제 시도, 별점 범위 밖 입력 |
| `error` | 처리 실패, 사람이 봐야 하는 문제 | DB 연결 실패, 파일 저장 실패 (예외 객체도 함께) |

- 설정한 레벨 **이상만** 찍힘: `debug`로 두면 debug·info·warn·error 출력, trace는 안 찍힘
- 개발은 debug, 운영은 info가 보통. 코드는 그대로 두고 **설정만 바꿔서** 양을 조절
- 비밀번호 같은 민감 정보는 어떤 레벨에서도 찍지 않는다

**12. 메인 화면 요청 흐름과 DataSource 주입 테스트**
- `GET /` → DispatcherServlet → `@Controller`의 `@GetMapping("/")` 메서드 → 뷰 이름 `"index"` → ViewResolver(prefix + 이름 + suffix) → `/WEB-INF/views/index.jsp`
- `WEB-INF` 안의 JSP는 브라우저가 URL로 직접 못 연다 → 반드시 Controller를 거침
- JSP 인코딩: `pageEncoding`(JSP 파일을 읽을 때) + `contentType`(브라우저로 응답할 때) 둘 다 UTF-8
- 컴포넌트 스캔: `@SpringBootApplication`이 있는 패키지와 **하위 패키지**만 스캔 → `com.study.matzip.controller`는 자동으로 잡힘
- `ConfigTest`: `@SpringBootTest` + `@Autowired DataSource` → 실제 클래스 `com.zaxxer.hikari.HikariDataSource`. 내가 `new`하지 않음 = Auto Configuration이 만들고 컨테이너가 주입 (DI), 인터페이스로 받으니 구현체가 바뀌어도 코드 그대로 (PSA)
- `DataSource`는 `javax.sql` (JDK 표준이라 jakarta로 안 바뀜)
- **지연 연결**: 빈 생성 ≠ DB 접속. 실제 접속은 `getConnection()`이 처음 호출될 때 → "앱이 떴다 ≠ DB가 정상이다". DB 에러는 첫 DB 사용 시점에 드러난다

**13. `@SpringBootApplication` = 애너테이션 3개의 묶음** (`Ctrl + 클릭`으로 직접 확인)

| 애너테이션 | 기능 | M1에서 겪은 일 |
|---|---|---|
| `@SpringBootConfiguration` | 이 클래스는 **설정 클래스** (`@Configuration`과 같음 → 안에 `@Bean` 메서드를 둘 수 있음) | DI 교안의 `@Configuration` + `@Bean` |
| `@EnableAutoConfiguration` | 클래스패스를 보고 **필요한 빈을 자동으로 등록** (`@ConditionalOnClass` 등 조건 검사) | MyBatis만 넣었는데 DataSource를 찾음 (개념 5), `HikariDataSource` 자동 생성 |
| `@ComponentScan` | 이 클래스의 **패키지와 하위 패키지**에서 `@Component`류(`@Controller`, `@Service`...)를 찾아 빈으로 등록 | `MainController`가 자동으로 잡힘, 바깥 패키지면 404 |

- 애너테이션은 스스로 실행되지 않는 **표시**. 실제로 컨테이너를 띄우는 건 `main()` 안의 `SpringApplication.run()`
- **자동 설정이 빈을 만드는 사슬** (질문: `@AutoConfiguration` 때문에 빈이 등록된 건가?)
  1. `@EnableAutoConfiguration` → 각 jar 안의 목록 파일 `META-INF/spring/...AutoConfiguration.imports`를 읽음 (스위치)
  2. 목록에 있는 클래스들(예: `DataSourceAutoConfiguration`)에 붙은 것이 `@AutoConfiguration` = "나는 **자동 설정용 설정 클래스**" (`@Configuration`의 일종) (이름표)
  3. `@ConditionalOnClass` 등 **조건**이 맞으면 → 그 안의 `@Bean` 메서드가 실행돼 빈 생성 (조건 + 실제 생성)
  - 의존성의 클래스가 **전부** 빈이 되는 게 아님. 자동 설정 클래스가 `@Bean`으로 만들기로 정한 것만 (예: `HikariDataSource`는 빈, Hikari의 다른 내부 클래스는 아님)
  - 직접 만들기는 범위 밖, 구조만 이해

**10. AND/OR 우선순위와 괄호, 회귀 테스트**
- SQL에서 `AND`가 `OR`보다 먼저 묶인다 (× 가 + 보다 먼저인 것과 같음): `A AND B OR C` = `(A AND B) OR C`
- 검색에서 괄호가 없으면 → **별점 4 미만 + 제목에만 키워드**인 리뷰가 섞인다 (C 하나만 참이어도 통과)
- 괄호 위치: 연결어 `AND`는 괄호 **밖 맨 앞**(그래야 `<where>`가 지울 수 있음), 괄호는 묶을 두 조건만. `(AND ...`로 쓰면 `<where>`가 `(`로 시작해 AND를 못 지움 → 문법 에러
- DEBUG 로그의 `Preparing:`(만들어진 SQL) / `Parameters:`(`?`에 들어간 값)로 동적 SQL을 확인한다
- **회귀 테스트**: 고친 버그가 다시 생기면 빨간불이 켜지는 테스트. "괄호가 없을 때만 결과에 섞이는" 함정 데이터를 넣고, 결과에 **없어야 한다**고 검증 (`anyMatch` → `assertFalse`, `allMatch`). 로그만 찍는 테스트는 항상 초록
- `@BeforeEach`도 `@Transactional` 테스트와 **같은 트랜잭션** → 함께 rollback. 테스트가 기존 DB 데이터에 기대지 않게 필요한 데이터를 직접 만든다

**7. redirect 너머로 값 전달하기 (`RedirectAttributes`) — 샘플 FW_05 `SimpleController` TODO 10-2**
- Controller 파라미터에 `RedirectAttributes`를 받아 사용
- `addFlashAttribute("msg", 값)`: 값을 **세션에 잠깐** 맡겨 둠 → redirect된 **다음 요청 한 번**의 Model로 옮겨 줌 → 바로 삭제. 주소창에 안 보임. F5 하면 사라짐 = "한 번만 보여 줄 알림"에 딱 맞음
- `addAttribute("이름", 값)`(RedirectAttributes의 것): 주소 뒤 **쿼리 스트링**(`?byParam=dirty`)으로 붙음 → 주소창에 노출, 계속 남음
- 비교: `model.addAttribute` = redirect하면 사라짐 / `session.setAttribute` = 지울 때까지 계속 남음(알림이 계속 뜸) / flash = 딱 한 번
- JSP에서는 평소처럼 `${msg}` (다음 요청의 Model에 들어와 있으므로)

### 🙋 내 답변과 피드백

| 질문 | 내 답 | 피드백 |
|---|---|---|
| 어떤 의존성이 Tomcat을 데려오고, 왜 설치가 필요 없나? | Spring Web에 내장 Tomcat이 들어 있어 | ✅ 정답. 추가로 "Spring이 main()에서 Tomcat을 직접 만들어 켠다(주도권 역전)"까지 |
| 왜 MyBatis만 버전을 적어야 하나? | MyBatis는 Spring Boot에 없기 때문 | 🟡 방향은 맞음. 정확히는 "Boot의 **버전 관리 목록(BOM)**에 없기 때문". Lombok·MySQL도 Spring이 만든 건 아니지만 목록에 있어서 생략 가능 |
| 실험 C에서 `matzip` DB가 없는데 앱이 뜬 이유? | (1차) 빈 만드는 시점과 접속 시점이 다르다 → (2차) `getConnection()` 할 때까지 접속을 미루고, 실험 C에는 호출한 코드가 없어서 | ✅ 정답. 빈 생성 ≠ DB 접속 (지연 연결). DB 문제는 앱 시작이 아니라 **첫 DB 사용 시점**에 드러난다 |
| 메인 화면 Controller 로그 레벨은? | debug | ✅ 개발할 때만 필요한 로그 |
| 1-4 코드 리뷰 (`MainController`, `index.jsp`, `ConfigTest`) | 직접 작성 | ✅ 전부 통과. 💅 `Ctrl + Shift + F`로 포맷 정리(괄호 앞 공백, 빈 줄) |
| rating 6 insert 직접 작성 | `items(...)`, 큰따옴표, `created_at`+`Now()` | 🟡 `items` → `VALUES`, 문자열은 작은따옴표, `created_at`은 DEFAULT라 생략 가능. 👍 컬럼 명시 + 조건 하나만 틀리게 설계 |
| 셀프 퀴즈 1: `@SpringBootApplication`의 세 역할? | main 함수 실행, ?, ? | ❌ → 🟡 main 실행은 `SpringApplication.run()`의 일. 재도전: `Ctrl + 클릭`으로 직접 열어 세 애너테이션(`@SpringBootConfiguration`, `@EnableAutoConfiguration`, `@ComponentScan`)을 찾음 👍 → 각 기능은 개념 13 |
| 셀프 퀴즈 2: Controller를 `com.study.controller`에 두면? | 404. 메인 클래스 패키지의 하위만 스캔하기 때문 | ✅ 정답. 메인 클래스 패키지는 `com.study.matzip` (답에 `com.study.controller.matzip`으로 오타). 스캔 안 됨 → 빈 없음 → 매핑 없음 → 404 |
| 셀프 퀴즈 3: 왜 `HikariDataSource` 대신 `DataSource`로 받나? | 다른 라이브러리로 교체가 쉽게 | ✅ 정답 (PSA, 구현체에 묶이지 않음) |
| 의존성만 추가했는데 실행이 왜 실패하나? | DataSource url이 없다고 나와 | ✅ 정답. 직접 MyBatis를 빼고 실행해 보는 실험까지 함 👏 |
| `review.xml` search 4차 (2026-11-17) | 키워드 조건을 `(AND content ... OR title ...)`로 | 👍 괄호를 `<if>` **안**에 둔 것 정답. ❌ `AND`까지 괄호 안 → `<where>`가 AND를 못 지움(키워드만: `WHERE (AND ...`) + 둘 다: `rating >= ? (AND ...` 연결어 없음 → 둘 다 문법 에러 |
| `review.xml` search 5차 | `AND (content ... OR title ...)` | ✅ **통과**. 실행 로그 `WHERE rating >= ? AND (content LIKE ... OR title LIKE ...)`, `?` 3개, Total 2 |
| `review.xml` search 6차 | 별점 `test="minRating != null"` | ✅ **search 완성**. `Integer`라 null 검사 가능(지난번 선택이 효과), 잘못된 값(7)이 조용히 무시되지 않고 0건으로 드러남 |
| 괄호가 없으면 어떤 리뷰가 섞이나? | "`A AND (B OR C)`로 수정되지 않았어?" → 넘어감 | 🟡 지금 코드는 맞게 고쳐진 상태 ✅. 질문은 **고치기 전**을 묻는 것(회귀 테스트용) → 튜터 답: 별점 4 미만 + 제목에만 키워드 |
| `ReviewDaoTest` 재작성 | (튜터에게 작성 요청) | → 튜터 작성 (주석 포함): `@RequiredArgsConstructor` 제거, `@Transactional`, `@BeforeEach` 테스트 회원, 로그 대신 assert. 3개 통과 |
| 이해 확인: 함정을 본문에 넣으면? / `@BeforeEach` 회원이 안 남는 이유? | (넘어감) | → 본문에 넣으면 괄호 유무와 결과가 같아 버그를 못 잡음 / 같은 트랜잭션이라 rollback |
| M2 셀프 퀴즈 3개 | (넘어감) | → 튜터 답: Q1 `@ComponentScan` 범위(메인 클래스 패키지 하위) 안이어야 빈 / Q2 `@MapperScan` → MyBatis가 interface를 **구현한 프록시** 생성, namespace+id로 SQL 실행 / Q3 `#{}` = `?` 값 바인딩(PreparedStatement), `${}` = SQL에 그대로 붙음 → `' OR '1'='1` 입력 시 **SQL Injection** (`${}`는 컬럼명·정렬 방향처럼 값이 아닌 자리에 허용 목록 검사 후만) |

### 🛠️ 트러블슈팅

**`Cannot access session scope outside of a scoping block` (pom.xml, Maven Configuration Problem)**
- 증상: STS에서만 뜨고, 명령줄 `mvnw compile`은 정상 → STS 내장 Maven(m2e) 쪽 문제
- 해결 과정에서 발견한 실수: 컴파일 플러그인을 두 번 선언 / `spring-boot-maven-plugin`을 실수로 지움 / `Active Maven Profiles`에 `pom.xml`이 들어 있음
- 해결: `pom.xml` 정리 + `Alt + F5` → **Force Update of Snapshots/Releases**

**`Access denied for user 'ssafy'@'localhost' to database 'matzip'` (2026-11-14, 집 컴퓨터)**
- 증상: Workbench에서 `CREATE DATABASE`가 권한 부족으로 실패. `ssafy` 계정은 `enjoytrip`, `ssafy_mobile`에만 권한이 있음 (`SHOW GRANTS;`로 확인)
- 원인: `matzip` DB는 root로 만들어져 있었고, `ssafy`에는 권한이 없음 → 권한이 없으면 `SHOW DATABASES`에 DB가 **보이지도 않음**
- 배운 것:
  - Workbench는 로그아웃/로그인이 없다. **연결(Connection) 하나 = 계정 하나** → 홈 화면 `⊕`로 root 연결을 새로 만들거나, `Edit Connection`에서 Username을 바꾼다
  - 지금 계정 확인: `SELECT CURRENT_USER();` (`USER()`는 접속할 때 입력한 계정, `CURRENT_USER()`는 권한 검사에 실제로 쓰는 계정)
  - **최소 권한 원칙**: 실무에서는 앱 계정에 자기 DB 권한만 준다 (`GRANT ... ON DB.* TO ...`). 해킹이나 실수로 인한 피해가 그 DB에서 멈추게
- 해결(내 선택): 학습용 로컬이라 앱도 **root 계정**으로 접속하도록 `application.properties`를 수정 (`username=root`). 실무에서는 root를 쓰지 않고, 비밀번호는 git에 올리지 않게 환경 변수 등으로 분리한다

### ❓ 내가 한 질문

- 버전 목록이 `<parent>` 몇 줄밖에 안 보이는데 어디 있나? → 부모 pom에 있음 (상속)
- 의존성은 어떻게 추가하나? → `pom.xml`의 `<dependencies>` 안에 `<dependency>` 블록 추가 후 저장 + `Alt + F5`
- 라이브러리는 어디서 가져오나? / 인터넷 창고에서 가져오면 내 창고에도 자동 저장되나? → 그렇다 (Maven Central → `.m2`)
- DataSource가 뭔가? → 커넥션 풀의 대출 창구 인터페이스
- 강의 자료가 없으면 properties 키는 어디서 찾나? → STS 자동 완성 → 공식 문서 → Properties 클래스 (개념 8)
- rating을 1~5만 허용하려면? → `CHECK` 제약 조건 (개념 9)
- `schema.sql`은 어디에 저장하나? → `src/main/resources/scripts/` (개념 9)
- `CREATE DATABASE` 이름은 뭐로? → `application.properties` url의 DB 이름과 같게 (개념 7)
- `@Slf4j` + `log.debug` 문법은? / 우리 패키지 debug 설정은 어디 있나? → `@Slf4j`(Lombok)가 `log` 필드를 만들어 줌, `{}` 자리 표시자 / `application.properties`의 `logging.level.com.study=debug` (패키지 접두사 기준, 하위 패키지 전부 적용)
- url의 `serverTimezone=Asia/Seoul`은 뭔가? → 서버 시간대를 드라이버에 알려 주는 옵션 (개념 7)
- 그걸 왜 따로 쓰나? → 서버의 시간대 이름이 애매해서 표준 이름으로 확정 + Spring이 아니라 드라이버 옵션이라 url에 붙임 (개념 7)

### ⏭️ 남은 일

- [x] 1-1 `pom.xml` 의존성 추가 ✅
- [x] 1-2 `application.properties` 작성 → 실험 C ✅
- [x] 1-3 DB와 테이블 만들기 ✅
  - [x] `schema.sql` 작성·저장, 재실행 가능, 컬럼 길이 수정 완료
  - [x] **테스트 데이터 insert** (회원 1명 → 리뷰 2개, 하나는 image_name 없이) — 스크립트 마지막에 추가 ✅
  - [x] **rating 6 insert 실험** → `review_chk_1` 위반 (3819) ✅
- [x] 1-4 메인 화면 Controller + `index.jsp` + DataSource 주입 테스트
  - [x] `MainController`(`@Controller` + `@GetMapping("/")` + debug 로그), `index.jsp`, `ConfigTest` 작성 → 리뷰 통과
  - [x] `ConfigTest` 통과 → 로그에 `com.zaxxer.hikari.HikariDataSource` (인터페이스로 받았지만 실제 객체는 HikariCP = PSA)
  - [x] 브라우저 `http://localhost:8080/`에서 index.jsp 확인 + 콘솔 debug 로그 확인 ✅
- [x] 미답 질문: 실험 C에서 `matzip` DB가 없는데도 앱이 뜬 이유 ✅

### 📌 다음 세션 시작점

M1 완료 ✅ → M2 챕터로 이동

---

## M2. 회원가입 관통 + MyBatis CRUD — ✅ 완료 (2026-11-17, 일부 미룸) · [Notion](https://app.notion.com/p/41b6412b0acd81c6983aeffe279081f1)

### ✅ 한 일

- **리뷰 검색 완성** (2026-11-17): `review.xml` search 키워드 조건 괄호 `AND (content ... OR title ...)` + 별점 `minRating != null` → 실행 로그로 SQL 확인
- `ReviewDaoTest` 재작성 (교육생 요청으로 튜터 작성): `@BeforeEach` 테스트 회원 / `insertTest` / `searchTest`(괄호 회귀 테스트) / `searchWithoutConditionTest` → 3개 통과
- M2 마무리: join 상세·update·delete 테스트, `@MybatisTest`, `@Mapper` 실험은 **M3로 미룸** (교육생 결정)
- 리뷰 검색 진행 (2026-11-16): `ReviewSearch` DTO + `ReviewDao.search` 통과, `review.xml` search 작성(3차 리뷰, XML 파싱 통과 / OR 괄호 남음), `ReviewDaoTest` 초안 작성(리뷰 전)
- `MemberController` + `member/signup.jsp`(튜터 제공) + `index.jsp` 링크 → **브라우저에서 회원가입 → DB 저장 첫 관통** (2026-11-15)
- **2-1 완료** ✅: 브라우저 최종 확인 3가지 통과 (성공 redirect / 중복 에러 메시지 / F5 PRG)
- `Review` DTO 완성 (컬럼 7개 ↔ 필드 7개), 1차 리뷰 통과
- `ReviewDao` 완성 (2차 리뷰 통과: 오타 `selet` 수정, `update(Review)` 하나로) (2026-11-15)
- `review.xml` SQL 4개 1차 작성 → 리뷰 받음 (insert·update 수정 필요)

- `MemberService` + `MemberServiceImpl`(중복 검사 → insert, mno 반환) + `MemberServiceTest`(성공 / 중복 예외)

- `MemberDao`(insert, selectByEmail) + `mappers/member.xml` 완성
- `config/DBConfig` (`@Configuration` + `@MapperScan`) 완성

- `Member` DTO 완성 (`mno`, `email`, `password`, `name`, `LocalDateTime createdAt` + `@Data` `@NoArgsConstructor` `@AllArgsConstructor`), 3차 리뷰 통과
- `mybatis.configuration.map-underscore-to-camel-case=true` 추가

### 📚 배운 개념

**1. DTO (Data Transfer Object)**
- 계층(Controller ↔ Service ↔ DAO ↔ DB) 사이에서 데이터를 **담아 나르는 상자**. 필드 + getter/setter뿐
- 로직(검사·계산)은 넣지 않는다 → 로직은 **Service**의 몫. DTO는 "무엇을", Service는 "어떻게"
- 테이블 한 행 ↔ DTO 객체 하나 (컬럼 ↔ 필드)

**2. 컬럼명 ≠ 필드명 (`created_at` ↔ `createdAt`)**
- 이름이 다르면 MyBatis가 **에러 없이** 그 필드를 비워 둔다 (null) → 조용한 버그
- 해결 3가지 (교안 7장): ① SQL 별칭 `as createdAt` (쿼리마다) / ② `mybatis.configuration.map-underscore-to-camel-case=true` (한 번에, 우리 선택) / ③ resultMap (join에서 필수, 2-2에서)

**3. insert 후 PK 받아오기 & DEFAULT 함정**
- `useGeneratedKeys="true"` = DB가 만든 키를 받아 와라 / `keyProperty="mno"` = 그 키를 넘겨준 객체의 `mno` 필드에 넣어라
- PK는 반환값(영향받은 행 수)이 아니라 **넘겨준 객체 안에** 채워진다 → insert 후 `member.getMno()`
- DEFAULT는 **컬럼을 안 썼을 때만** 동작. `#{createdAt}`(null)을 쓰면 NULL을 직접 넣는 것 → 컬럼 목록·값 목록 둘 다에서 뺀다
- 이름(테이블·컬럼)은 따옴표 없이, 값(문자열)은 작은따옴표. `#{}`는 MyBatis가 `?`로 바꾸므로 따옴표 불필요

**4. 테스트와 트랜잭션**
- insert/update/delete 반환값 = **이번 SQL 한 번**으로 영향받은 행 수 (Workbench의 `1 row(s) affected`). 증가하는 값은 PK(`mno`)
- `@Rollback`은 "트랜잭션을 rollback으로 끝내라"는 옵션일 뿐 → 트랜잭션을 **시작**하는 건 `@Transactional`. 테스트의 `@Transactional`은 기본이 rollback
- 트랜잭션이 없으면 SQL마다 자동 commit → 테스트 데이터가 실제 DB에 남음 (실제로 겪음: test@, test1@, test2@ 저장)
- AUTO_INCREMENT는 실패·rollback돼도 번호를 되돌리지 않음 → **PK에 빈칸**(3, 4 건너뜀). PK는 "중복 없는 번호"이지 연속 번호가 아니다 → 테스트는 "정확히 +1"이 아니라 "더 큰지"로
- 테스트 클래스는 DAO를 **호출해서 검증**하는 쪽. `@Test` + `void` + 파라미터 없음

**5. Service의 회원가입 (정석)**
- 구조: `@Service` + `@RequiredArgsConstructor` + `private final MemberDao` → 생성자 주입. 싱글톤 빈은 상태를 갖지 않는다 (필드는 협력 객체만)
- **로그는 기록, `throw`는 흐름 정지** → 막아야 하는 경우는 `throw` (guard clause: 안 되는 경우를 먼저 막고 아래로 정상 흐름)
- insert 실패는 반환값 0이 아니라 예외(`DataAccessException`) → `res != 1` 검사 불필요
- 실패는 예외로 알리므로 반환은 성공 시 쓸모 있는 값(새 `mno`)
- Service 중복 검사 + DB UNIQUE 둘 다 필요: 친절한 메시지 / 동시 가입 경쟁 상황의 마지막 방어선
- 비밀번호는 로그에 남기지 않는다

**6. Service를 interface + 구현 클래스로 나누는 이유**
- 사용하는 쪽(Controller, 테스트)은 interface만 안다 → 구현을 바꿔도 사용하는 쪽 코드는 그대로 (DIP·OCP, `DataSource` ↔ `HikariDataSource`와 같은 원리, M5 `ImageStorage`에서 직접)
- 테스트에서 가짜 구현으로 바꿔 끼우기 쉬움 (M5 `@MockitoBean`)
- 역할(무엇을 하나: interface)과 구현(어떻게 하나: impl)이 분리되어 interface만 보면 기능 목록이 보임
- 참고: Spring은 클래스로도 프록시(`@Transactional`, AOP)를 만들 수 있어서, 구현이 하나뿐이면 interface를 생략하는 팀도 있음. 교안·샘플 관례를 따라 우리는 나눈다

**7. `resultMap` + `<association>` — 평평한 표를 2층 객체로**
- `resultType` = "이름 같은 칸에 알아서 넣어" → 한 층(`Review`)만 채움. join으로 온 `name`은 `Review`에 `setName()`이 없어 **에러 없이 버려짐**
- `resultMap` = **배치도**: `column`(결과 표 컬럼) → `property`(Java 필드). `<id>`는 PK 줄(같은 객체인지 구분 기준), 나머지는 `<result>`
- `<association property="writer" javaType="Member">` = "이 컬럼들은 `writer` 필드의 `Member` 객체 안으로" (has-a 한 개). select에는 `resultType` 대신 `resultMap="배치도 id"`
- 같은 이름 컬럼(`mno`, `created_at`)은 테이블 별칭 + `AS writer_mno`로 구분 → association의 `column`에 바뀐 이름
- `<association>`이 있으면 기본 설정상 자동 매핑이 꺼짐 → 필요한 컬럼은 전부 적는다
- update·delete에서 `WHERE`를 빠뜨리면 **모든 행**이 바뀜/삭제됨 (에러 없음)

**8. 동적 SQL — `<where>` + `<if>` (리뷰 검색)**
- 검색 조건은 사용자가 **입력한 것만** SQL에 들어가야 함: 둘 다 → `WHERE 키워드 AND 별점` / 키워드만 → `WHERE 키워드` / 둘 다 비움 → **`WHERE` 자체가 없음**(전체 목록)
- 문자열로 직접 이어 붙이면 생기는 문제: `WHERE AND rating >= 3`(앞에 남은 AND) / 내용 없이 끝나는 `WHERE` → 둘 다 SQL 문법 에러. 옛날 해결책은 `WHERE 1=1` 뒤에 `AND ...`를 붙이는 꼼수
- `<if test="조건">` = 조건이 참일 때만 안의 SQL 조각을 넣는다. `test` 안에서는 파라미터 객체의 **필드 이름**을 그대로 쓴다 (`#{}` 없이)
- `<where>` = 안에 들어간 내용이 **있을 때만** `WHERE`를 붙이고, 맨 앞의 `AND`/`OR`를 자동으로 지운다 → 그래서 모든 조건을 `AND ...`로 시작하게 써도 됨
- (개념 9) `<if test="조건식">SQL 조각</if>`: 조건식이 true면 조각을 넣고 false면 뺀다. 조건식은 Java가 아니라 **OGNL**: 필드 이름 그대로(`#{}` 없음), `and`/`or`, `==`/`!=`, 문자열은 작은따옴표 `''`(속성이 큰따옴표라서). 타입별 "입력했나?": String → `x != null and x != ''` / Integer → `x != null`. 값 범위 검증은 `test`에 넣지 않는다
- 빈 문자열 함정: 폼에서 키워드 칸을 비워 보내면 null이 아니라 `""` → `null` 검사만 하면 `LIKE '%%'`(전부 일치)가 들어감 → null과 빈 문자열을 **둘 다** 검사

### 🙋 내 답변과 피드백

| 질문 | 내 답 | 피드백 |
|---|---|---|
| DTO는 무엇을 위한 객체? 로직을 넣지 않는 이유? | 비즈니스 로직을 처리할 때 쓰이는 목적어 같은 역할 | 🟡 "목적어" 비유 좋음 (로직이 다루는 대상). 빠진 것: **계층 사이에서 데이터를 나르는** 용도, 로직은 Service에 두는 이유(역할 분리) |
| `created_at` ↔ `createdAt` 이름이 다르면? | (튜터에게 설명 요청) | → 개념 2 |
| `Member` DTO 1차 | `int mId`, 접근 제어자 없음, `String createdAt`, `@Data`+`@Getter`+`@Setter` | ❌ `mId` ≠ 컬럼 `mno` → 조용히 0 / 필드에 `private` 없음(package-private) / 날짜는 String 말고 `java.time` / `@Data`가 getter·setter 포함 → 중복. 👍 패키지 위치, 기본·전체 생성자, `createdAt` 카멜 케이스 |
| properties 1차 | `map-underscore-to-camel-case=True;` | ❌ `;`까지 값에 포함 → boolean 변환 실패로 앱 시작 실패. properties엔 문장 끝 기호 없음 |
| `Member` DTO 2차 | `mno`, `private`, `LocalDate`, `;` 삭제 | ✅ 4개 수정 완료 👍 / 🟡 `LocalDate`는 날짜만 → DATETIME의 **시간이 버려짐** → `LocalDateTime` / 💅 안 쓰는 import(`Getter`, `Setter`) 정리 |
| `Member` DTO 3차 | `LocalDateTime` + import 정리 | ✅ 완성. 타입 짝: DATE↔`LocalDate`, TIME↔`LocalTime`, DATETIME↔`LocalDateTime` |
| `MemberDao` | `int insert(Member)`, `Member selectByEmail(String)` | ✅ 통과. 💅 interface 메서드는 자동 public |
| `member.xml` namespace 1차 | `src.main.java.com...MemberDao` | ❌ 파일 경로가 아니라 **FQCN**(패키지 + 이름) → `com.study.matzip.model.dao.MemberDao` (튜터에게 값 요청). STS `Copy Qualified Name` |
| `member.xml` insert 1차 | 컬럼에 `'email'`, `created_at` + `#{createdAt}`, `keyProperty=""` | ❌ 컬럼 이름엔 따옴표 없음(값만 작은따옴표) / `#{createdAt}` = NULL을 직접 넣음 → **DEFAULT 무시, 가입일 NULL** (조용한 버그) / `keyProperty="mno"` / DOCTYPE 누락. 👍 namespace·id·resultType·select 정확 |
| `member.xml` 2차 | DOCTYPE, `keyProperty="mno"`, 따옴표·`created_at` 삭제 | ✅ 완성. 컬럼 3개 ↔ 값 3개 |
| Q3. interface뿐인 DAO의 객체는 누가 만드나? | MyBatis가 프록시로 상속받아 만든다. 그래서 `@Mapper`/`@MapperScan`으로 빈으로 만든다 | ✅ 거의 정답. 🟡 "상속(extends)"이 아니라 interface를 **구현(implements)**한 프록시. 메서드 호출 → namespace+id로 SQL 찾아 실행 |
| Q4. Java와 XML을 잇는 이름 두 개? | namespace: 인터페이스 이름, id: 메서드 | 🟡 id ✅ / namespace는 **패키지까지 포함한 전체 이름(FQCN)** — 직접 `src.main.java...`로 틀렸다 고친 그 부분 |
| `DBConfig` 1차 | `@MapperScan`만 | ❌ `@Configuration` 없음 → 설정 클래스로 안 읽혀 `@MapperScan` 무시 → DAO 빈 없음 (앱은 정상으로 뜸 = 조용한 버그) |
| `DBConfig` 2차 | `@Configuration` 추가 | ✅ 통과. 💅 정체(`@Configuration`)를 맨 위에 |
| `MemberDaoTest` 1차 | `@SpringBootTest`, insert만, 로그로 확인, `new Member().builder()`, 클래스에 `selectByEmail(String);` 선언 | ❌ 몸통 없는 메서드 선언 → 컴파일 에러 / `@Test` 없음 → 실행 안 됨 / `@Transactional` 없음 → 실제 저장, 재실행 시 UNIQUE 위반 / Assertions 없음 → 항상 초록. 💅 `builder()`는 static → `Member.builder()`. 👍 주입 구조, `{}` 로그, DTO에 `@Builder` 직접 추가 |
| `MemberDaoTest` 2차 | `@Test` + `@Rollback`, insert 2번, `assertEquals(2, res2)` | ❌ 반환값은 **이번 호출의 영향받은 행 수** → 둘 다 1 (누적 아님) / `@Rollback`만으로는 rollback 안 됨 → 트랜잭션을 여는 `@Transactional` 필요 → 테스트 회원 3명이 실제 저장됨 / 클래스에 `return` 없는 `Member selectByEmail(){}` → 컴파일 에러 |
| `MemberDaoTest` 3차 | `@Transactional`, 조회 테스트(있는/없는 이메일) | ✅ rollback 동작 (DB 1명 유지, AUTO_INCREMENT=4 → 2·3번 썼다가 되돌림). 🟡 `assertNotNull(m1)` 먼저 / ⭐ `createdAt` 검증 누락(camel-case 설정 증명) / 객체 전체 로그. 💅 `Member.builder()`, 테스트 하나에 확인 하나 |
| `MemberServiceImpl.signUp` 1차 | 중복이면 `log.debug`, `res != 1` 검사, `Boolean` 반환 + `return null` | 👍 `@Service` + `@RequiredArgsConstructor` + `private final` 구조는 정석. ❌ 로그는 흐름을 멈추지 않음 → 중복이어도 insert 실행 → DB UNIQUE 에러 / insert 실패는 0이 아니라 예외 / 항상 null 반환. → **정석 코드는 튜터가 작성** (내 요청, 이유 주석 포함): `throw new IllegalArgumentException`, 반환 `int`(mno), `log.info` |
| `MemberServiceImpl` 반영 | 정석 코드 반영, interface 반환 `int` | ✅ 💅 미사용 import(`lombok.extern.java.Log`), 탭/공백 섞임 → `Ctrl + Shift + F` |
| `MemberServiceTest` 1차 | 한 메서드에 두 경우, `try-catch`로 예외 잡아 로그 | 👍 `@Transactional`, **interface 타입으로 주입**. ❌ `= ms.signUp(...)` 컴파일 에러 / **try-catch로 예외를 삼키면 항상 초록** → 실패 케이스는 `assertThrows`, 성공 케이스는 try 없이 호출 + 검증 / `res`가 try 블록 안 → 검증 불가. 🟡 테스트 2개로 분리. 💅 `Member.builder()` (3번째) |
| `MemberServiceTest` 2차 | (튜터에게 작성 요청) | → 튜터 작성: `signUp_success`(try 없이 호출 + mno 검증), `signUp_duplicateEmail`(`assertThrows` + 메시지 검증), given/when/then. 실험: `throw` 주석 처리 시 빨간 막대 확인 |
| Q5. DAO와 Service의 단위? | DAO는 DB와 연결, Service는 비즈니스 로직을 처리하는 원자성 단위 | ✅ 👍 "원자성 단위" = 트랜잭션과 연결 (M4). 🟡 DAO는 "연결"보다 **쿼리 하나 단위**의 DB 접근 (연결은 HikariCP 몫) |
| Q6. Service를 interface + 구현으로 나누는 이유? | (튜터에게 설명 요청) | → 개념 6 |
| Q7. 가입 성공 후 forward가 아니라 redirect인 이유? | forward면 F5 때 주소가 안 바뀌어서 계속 회원가입 요청 발생 | ✅ 정답 (PRG: Post-Redirect-Get). redirect 후 브라우저 마지막 요청 = `GET /` → F5는 GET만 반복. 우리 앱에선 중복 POST가 가면 중복 검사 예외로 이어짐 |
| Q8. 폼 값 3개가 어떻게 `Member` 하나로? | `@RequestParam`으로 받아서 `Member`로 만든다 | 🟡 그것도 가능하지만 수동. **`@ModelAttribute`**는 기본 생성자로 객체를 만들고 파라미터 이름 = 필드 이름인 **setter**를 호출해 자동으로 채움 (`@RequestParam` = 값 하나, `@ModelAttribute` = 객체) |
| `MemberController` 1차 | `@RequestMapping("/member")` + `@Controller`, `@ModelAttribute`, try-catch | ✅ **브라우저 가입 → DB 저장 성공** (mno 5, created_at DEFAULT). ❌ `redirect:/index` → 404 / catch에서 Model에 error 안 담음 → 메시지 안 보임 / `catch (Exception)` → 진짜 장애까지 숨김 → `IllegalArgumentException`만. 🟡 URL `signUp-form`(앞 `/` 없음, 대문자) → GET·POST 같은 `/signup` 고려 / Service와 로그 중복 |
| `MemberController` 2차 | `redirect:/`, `Model`에 error, `IllegalArgumentException`만 catch, `public`, `@Controller` 맨 위 | ✅ 3개 모두 수정. 💅 안 쓰는 `int mno`. 💡 `@ModelAttribute` 객체는 자동으로 Model에 `member`로 담김 → `value="${member.name}"`로 입력값 유지 가능 |
| Q2-1. review와 member를 SQL에서 무엇으로 합치나? | `mno` | 🟡 `mno`는 **연결 고리(조인 키)**로 정답. 합치는 문법은 **`JOIN ... ON r.mno = m.mno`** → 두 테이블의 행을 옆으로 붙여 한 행으로 만듦 |
| Q2-2. 작성자 정보를 `Review` 객체에 어떻게 담나? | `mno` | ❌ `mno`는 숫자뿐 → 이름을 알려면 DB를 한 번 더 조회해야 함. 담는 방법 ① `writerName` 같은 String 필드 ② **`Member` 객체를 필드로**(객체 안에 객체, has-a) → MyBatis `<association>`이 ②를 채움 (join 단계) |
| `Review` DTO 1차 | 컬럼 7개 ↔ 필드 7개, `private`, `imageName`·`createdAt` 카멜 케이스, `LocalDateTime`, Lombok 4종 | ✅ **한 번에 통과**. M2 1차에서 짚은 것(접근 제어자, 이름 짝, 날짜 타입, import)을 전부 반영 👍 |
| Q3. `ReviewDao`를 만들면 `DBConfig`를 고쳐야 하나? | 그대로 둬도 됨, 같은 dao 패키지라서 | ✅ 정답. `@MapperScan`은 **패키지 단위**로 interface를 찾아 등록 (하위 패키지 포함). XML 쪽은 `mapper-locations`의 `mappers/**/*.xml`이 `review.xml`도 잡음 → **Java 쪽·XML 쪽 둘 다** 설정 변경 없음 |
| `ReviewDao` 1차 | `insert` / `seletByRno` / `update(int rno, Review review)` / `delete` | 👍 패키지 위치, 반환 타입(`int` 행 수 / 단건 `Review`). ❌ `selet` 오타 → XML id와 짝이 안 맞으면 호출할 때 `BindingException` / `update` 파라미터 2개 → `Review` 안에 `rno`가 이미 있음, 2개면 `@Param` 필요 |
| `ReviewDao` 2차 | `selectByRno`, `update(Review review)` | ✅ 통과. 💅 interface 메서드의 `public` 생략 가능 |
| `review.xml` 1차 | insert / select / update / delete 4개 | 👍 namespace, id 4개가 DAO와 정확히 짝, delete·select의 `WHERE rno`, DEFAULT 컬럼 `created_at` 뺌, `resultType="Review"` alias. ❌ `keyProperty="mno"` → 새 rno가 **작성자 mno 필드를 덮어씀** (조용한 버그) / `#{image_name}` → `#{}` 안은 **DTO 필드 이름** `imageName` (getter 없음 에러) / update에 `INSERT` 문법(`INTO`, `VALUES`) → UPDATE는 `SET 컬럼 = 값, ...` + 작성자 `mno`는 수정 대상 아님. 💅 SQL 끝 `;` 빼기 (한 태그 = 한 문장) |
| `review.xml` 2차 | `keyProperty="rno"`, `#{imageName}`, update를 `SET(컬럼들) VALUES(값들)`로 | ✅ insert 완성 (keyProperty, imageName 둘 다 수정). ❌ update는 아직 문법 에러: INSERT는 **컬럼 목록 + 값 목록을 순서로 짝짓기**, UPDATE는 **`컬럼 = 값` 쌍을 쉼표로 나열** (괄호·VALUES 없음) / `mno`(작성자)는 수정 대상 아님 → SET에서 제외 |
| `review.xml` 3차 | update를 `SET 컬럼 = #{필드}, ...` + `WHERE rno`로 | ✅ **UPDATE 문법 완성** (쉼표 앞에 두는 스타일 👍, WHERE 유지). ❌ `mno = #{mno}`가 아직 SET에 있음 → 작성자가 바뀔 수 있음 + 수정 폼에 mno가 없으면 0이 들어가 FK 에러 → SET에서 제외. 💅 `Set` → 키워드 대문자 `SET` 통일 |
| `review.xml` 4차 | update SET에서 `mno` 제거 | ✅ **SQL 4개 완성**. 수정 가능한 것만 SET, 대상은 WHERE rno. 튜터 확인: `MemberDaoTest` 실행 시 `review.xml` 파싱 포함 앱 컨텍스트 정상 로딩 |
| Q2-3. join 결과의 `name` 컬럼을 `resultType="Review"`만으로 받으면? | ① `Review`에는 `setName()`이 없다 ② MyBatis가 `Member`를 알아야 한다 | 🟡 ① ✅ 원인 정확. 빠진 것: 그 값은 **에러 없이 버려짐** (조용한 버그, `writer`는 null) ② 🟡 방향 맞음. 정확히는 "**`name` 컬럼은 `writer` 필드(`Member` 타입) 안의 `name`에 넣어라**" = 어느 필드로 + 어떤 타입으로 + 어느 컬럼을 → 이것이 `<association property javaType>` + `<result>` |
| 워밍업. update에서 `WHERE`를 빠뜨리면? | (답 안 함) | → 튜터 설명: **테이블의 모든 행**이 같은 값으로 바뀜 (리뷰 전부가 같은 제목). 에러 아님. DELETE도 같음 → 전부 삭제. Workbench는 Safe Updates 모드로 막아 주지만 JDBC/MyBatis는 안 막음 |
| 리뷰 상세 join | `Review`에 `Member writer` 필드, `ReviewDao.selectDetail(int rno)` 직접 작성 / `review.xml`은 **튜터에게 작성 요청** (주석 포함) | ✅ DTO·DAO 통과 (반환 `Review`: PK 조회라 최대 한 행). 튜터 작성: `reviewDetailMap`(`<id>` + `<result>` 6개 + `<association property="writer" javaType="Member">`) + join select (`m.mno AS writer_mno`, member `created_at`·`password` 제외). 💡 resultMap id를 select id와 같게(`selectDetail`) 했었음 → 허용은 되지만 헷갈려서 `...Map`으로 구분 |
| 복습. `m.mno`에 `AS writer_mno` 별칭이 없다면? | (튜터에게 설명 요청, 2026-11-16) | → 결과 표에 `mno` 컬럼이 **두 개** 생김. resultMap은 컬럼을 **이름**으로 찾으므로 둘을 구분할 수 없고, 보통 앞쪽 컬럼 값을 씀. 지금은 `ON r.mno = m.mno`라 두 값이 같아서 **우연히 맞음** → 값이 다른 컬럼(`created_at`)이었다면 리뷰 작성일 자리에 가입일이 들어가는 조용한 버그. 별칭은 이름을 하나로 정해 주는 것 |
| Q1·Q2. 키워드만 / 둘 다 비웠을 때 WHERE는? | (튜터에게 설명 요청) | → 키워드만이면 `AND rating ...`이 빠져야 하고, 둘 다 비면 `WHERE` 자체가 없어야 함. 직접 이어 붙이면 `WHERE AND ...`, 끝에 남은 `WHERE` → 문법 에러 → 개념 8 |
| `ReviewSearch` DTO + `ReviewDao.search` | `String keyword`, `Integer minRating`, Lombok 4종 / `List<Review> search(ReviewSearch)` | ✅ **한 번에 통과**. `Integer` 선택 정답: `int`는 비우면 0 → "비움"과 "0점 이상"을 구분 못 함 / `Integer`는 null → `<if test>`에서 null 검사로 구분. 반환 `List` = 여러 행 (0건이면 빈 List, null 아님). 💅 interface의 `public` 생략 가능(기존과 통일해 둬도 OK) |
| `review.xml` search 1차 | `<where>` + `<if>` 2개: `test="1 <= minRating and minRating <= 5"` → `rating = #{minRating}` / `keyword != null and keyword != ''` → `content LIKE CONCAT(...)` | 👍 `resultType="Review"`(한 행 타입), 키워드 test(null + 빈 문자열) 완벽, 조각을 `AND`로 시작, `CONCAT` 사용. ❌ **`test` 속성 안의 `<`** → XML 문법 위반, 파일 전체 파싱 실패(튜터 확인: 60행) → **앱 시작/모든 테스트 실패** → `&lt;` 또는 `lt`/`lte` 같은 OGNL 단어 연산자 / ❌ `rating =` → "최소 별점"이면 `>=` (= 은 정확히 그 별점만) / ❌ 키워드가 `content`만 검색 → 제목도 (`OR` + 괄호 함정, 답 대기) / 🟡 범위 검사(1~5)는 DAO가 아니라 입력 검증(Controller/Service) 몫, DAO의 `test`는 "조건이 **있나 없나**"(null 검사)만 / 💅 `ORDER BY` 최신순 |
| `review.xml` search 2차 | `test="1 &lte minRating and minRating &lte 5"`, `rating >= #{minRating}` | ✅ `>=` 수정. ❌ `&lte` = 두 방법을 섞음 → XML 엔티티는 `&이름;`(세미콜론 필수, `&lt;`= `<` 한 글자), OGNL 단어 연산자는 `&` 없이 `lte` → 아직 60행 파싱 실패(튜터 확인) / ❌ 키워드 `content`만 + 괄호 질문 미답 / 🟡 범위 검사 여전히 DAO에 → `minRating != null`이면 1번 문제 자체가 사라짐 |
| (요청) 어디를 바꿀지 주석으로 표시해 달라 | — | → 튜터가 `review.xml` search에 `TODO: 1`(test를 null 검사로) / `TODO: 2`(title OR content + 괄호) / `TODO: 3`(선택, ORDER BY 위치) 주석만 추가. 정답 코드는 넣지 않음. 고친 뒤 TODO 주석은 직접 지우기 |
| (질문) 60행 `test`를 어떻게 고치라는 건지? / 키워드에 `OR title LIKE` 추가(괄호 없음) | — | → `test`의 질문은 "값이 올바른가?"가 아니라 "**사용자가 입력했나?**" → 바로 아래 키워드 `test`의 앞부분과 같은 패턴(숫자라 빈 문자열 검사는 불필요). 🟡 OR 추가 👍, 하지만 괄호 없음 → `(content AND rating) OR title`로 묶여 별점 4 이상 검색에도 제목에 키워드만 있으면 1점 리뷰가 섞임 |
| `review.xml` search 3차 | `test="1 &lt;= minRating and minRating &lt;= 5"`, 키워드 `content ... OR title ...`(괄호 없음) | ✅ `&lt;=` 엔티티 정확 → **XML 파싱 통과**(튜터 확인). 🟡 동작은 함: OGNL이 비교할 때 null을 0으로 바꿔서 `1 <= 0` = false → 조건 빠짐. 하지만 ① "입력했나?"라는 의도가 안 보이고 null→0 변환에 숨어서 기댐 ② 7·0 같은 잘못된 값이 **에러 없이 무시**되어 전체 목록이 나옴(사용자는 모름) → `minRating != null` 권장, 범위는 M3 검증. ❌ OR 괄호 아직 없음 → 별점 필터가 title 쪽에 안 걸림 (테스트로 증명 예정) |

### 🛠️ 트러블슈팅

**`MemberDaoTest` 재실행 시 실패 — `Duplicate entry 'test1@test.com' for key 'member.email'`**
- 원인: `@Rollback`만 있고 `@Transactional`이 없어서 지난 실행의 insert가 commit됨 → 같은 이메일로 다시 insert → UNIQUE 위반
- 해결: ① `schema.sql` 전체 재실행으로 DB 청소 ② `@Transactional`로 매번 rollback (청소만 하면 다음 실행에서 또 실패)
- 교훈: 좋은 테스트 = **몇 번을 실행해도 같은 결과** (반복 가능성)

**STS에서 `application.properties` 한글 주석이 `í¨í¤ì§`처럼 깨짐 (2026-11-14)**
- 디스크의 파일은 UTF-8로 정상 → STS가 **읽는 인코딩**만 틀림
- 원인: Eclipse가 `.properties` 형식에 기본 인코딩 **ISO-8859-1**을 따로 지정 (옛날 Java가 properties를 ISO-8859-1로만 읽던 관례). 형식별 설정이 프로젝트 UTF-8 설정보다 먼저 적용됨
- 해결: `Window > Preferences > General > Content Types > Text > Java Properties File` → Default encoding `UTF-8` → Update (워크스페이스 전체 적용). 대안: 파일 `Properties > Resource > Text file encoding > Other: UTF-8`
- ⚠️ 깨진 채로 저장하면 파일이 진짜로 망가짐 → 저장하지 말고 닫은 뒤 설정부터
- 실제로 깨진 채 저장됨 → 튜터가 내 요청으로 한글 주석 복구 (UTF-8 바이트를 ISO-8859-1로 읽은 글자를 다시 UTF-8로 저장한 상태 = 이중 인코딩 → 역변환). 설정 값은 그대로

### ❓ 내가 한 질문

- `return "redirect:/index"`가 왜 메인으로 안 가나? → `redirect:` 뒤는 **URL**(브라우저가 새로 요청할 주소), 그냥 문자열은 **뷰 이름**(JSP 파일). `/index`를 받는 `@GetMapping`이 없어서 404. 메인 URL은 `MainController`의 `@GetMapping("/")` → `redirect:/`

- `<a href="..."></a>`가 왜 안 보이나? → 태그 사이에 글자가 없으면 크기 0 → 안 보임. `href`는 목적지, 사이의 내용이 화면 표시. 덤: `<c:set>`은 JSTL **taglib 선언**이 있어야 동작 (없으면 그냥 글자로 출력, `${root}`는 빈 값 → 우연히 동작). 그리고 `@Controller("/member")`의 괄호 값은 **빈 이름**이지 URL이 아님 → URL 앞부분은 `@RequestMapping`

- 명확성을 위해 `@Qualifier`를 항상 붙이는 게 낫지 않나? → 보통은 붙이지 않는다. ① 사용하는 쪽이 **구현 클래스 이름**(`"memberServiceImpl"`)을 알게 됨 → interface로 받는 의미(Q6)가 약해짐 ② 문자열이라 오타·클래스 이름 변경을 컴파일러가 못 잡음 ③ 구현을 바꾸면 사용하는 곳을 전부 찾아 고쳐야 함. 명확성은 **타입**(interface)이 주고, 어떤 구현을 쓸지는 구현·설정 쪽(`@Primary`, `@Profile`)에서 정한다. `@Qualifier`는 같은 타입이 여럿이고 **골라야 할 때만**

- `MemberService ms;`에 `@Qualifier`를 안 붙여도 되나? → 됨. `@Autowired`는 **타입**으로 찾고, `MemberService` 타입 빈이 `MemberServiceImpl` **하나뿐**이라 헷갈릴 게 없음. 같은 타입 빈이 2개 이상이면 `NoUniqueBeanDefinitionException` → `@Qualifier("빈 이름")`(사용하는 쪽에서 지목) 또는 `@Primary`(구현 쪽에서 기본값 지정). 빈 이름 기본값 = 클래스 이름 첫 글자 소문자(`memberServiceImpl`). M5 `ImageStorage`에서 직접

- `selectByEmail`로 조회되는 이메일이 없으면 null인가? → ✅ null (`MemberDaoTest`의 `assertNull`로 이미 증명). 단일 객체 반환: 0건 null / 1건 객체 / 2건 이상 `TooManyResultsException` (email UNIQUE라 불가능). List 반환은 0건이면 빈 List. 변수 이름 바꾸기 `Alt + Shift + R`

- 예외는 어떻게 던지나? → `throw new 예외클래스("메시지");` (`throw` = 던지는 문장, `new` = 예외도 객체). `throw` 아래 코드는 실행 안 됨 → if 안에서 던지면 else 불필요. RuntimeException 계열(unchecked)은 `throws` 선언 없이 던질 수 있음 (Spring의 `DataAccessException`도 같은 이유로 Runtime). 받는 쪽은 `try-catch` 또는 그대로 위로 전파 → M3에서 `@ControllerAdvice`가 한곳에서 처리

- `@Rollback`을 붙여도 실제 DB에 반영되는 이유? → `@Rollback`은 "트랜잭션을 어떻게 끝낼지" 정하는 **옵션**일 뿐, 트랜잭션을 만들지 않음 (장바구니 없이 "비워라"). 트랜잭션이 없으면 SQL마다 즉시 commit. `@Transactional`이 트랜잭션을 만들고, 테스트에서는 기본이 rollback → `@Rollback`은 기본값을 바꿀 때(`@Rollback(false)`)만. import는 `org.springframework.transaction.annotation.Transactional`

- member는 `@MapperScan`, review는 `@Mapper`로 섞어서 둘 다 공부해도 되나? → 섞지 말고 차례로. 같은 패키지면 `@MapperScan`이 이미 등록해서 `@Mapper`가 의미 없음 / 다른 패키지면 `@MapperScan`이 있는 순간 MyBatis 자동 설정의 `@Mapper` 탐색이 꺼짐(`@ConditionalOnMissingBean`) → 등록 안 됨. 2-2 테스트 통과 후 바꿔서 같은 테스트로 비교

- `DBConfig` 없이 `MemberDao`에 `@Component`를 붙이면 안 되나? → ❌ 안 됨. `@ComponentScan`은 **구체 클래스만** 빈 후보로 삼고 interface는 건너뜀 (interface는 `new` 불가, 구현도 없음) → 빈이 없어 주입 시 `NoSuchBeanDefinitionException`. interface에 붙여 쓸 수 있는 건 MyBatis의 **`@Mapper`** (MyBatis가 프록시를 만들어 빈 등록). `@Mapper`(DAO마다) vs `@MapperScan`(패키지 한 번) 중 하나 선택

- `<!DOCTYPE mapper PUBLIC ...>`은 뭔가? → 이 XML의 **문법 규칙서(DTD)**를 지정하는 선언. `mapper` = 최상위 태그 이름, `-//mybatis.org//DTD Mapper 3.0//EN` = 규칙서의 공식 이름, URL = 규칙서 위치. 규칙서에 어떤 태그(`insert`, `select`…)와 속성(`id`, `resultType`…)을 쓸 수 있는지 정해져 있음 → STS 자동 완성·오타 검사, MyBatis가 읽을 때 검증 (규칙서는 MyBatis jar 안에 있어 인터넷 없어도 됨). HTML의 `<!DOCTYPE html>`과 같은 역할. 외우지 말고 복사

- `useGeneratedKeys`는 어떤 기능이고 어떻게 동작하나? → JDBC 기능을 MyBatis가 대신 써 줌: `prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)` → `executeUpdate()` → `getGeneratedKeys()`(ResultSet) → `keyProperty` 필드의 setter 호출. MySQL은 **커넥션별로** 마지막 생성 키를 기억 → 동시에 여러 명이 가입해도 내 키만 받음 (`SELECT MAX(mno)`로 찾으면 위험). 지원 안 하는 DB용 대안: `<selectKey>`

- `mybatis.mapper-locations=classpath:mappers/**/*.xml` 해석? → `classpath:` = 빌드 후 클래스패스 루트(`src/main/resources`의 내용이 복사되는 곳) / `mappers/` 폴더 / `**` = 하위 폴더 몇 단계든(0단계 포함) / `*.xml` = 이름 상관없이 xml 전부 → `mappers/member.xml`, `mappers/review/review.xml` 모두 해당. MyBatis가 이 xml들을 읽어 namespace로 DAO와 연결

- `<where>` `<if>` 사용 예제를 보여 달라 (2026-11-16) → 맛집 노트와 다른 **상품(product) 검색** 예제로 설명: `<if test="name != null and name != ''">`, `AND`로 시작하는 조각, 입력 조합별로 만들어지는 SQL 3가지, `test`는 필드 이름·`and`/`or`·`==`/`!=`, XML에서 `<=`는 `&lt;=`. 샘플 `FW_07` `address.xml`의 `dynamicSelect`(`<trim prefix="where" prefixOverrides="or|and">` = `<where>`의 원리)와 `member.xml`의 `searchCondition`(`<sql>` + `<include>`)도 같은 패턴. ⚠️ OR 괄호 함정은 예제에서 빼고 직접 풀기로

- `<if test>` 문법을 다시 정리해 달라 → 📚 개념 9
- `&lt;`는 언제 어떻게 쓰나? → XML에서 `<`는 "태그 시작" 예약 문자 → 글자로서의 `<`가 필요하면 **엔티티** `&lt;`(less than)로 쓴다. 형식은 `&` + 이름 + `;`(세미콜론 필수). `<=`는 `&lt;=`. 자주 쓰는 것: `&lt;`(<) `&gt;`(>) `&amp;`(&). `>`는 그대로 써도 됨. 대안: SQL 조각 안에서는 `<![CDATA[ ... ]]>`(속성 안에서는 불가), `test` 안에서는 OGNL 단어 `lt`/`lte`/`gt`/`gte`. `&lte`는 둘을 섞은 것이라 오류. 지금 검색에서는 쓸 일 없음(null 검사만)

### ⏭️ 남은 일

- [x] 2-1 회원가입 한 줄 관통 ✅
  - [x] `Member` DTO ✅
  - [x] `MemberDao` + `member.xml` (insert + PK 받아오기, selectByEmail) + `@MapperScan` ✅
  - [x] `MemberDaoTest` (`@SpringBootTest` + `@Transactional`): insert / 있는 이메일 / 없는 이메일 → 통과, rollback 확인 ✅
    - [ ] (보강, 미룸) `assertNotNull(m1)`, **`createdAt` 검증**, `Member.builder()`, 미사용 import 정리
  - [x] `MemberService` (interface + impl), 생성자 주입, 이메일 중복 검사 ✅
  - [x] `MemberController` + `member/signup.jsp`(튜터 제공) + `index.jsp` 링크 → 브라우저 가입 → DB 저장 확인 ✅ (2차 리뷰 통과)
  - [x] 브라우저 최종 확인 3가지 ✅ (2026-11-15): 새 이메일 가입 → 메인 redirect / `kim@matzip.com` → 폼에 에러 메시지 / F5 → 회원 추가 안 됨 (PRG)
  - [ ] (💅 선택) 실패 시 입력값 유지(`value="${member.name}"`) (`int mno` 정리는 완료 ✅)
  - [ ] (미확인) `MemberServiceTest` 실행 결과 공유: 초록 2개, 로그의 mno·`createdAt`(insert 직후 객체엔 null인 이유), 예외 메시지
- [x] 2-2 리뷰 CRUD ✅ (`@MybatisTest`는 미룸)
  - [x] `Review` DTO ✅
  - [x] `ReviewDao` ✅
  - [x] `review.xml` SQL 4개 (insert / selectByRno / update / delete) ✅ (4차 리뷰 통과)
  - [x] 리뷰 상세 join: `Review.writer` + `selectDetail` + `reviewDetailMap` ✅ (XML은 튜터 작성)
  - [x] 리뷰 검색: `<where>` + `<if>` (키워드, 최소 별점) ✅ (2026-11-17)
    - [x] `ReviewSearch` DTO (`String keyword`, `Integer minRating`) + `ReviewDao.search` (`List<Review>`) ✅
    - [x] `review.xml` search: XML 파싱 통과 (`&lt;=`), `rating >=`, 키워드 test ✅
    - [x] 키워드 `content OR title` **괄호로 묶기** ✅ (2026-11-17 5차 리뷰 통과: `AND (... OR ...)`, 실행 로그 `WHERE rating >= ? AND (content LIKE ... OR title LIKE ...)` Total 2 확인)
      - (5차 세션 4차 리뷰) 괄호를 `<if>` 안에 넣은 건 ✅. 단 `(AND content ... OR title ...)`처럼 **`AND`까지 괄호 안** → `<where>`가 `(`로 시작해 AND를 못 지움(키워드만: `WHERE (AND ...` 에러) + 둘 다: `rating >= ? (AND ...` 연결어 없음 에러 → `AND`는 괄호 밖 맨 앞, 괄호는 두 LIKE만
    - [x] 🟡 별점 test를 범위 비교 → `minRating != null`로 ✅ (2026-11-17, `Integer`라 null 검사 가능, 실행 확인)
    - [ ] 💅 (선택) `ORDER BY` 최신순 (건너뜀)
    - 괄호 질문은 교육생이 넘어가길 원해 튜터가 답 제시 (2026-11-17): 괄호 없으면 `(A AND B) OR C` → **별점 4 미만 + 제목에만 키워드**인 리뷰가 섞임 → 회귀 테스트의 함정 리뷰로 사용
  - [x] `ReviewDaoTest` 재작성 ✅ (2026-11-17, **교육생 요청으로 튜터 작성**, 주석 포함): `@BeforeEach`로 테스트 회원 insert(rollback됨) / `insertTest` / `searchTest` 회귀(함정 리뷰) / `searchWithoutConditionTest`(WHERE 빠짐) → 3개 통과
    - [ ] 교육생 실험: 괄호 지우고 `searchTest` 빨간불 확인 → 원복
    - (실험·이해 확인 질문은 교육생이 넘김 → 튜터가 답 정리: 본문에 키워드면 괄호 유무와 결과 같아 버그 못 잡음 / `@BeforeEach`도 같은 트랜잭션이라 rollback)
    - [ ] ⏸️ (미룸, 2026-11-17 교육생 결정) `selectDetail` / update / delete 테스트 + `@MybatisTest` 전환 → M2 완료 기준(join 상세·delete 테스트) 미충족 상태, M3 `ReviewService` 때 보강 (교육생 작성, 요구사항 제시: writer not null·name·mno, createdAt not null(DB DEFAULT) / update 후 selectByRno로 다시 꺼내 비교 / delete 후 selectByRno null / 💅 insert 도우미 메서드)
  - (이전 기록) `ReviewDaoTest` 요구사항 (요구사항 제시: `@RequiredArgsConstructor` 제거, 클래스 `@Transactional`, `MemberDao.selectByEmail`로 mno 확보, `insertTest`(반환 1 + rno 채워짐), `searchTest` 회귀(함정 리뷰 insert → minRating 4 + "너무" → 함정 rno 없음 + 전부 rating≥4), 괄호 지워 빨간불 확인)
  - [x] 셀프 퀴즈(M2 PLAN) — 교육생이 넘김, 튜터 답은 🙋 표 (복습 권장): `@Component`만 붙이면 빈이 될까? / `@Mapper` interface는 왜 주입될까? / `#{}` 대신 `${}`를 쓰면?
  - [ ] ⏸️ (미룸 → M3) DAO 테스트 통과 후 실험: `@MapperScan` 주석 처리 → 두 DAO에 `@Mapper` → 같은 테스트 통과 확인 / (선택) 섞어 쓰면 `@Mapper` 쪽이 등록 안 되는 것 확인 → 하나로 정리

### 📌 다음 세션 시작점

M2 완료 ✅ (2026-11-17) → **M3 챕터로 이동**. M3 시작 전 확인할 미룬 일(⏭️ ⏸️ 표시):
- `ReviewDaoTest`: `selectDetail`(writer) / update / delete 테스트 → M3 `ReviewService`의 수정·삭제와 함께
- `@MybatisTest` 전환 + `@Mapper` 실험 (선택)
- 교육생 실험: 괄호 지우고 `searchTest` 빨간불 확인

#### (지난) 4차 세션 종료 시점 기록 (2026-11-16)

1. **`review.xml` search 마무리부터** (3차 리뷰까지 진행, 🙋 표 참고)
   - ❌ 키워드의 `content LIKE ... OR title LIKE ...`를 **괄호로 묶기** → 안 묶으면 `(rating AND content) OR title`로 해석되어 별점 필터가 title 쪽에 안 걸림
   - 🟡 별점 `test="1 &lt;= minRating and minRating &lt;= 5"`: 동작은 함(OGNL이 null을 0으로 비교) → 의도 표현·잘못된 값 조용히 무시 문제로 `minRating != null` 권장 (교육생 선택)
   - 💅 `ORDER BY` 최신순 (`</where>` 바깥)
   - ❓ 괄호 질문 답 아직 못 들음: "괄호 없이 쓰면 어떤 리뷰가 잘못 섞이나?"
2. **`ReviewDaoTest` 리뷰** — 교육생이 세션 중 **초안을 이미 작성**(아직 리뷰 전): `@SpringBootTest` + `searchTest`(keyword "너무", minRating 1, 결과 로그 출력)
   - 튜터 메모(리뷰 때 짚을 것): 필드 `@Autowired`인데 `@RequiredArgsConstructor` 붙음 → 의미 없음(테스트는 필드 주입 or 생성자에 `@Autowired`) / `assertNotNull(reviewDao)`만 있고 **결과 검증 없음** → 로그만으로는 항상 초록 / 괄호 버그 증명 테스트: 제목에만 키워드가 있는 1점 리뷰를 insert → minRating 4로 검색 → 결과에 없어야 함 (`@Transactional`로 rollback) / 키워드만·별점만·둘 다 비움 3경우 / 생성된 SQL을 DEBUG 로그로 확인
   - 이어서 insert(rno 채워짐) / `selectDetail`(`writer.name`) / update / delete
   - ⚠️ `@MybatisTest`로 바꿀 때: `DBConfig`의 `@MapperScan`을 안 읽을 수 있음 + 내장 DB 교체(`@AutoConfigureTestDatabase(replace = NONE)`) → `@Mapper` 실험과 연결
3. 그다음: 셀프 퀴즈 3개(`#{}` vs `${}`는 검색 LIKE와 연결) → M2 완료 기준 확인 → Notion 업로드
4. 환경 메모: 앱·Workbench **root/1234**. ⚠️ STS 저장(`Ctrl + S`) 확인. 튜터는 `review.xml` XML 문법을 파이썬 파서로 확인 중(DB 직접 조회는 권한 거부됨 → 필요하면 교육생이 Workbench로 확인)

#### (지난) 3차 세션 종료 시점 기록

1. **리뷰 검색 개념 질문부터** (답 대기 중): "키워드만 입력하고 별점은 비웠다면 `WHERE`절은 어떻게 달라져야 할까? 둘 다 비웠다면?"
   - 답을 들은 뒤 → `<where>` + `<if test="...">` 개념 (빈 조건이면 `WHERE` 자체가 빠짐, 앞의 `AND` 자동 정리)
2. **검색 작성**: 검색 조건을 담을 `ReviewSearch` DTO(키워드, 최소 별점) → `ReviewDao.search(ReviewSearch)`(반환 `List<Review>`) → `review.xml`에 동적 SQL (키워드는 `LIKE CONCAT('%', #{keyword}, '%')`, 별점은 `rating >=`)
   - 튜터 메모: 셀프 퀴즈 `#{}` vs `${}`를 LIKE에서 연결 (SQL Injection) / `test` 안의 빈 문자열 검사(`keyword != null and keyword != ''`) / `int minRating`이면 0이 기본값 → `Integer`로 null 검사할지, `> 0`으로 검사할지 고민
3. **`ReviewDaoTest`** (교육생 작성): insert(rno 채워짐) / `selectDetail`(`writer` not null, `writer.name`, `imageName`·`createdAt`) / 동적 검색 / update / delete → `@MybatisTest` 슬라이스 테스트
   - ⚠️ `@MybatisTest`는 `DBConfig`의 `@MapperScan`을 안 읽을 수 있음 + 기본으로 내장 DB로 바꾸려 함(`@AutoConfigureTestDatabase(replace = NONE)` 필요할 수 있음) → `@Mapper` 실험과 연결
4. 그다음: 셀프 퀴즈 3개 → M2 완료 기준 확인 → Notion 업로드
5. 환경 메모: 앱·Workbench 모두 **root/1234**. ⚠️ STS에서 **저장(`Ctrl + S`)** 확인. 이번 세션에 `review.xml` resultMap·join select는 교육생 요청으로 **튜터가 작성**(주석 포함) → 다음 세션에 주석 읽고 이해했는지 짧게 확인

#### (지난) 2차 세션 종료 시점 기록

1. **`ReviewDao` 리뷰부터**: 저장된 선언 4개(`insert` / `seletByRno` / `update(int rno, Review review)` / `delete`)를 한 줄씩 리뷰
   - 튜터 메모(리뷰 때 짚을 것): 메서드 이름 오타 `selet` → `id`와 짝이 맞아야 함 / `update`의 파라미터 2개 → `Review` 안에 이미 `rno`가 있음 + 파라미터가 2개 이상이면 MyBatis에서 `#{}` 이름을 어떻게 찾을지(`@Param`) / 💅 interface 메서드의 `public`은 생략 가능 (`MemberDao` 리뷰 때와 같음)
2. **`review.xml` SQL 4개** 작성: `insert` 한 쌍 먼저 → 리뷰 → 나머지 3개 (`keyProperty="rno"`, DEFAULT 컬럼 빼기, update·delete의 `WHERE` 필수, `resultType`은 alias `Review` 가능)
3. 그다음: 리뷰 상세 join(`resultMap` + `<association>`, `Review`에 `Member` 필드 추가: Q2-2 답) → 검색(`<where>` + `<if>`) → `@MybatisTest` DAO 테스트
   - ⚠️ `@MybatisTest`는 슬라이스 테스트라 별도 `@Configuration`(`DBConfig`)의 `@MapperScan`을 안 읽을 수 있음 → 이때 `@Mapper` 실험과 연결 (위 ⏭️ 참고)
4. 환경 메모: 이 컴퓨터는 앱·Workbench 모두 **root/1234**. STS의 `.properties` 인코딩은 UTF-8로 설정 완료. ⚠️ STS에서 작성 후 **저장(`Ctrl + S`)** 확인 (이번 세션에 미저장 상태로 리뷰 요청이 두 번 있었음)

---

## M3. MVC 요청 처리 — ✅ 완료 (2026-11-22) · [Notion](https://app.notion.com/p/41f6412b0acd815eaaedc11cfc01c322)

### ✅ 한 일

- M3 시작 (2026-11-17)
- **3-1 로그인·로그아웃 완료** ✅ (2026-11-17): `MemberService.login`(통합 실패 메시지, password null) + `MemberServiceTest` 5개(튜터 작성) + `MemberController`(`/member/signup`·`/member/login` GET/POST 통합, `POST /member/logout` + `invalidate()`) + `login.jsp` + `index.jsp`(`<c:choose>`, 로그아웃 POST 폼) → 브라우저 확인 4가지 통과
- **3-2-① 리뷰 목록(검색) 완료** ✅ (2026-11-17): `ReviewService`/`ReviewServiceImpl`(search 위임) + `ReviewController` `GET /review/list`(`@ModelAttribute ReviewSearch`, `addAttribute("reviewList")`) + `review/list.jsp`(GET 검색 폼, `<c:choose>` 0건, `<c:forEach>` + `varStatus`) + index 링크 → 2차 통과, 브라우저 확인 5가지 통과 (조건 없음·키워드만·별점만·둘 다·빈 별점)
- **3-2-② 리뷰 상세 완료** ✅ (2026-11-17): Service `selectDetail`(DAO `selectDetail` JOIN 재사용, null → `IllegalArgumentException`) + `GET /review/{rno}`(`@PathVariable int rno`, `IllegalArgumentException`만 catch → `error`) + `review/detail.jsp`(`<c:choose>`, `${review.writer.name}`) + 목록 제목 링크(`item.rno`) → 3차 통과, 브라우저 4가지 통과 (클릭·없는 번호·`abc` 400·`/list` 우선)
- **3-2-③ 리뷰 작성 완료** ✅ (2026-11-18): `GET·POST /review/write`, 작성자 mno는 세션 `loginUser`에서, 로그인 검사, insert 후 `getRno()` → `redirect:/review/` + rno (PRG), `addFlashAttribute("msg")` 등록 완료 메시지, `write.jsp`(`<textarea>`) → 3차 통과
- **3-3 작성자 검사 + 3-2-④ 삭제 완료** ✅ (2026-11-19): `ForbiddenException`, Service private `checkReviewMno`(DB 원본으로 비교) → `deleteReview`·`updateReview`, `POST /review/{rno}/delete`(없음 → 목록, 남의 글 → 상세, flash `msg`), 삭제 버튼 작성자에게만(`<c:if>`) → 5차 통과, 브라우저 4가지
- **3-2-④ 리뷰 수정 완료** ✅ (2026-11-22): 데스크탑 이동으로 초안 유실 → 튜터가 Service `getReviewForEdit`·`GET /{rno}/edit`·`edit.jsp` 작성, 교육생이 `POST /{rno}/edit` + 상세 수정 링크 작성 → 1차 리뷰(🔴 `updateReview` 두 번 호출) 후 튜터 수정 → 브라우저 5가지 통과 (리뷰 2 수정 포함)
- **`ReviewDaoTest` 보강** ✅ (2026-11-22, 튜터 작성): `selectDetail`(association)·`update`(다시 조회)·`delete`·없는 번호(행 수 0) → 전체 16개 초록
- 셀프 체크 3문제 (Q1·Q2 정답, Q3 보충)

### 📚 배운 개념

**1. 세션과 로그인**
- HTTP는 무상태 → 로그인 성공 시 Tomcat이 서버 메모리에 **세션 저장소**를 만들고 그 번호를 `JSESSIONID` 쿠키로 보냄 → 브라우저가 매 요청에 자동 첨부 → 서버가 번호로 저장소를 찾아 "같은 사람" 판단
- 세션 저장소는 Spring이 아니라 **WAS(Tomcat)** 영역. Controller는 `HttpSession`으로 넣고(`setAttribute`) 빼고(`getAttribute`) 끝낼(`invalidate`) 뿐
- 세션에는 필요한 것만: **`password`는 넣지 않는다** (로그인 비교 후 쓸 일 없음, `${loginUser}`·로그의 `toString()`으로 노출) → 세션에 넣기 전 `password`를 비움. 실무는 해시 저장(`PasswordEncoder`, 범위 밖)

**2. 실패 시 redirect하지 않는 이유 (Model의 수명)**
- `Model` = **요청 한 번**만 사는 메모지. 뷰를 바로 반환하면 같은 요청 안에서 JSP가 읽음 → 에러 보임
- redirect = 브라우저에 "이 주소로 다시 와" → **새 요청** → 새 Model → 에러 메시지 사라짐
- 성공 정보는 **세션**에 넣었으므로 redirect해도 유지 (PRG). 실패한 로그인은 DB를 안 바꿔서 F5 재전송도 무해
- redirect + 메시지 유지가 필요하면 `addFlashAttribute` (다음 요청 한 번만 유지, 3-3에서)

**3. 로그아웃은 `invalidate()` (호텔 방 비유)**
- 세션 = 호텔 방, JSESSIONID = 카드키, `loginUser` = 방 안 신분증
- `removeAttribute` = 신분증만 치움: 다른 값 남음(공용 PC 다음 사용자) / 값 늘 때마다 지울 목록 관리 / **같은 세션 ID 계속 유효**(탈취 시 위험)
- `invalidate()` = 체크아웃: 전부 비우고 ID 무효 → 다음 요청에 새 ID. 로그아웃 의도에 맞고 빠뜨릴 위험 없음
- `removeAttribute`는 로그인 유지 중 임시 값 하나 치울 때

**4. 경로 변수 `/review/3` (`@PathVariable`)**
- 주소 = `/review` + `/3`. 리뷰마다 주소가 따로 있는 게 아니라 **틀 하나**(`/review/{rno}`)로 받는다. `{rno}` = "이 자리는 아무 값, 이름은 rno"
- 받기: 메서드 파라미터에 `@PathVariable` + 중괄호 이름과 같은 변수 이름 → 주소의 `"3"`(글자)을 `int 3`으로 바꿔 줌
- `@RequestParam`과 비교: `?rno=3`의 이름표 붙은 값 vs 경로 **자리**의 값. 둘 다 이름 맞추기 + 자동 형 변환
- 만드는 쪽(JSP 링크): `${root}/review/${item.rno}` 처럼 번호를 경로에 이어 붙임

**5. 작성자는 폼이 아니라 세션에서 (클라이언트를 믿지 않는다)**
- 폼으로 온 값(hidden 포함)은 **사용자가 마음대로 바꿀 수 있다**: F12로 `value` 수정, Postman·curl로 직접 POST → `mno=1`로 바꾸면 남의 이름으로 리뷰 작성(사칭)
- hidden은 "화면에 안 보일 뿐" 보안이 아니다. 브라우저에서 온 값 = 전부 사용자가 만든 값
- 세션 값은 **서버 메모리**에 있어 사용자가 못 바꿈. 사용자가 가진 건 JSESSIONID(열쇠)뿐 → "누가"는 세션 `loginUser`에서, "무엇을 썼는지"(제목·내용·별점)만 폼에서
- 원칙: 권한·신원과 관련된 값은 서버가 아는 정보로 정한다 (수정·삭제 권한 검사도 같은 이유 → 3-3)

**6. PRG (Post → Redirect → Get)와 F5**
- F5(새로고침) = 브라우저가 **마지막으로 보낸 요청을 그대로 다시** 보냄
- POST 처리 후 뷰를 바로 반환 → 마지막 요청 = POST → F5마다 다시 INSERT (중복 글), 브라우저가 "양식 다시 제출?" 경고
- POST 처리 후 redirect → 브라우저가 GET으로 새 요청 → 마지막 요청 = GET(조회) → F5 해도 다시 보기만 함
- 그래서 **DB를 바꾸는 요청(작성·수정·삭제) 성공 후엔 redirect**. 실패(입력 오류)는 DB를 안 바꿨으니 뷰를 바로 반환해 Model로 에러 표시 (개념 2)

**7. `addFlashAttribute` (redirect 너머로 메시지 한 번 보내기)**
- `RedirectAttributes.addFlashAttribute("msg", …)` → redirect 직전 **세션에 잠깐 보관** → 다음 요청에서 Model로 옮겨지고 세션에서 바로 지워짐 → JSP에서 `${msg}`
- 그래서 F5(새 요청)하면 메시지가 사라짐. 실패·성공 모두 같은 이름 `msg`로 통일하면 받는 JSP가 하나의 `<c:if test="${not empty msg}">`로 충분
- redirect 주소의 `{rno}`는 Model이 아니라 **현재 요청의 경로 변수**로 채워짐 (`POST /review/{rno}/edit` 안에서는 `redirect:/review/{rno}` 가능, `/write`처럼 경로 변수가 없으면 500)

**8. 권한 검사는 Service에 (규칙의 주인)**
- "작성자 본인만 삭제" = 화면 규칙이 아니라 **비즈니스 규칙** → 비즈니스 규칙을 책임지는 계층 = Service
- Controller에 두면: 삭제를 부르는 곳(웹 화면, 나중의 REST API, 관리자 기능, 테스트…)이 늘 때마다 **같은 `if`를 복사** → 한 곳이라도 빠뜨리면 구멍
- Service에 두면: 누가 부르든 `deleteReview`를 거치므로 **한 곳에서 항상 지켜짐**. Controller는 "누가 요청했는지(세션 mno)"만 넘기고 판단은 Service가
- 비교 대상: 요청자 mno = 세션, 작성자 mno = **DB 재조회** (폼 값은 믿지 않음, 개념 5)
- 위반 시 **커스텀 예외**: `IllegalArgumentException`(입력이 잘못됨)과 뜻이 다름 → 이름만 보고 "권한 없음"을 알 수 있고, M5에서 `@ExceptionHandler`로 403 페이지에 연결
- `<c:if>`로 버튼을 숨기는 건 **화면 편의**일 뿐, 주소창에 직접 입력하면 그대로 요청됨 → 진짜 차단은 Service
- DAO는 없는 번호로 update·delete해도 예외 없이 **행 수 0**만 돌려줌 → Service가 먼저 조회해서 의미 있는 예외로 바꿈

**9. "동작한다 ≠ 맞다" (테스트 데이터 고르기)**
- 수정 POST에서 `updateReview(rno, review)`(mno 자리에 rno)를 한 번 더 부른 버그가 리뷰 1(rno 1 = mno 1)에서는 **우연히 통과** → 리뷰 2에서 500
- 서로 다른 의미의 값이 겹치지 않게, 성공 하나가 아니라 두 번째 데이터·남의 글·없는 번호까지 확인. 수정 테스트는 **원래 값과 확실히 다른 값**으로, 결과는 **DB에서 다시 꺼내** 비교

### 🙋 내 답변과 피드백

| 질문 | 내 답 | 피드백 |
|---|---|---|
| Q1. 무상태 HTTP에서 로그인한 사람을 어떻게 아나? | JSESSIONID 쿠키 값을 보낸다 | ✅ 정답. 보충: 세션 저장소는 Tomcat 메모리, 쿠키는 그 번호(열쇠) |
| Q2. `Member`를 그대로 세션에 넣으면 문제 필드는? | 모르겠어요 | → `password`. 세션은 로그인 내내 남고 `toString()`·`${loginUser}`로 노출 → 넣기 전에 비운다 |
| `MemberServiceImpl.login` 1차 | `mem == null \|\| !password.equals(...)` → `throw new IllegalArgumentException()`, `setPassword("")`, `log.info` | 👍 guard clause, 두 실패를 한 길로(계정 존재 여부 노출 방지), `\|\|` 순서(short-circuit로 NPE 방지), `equals`, 비밀번호 로그 없음. ❌ 예외 메시지 없음 → Controller `e.getMessage()`가 null, 폼에 메시지 안 뜸 → 통합 메시지 하나 / 🟡 `""` 대신 `null`(값 없음, 테스트도 null 검증) / 💅 `mem.getPassword().equals(password)`(DB NOT NULL 쪽 기준), 로그에 `mno=` 이름 |
| `MemberServiceImpl.login` 2차 | 통합 메시지 `"이메일 또는 비밀번호가 틀렸습니다."`, `setPassword(null)`, `mem.getPassword().equals(password)` | ✅ **통과**. 3가지 모두 반영 (계정 열거 방지 포함) |
| `MemberServiceTest` 로그인 1차 | `@BeforeEach`로 `setMem`(new@) 가입, `login_success`, 실패 2개는 try-catch + 로그 | 실행: 5개 중 **`signUp_duplicateEmail` 실패** (kim@matzip.com이 DB에 없는 듯 → 예외 안 남). 👍 `@BeforeEach` 직접 도입, `signUp_success` 이메일 분리, password null 검증. ❌ 실패 테스트 try-catch = 예외 삼킴 → **항상 초록**(M2 1차와 같은 실수) → `assertThrows` + 메시지(두 메시지 같은지) / ❌ duplicate 테스트가 DB 데이터에 의존 → `setMem` 이메일로 재가입 / 🟡 `assertNotNull` 먼저, `assertEquals(기대, 실제)` 순서 / 💅 `setMem.getEmail()` 재사용 |
| `MemberServiceTest` 로그인 2차 | (튜터에게 수정 요청) | → 튜터 작성(주석 포함): `setMno` 필드, `setUp` assert 제거, duplicate는 `setMem.getEmail()`로 재가입, `login_success` assert 순서(`assertNotNull` 먼저, 기대·실제), 실패 2개 `assertThrows` + 상수 `LOGIN_FAIL_MESSAGE`로 같은 메시지 검증 → **초록 5개** |
| `MemberController` 로그인·로그아웃 1차 | 회원가입 URL `/member/signup`으로 통합(+index 링크), `GET/POST /member/login`, `@RequestParam` 2개 + `HttpSession`, 실패 시 error + 뷰, 성공 시 `loginUser` + `redirect:/`, **`POST /member/logout`** + `invalidate()` | ✅ **통과**. 👍 URL 규칙 통일, `login` 파라미터 모양에 맞춘 `@RequestParam`, 성공 시에만 세션 저장, 로그아웃을 POST로(정석 선택). ⚠️ JSP에서 로그아웃은 링크가 아니라 POST 폼 (링크면 405). 💅 logout의 미사용 `Model`. 🤔 2개 미답: 실패 시 redirect 안 하는 이유 / `invalidate` vs `removeAttribute` |
| JSP 1차 (`login.jsp`, `index.jsp`) | `login.jsp`: signup 복사, name=email/password, `type=password`, error `<c:if>` / `index.jsp`: `<c:choose>` + `${empty loginUser}`, `${loginUser.name}`, 로그아웃 POST 폼 | `index.jsp` ✅ 통과 (💅 href 없는 `<a>` → `<span>`, 들여쓰기). ❌ `login.jsp`에 **taglib 선언 누락** → `<c:if>`·`<c:set>`이 글자로 출력(에러 없음): `<p>` 항상 렌더, `${root}` 빈 값인데 우연히 동작 (M2 ❓와 같은 상황, 페이지 소스 보기로 확인) / 💅 17행 주석 "Member의 필드" → `@RequestParam` 이름 |
| JSP 2차 + 브라우저 확인 | taglib 추가, 확인 4가지 모두 통과 | ✅ **3-1 완료** |
| 🤔 실패 시 redirect 안 하는 이유 / `invalidate` vs `removeAttribute` | 모르겠어, 쉽고 자세히 | → 개념 2·3 (Model 수명 그림, 호텔 방 비유) |
| 3-2-① 목록 1차 (`ReviewService`·`ReviewController`·`list.jsp`·index 링크) | Service 1메서드 위임, `GET /review/list` + `@ModelAttribute ReviewSearch`, `model.addAllAttributes(reviewList)`, `return "/review/list"`, JSP 검색폼(`required`) + `<c:choose>`/`<c:forEach>` | 👍 Service 계층 구조·생성자 주입, `@ModelAttribute`로 검색 DTO 바인딩, taglib·`${root}` 선언, 0건 처리, `varStatus`. ❌ **`addAllAttributes(리스트)`** = 원소마다 자동 이름(`review`)으로 넣음 → `reviewList`라는 이름은 Model에 없음 → `addAttribute("이름", 값)` / ❌ JSP 이름 3곳 불일치(`resultList`·`reviewList`·Controller) → 항상 "결과 없음" / ❌ `required` → 조건 없는 검색·한쪽만 검색 불가(선택 조건이라는 요구사항과 충돌) / 🟡 form `action`에 `${root}` 빠짐 / 🟡 `return "review/list"`(앞 `/` 빼는 관례) / 💅 "(아이디)" 복붙 주석, 별점 `min`/`max`, 검색값 유지(`${reviewSearch.keyword}`) / Q1 미답 |
| 3-2-① 목록 2차 | `addAttribute("reviewList", …)`, JSP 이름 통일, `required` 제거, `${root}` 추가, `"review/list"` | ✅ ❌3개·🟡2개 모두 반영 → 코드 통과. 💅(주석 "(아이디)", `min`/`max`, 검색값 유지)는 선택으로 남김 / Q1·브라우저 확인 5가지 대기 |
| 3-2 Q1. 상세는 왜 `/review/3`(경로), 검색은 왜 `?keyword=…`(쿼리 스트링)? | 모르겠어 | → **경로 = "어떤 것"(자원을 가리킴, 필수, 하나)**: 3번 리뷰라는 주소. **쿼리 스트링 = "어떻게 볼지"(걸러 보기·정렬·페이지, 선택, 여러 개 조합)**: 빼도 같은 목록 페이지. 검색 조건을 경로에 넣으면 빠질 때 `/review/list//4`처럼 깨지고 순서도 고정됨. 받는 쪽: 경로 → `@PathVariable`, 쿼리 → `@RequestParam`/`@ModelAttribute` |
| 3-2-② Q. 상세에는 `selectByRno` / `selectDetail` 중 무엇? | `selectDetail`, 작성자 이름이 필요해서 | ✅ 정답. `selectByRno`는 `review` 테이블만 → `writer`가 null. `selectDetail`은 JOIN + `<association>`으로 `writer`(Member)까지 채움 → `${review.writer.name}` |
| 3-2-② 상세 1차 (`detail.jsp` + 함께 본 Service·Controller·list 링크) | Service `selectDetail` 위임, `@GetMapping("/{rno}")` + `@PathVariable int rno`, `addAttribute("review")`, list 제목 `<a href="${root}/review/${item.rno}">`, detail.jsp 표에 `${title}`·`${writer.name}` 등 | 👍 경로 변수 매핑·이름 일치, 링크에 `status.count` 아닌 `rno`, taglib·`root`·목록 링크. ❌ JSP가 `${title}`처럼 **바로 필드 이름** → Model엔 `review` 하나뿐 → 모두 빈칸 (`list.jsp`의 `${item.title}`처럼 `review.`부터) / ❌ "점"이 본문(26행)에 붙고 별점엔 없음 / ❌ 없는 번호 흐름 3곳 모두 빠짐: Service null 검사·throw, Controller try-catch + `error`, JSP `<c:choose>` → 지금 `/review/9999`는 빈 표 / 💅 Service 메서드 이름 `selectDetail`은 DAO(SQL) 말투 → `getReview` 등 "하는 일" 이름, `<title>` 한글로 |
| 3-2-② 상세 2차 | Service null → `IllegalArgumentException("찾을 수 없는 리뷰 입니다.")`, Controller try-catch + `error`, JSP `${review.title}`… + `<c:if>` 에러 | 👍 Service guard clause·메시지, Controller 실패 시 error + 같은 뷰(로그인과 같은 흐름), `${review.writer.name}` 두 단계 점. ❌ "점"이 아직 본문(31행)에 붙어 있음 / 🟡 `<c:if>`가 `<table>` 안(`<thead>` 앞) → HTML상 표 안에 `<p>` 불가(브라우저가 표 밖으로 끌어내 우연히 보임) + 에러 때도 빈 표가 같이 나옴 → `<c:choose>`로 표를 otherwise에 / 🟡 `catch (Exception e)` → 너무 넓음: DB 오류 같은 진짜 버그도 "안내 메시지"로 삼켜짐 → Service가 던지는 `IllegalArgumentException`만 (MemberController처럼) + catch에 로그 / 💅 Service 26행 앞 공백+탭 섞임, 이름 `selectDetail`·`<title>` 그대로(선택) |
| 3-2-② 상세 3차 | "점" → 별점, `<c:choose>`(error / 표), `catch (IllegalArgumentException e)`, 포맷 정리 | ✅ **코드 통과**. 💅 catch 안 로그는 아직 없음(선택) / 브라우저 확인 4가지 결과 대기 |
| 3-2-② 브라우저 확인 | `/review/9999` → "찾을 수 없는 리뷰 입니다" / `/review/abc` → Whitelabel Error Page / `/review/list` → 목록 그대로 (이유는 미답) | ✅ 3개 기대대로. → `abc`: `int`로 못 바꿈 → **메서드 실행 전에** Spring이 400(타입 불일치) → 우리 try-catch까지 못 옴, Whitelabel = Boot 기본 에러 페이지(나중에 `@ExceptionHandler`로 바꿀 수 있음) / `list`: Spring은 **글자가 고정된 경로(`/list`)를 변수 경로(`/{rno}`)보다 먼저** 고름(더 구체적인 것 우선) → 앞으로 `/review/write`도 안전. 제목 클릭(1번)도 정상 → **3-2-② 완료** |
| 3-2-③ Q1. 작성자 `mno`를 폼 hidden으로 받으면 안 되는 이유? | 모르겠어요 | → 폼 값은 F12·Postman으로 **바꿔 보낼 수 있음** → 남의 mno로 사칭 작성. hidden은 숨김일 뿐 보안 X (개념 5) |
| 3-2-③ Q2. 그럼 mno는 어디서? | 세션에서 꺼내야 할까? | ✅ 정답. 로그인 성공 때 세션에 넣은 `loginUser`(Member)의 `mno`. 세션은 서버 메모리라 사용자가 못 바꿈 |
| 3-2-③ 작성 1차 (Service·Controller·`write.jsp`·list 링크) | Service `writeReview`: insert 결과 0이면 `IllegalArgumentException`, `return review.getRno()` / Controller GET·POST `/write`, try-catch, `return "redirect:/review/rno"` / JSP hidden `mno`(`${sessionScope.loginUser.mno}`), 입력 4개 `required` | 👍 `getRno()`로 새 번호(직전 질문 반영), GET·POST 같은 URL, taglib·`${root}`·`method="post"`, name = 필드 이름, 목록에 "리뷰 쓰기" 링크. 컴파일 OK. ❌ **hidden `mno`** = Q1에서 막자고 한 방식 그대로 → Controller에서 `HttpSession`의 `loginUser`로 `mno` 채우기 / ❌ `"redirect:/review/rno"` = 글자 "rno" → `/review/rno` → `int` 변환 실패 400 (변수를 문자열에 이어 붙여야) / ❌ 로그인 검사 없음 (GET·POST 둘 다) / 🟡 insert는 성공=1, 실패=DB 예외 → `res == 0`은 사실상 안 옴 + `IllegalArgumentException`은 "입력이 잘못됨" 뜻 → 검사·try-catch 빼고 위임만 / 🟡 `return "/review/write"` 앞 `/` / 🟡 이미지 입력 `required` → 업로드는 나중, 지금은 입력칸 빼기(`image_name` NULL 허용) / 🟡 내용 `<textarea>`, 별점 `min`/`max` 1~5 / 💅 디버그 로그 `CHECK!!!` 지우기 |
| 3-2-③ 작성 2차 | Controller GET·POST에 `HttpSession` + `loginUser == null` → `redirect:/member/login`, `review.setMno(loginUser.getMno())`, `"redirect:/review/{rno}"` / JSP hidden 삭제 | 👍 ❌1(hidden 삭제 + 세션에서 mno)·❌3(로그인 검사 2곳, guard clause, 형 변환) 해결. ❌ `"redirect:/review/{rno}"`도 여전히 **문자열 안의 글자** → Spring이 `{rno}`를 **Model**에서 찾음(지역 변수는 모름) → Model에 `rno` 없음 → `IllegalArgumentException: Model has no value for key 'rno'` **500** → `+`로 변수 이어 붙이기 / 🟡 4·5·6·7(Service `res == 0`·try-catch, `"/review/write"`, 이미지 입력칸, textarea·min/max) 아직 그대로 |
| 3-2-③ 작성 3차 | `"redirect:/review/" + rno`, Service 위임만(`insert` → `getRno()`), try-catch 제거, 이미지 칸 삭제, 별점 `min=1 max=5`, 제목·내용 `<input type="textarea">` | ✅ **코드 통과** (❌ 모두 해결, 컴파일 OK). 🟡 `<input type="textarea">`는 없는 type → 브라우저가 그냥 한 줄 text로 처리(에러 없음). 여러 줄은 **`<textarea>` 태그**(닫는 태그 있음, value 속성 대신 태그 사이가 값). 제목은 한 줄이라 `type="text"` 유지 / 💅 미사용 `Model` 파라미터, 속성 값 따옴표(`min="1"`) / 브라우저 확인 4가지 대기 |
| 3-2-③ 브라우저 4. 작성 직후 F5 → 글이 또 생기나? | 아니오, redirect 했기 때문 | ✅ 정답 (DB 확인: rno 12·13·14 mno=1, 중복 없음). 보충: F5 = **마지막 요청을 다시 보냄**. redirect 후 마지막 요청은 `GET /review/14`(조회) → 몇 번 눌러도 무해. 뷰를 바로 반환했다면 마지막 요청이 `POST /review/write` → F5마다 INSERT(브라우저 "양식 다시 제출" 경고) = **PRG**(Post → Redirect → Get). 참고: rno가 3~11을 건너뛴 건 테스트 롤백에도 AUTO_INCREMENT 번호는 되돌아가지 않기 때문 |
| 3-2-③-2 Q. redirect 직전 `model.addAttribute("msg")` → 상세에서 `${msg}` 보이나? | 안 보여요. redirect하면 새 요청이라 Model이 사라져서 | ✅ 정답 (개념 2와 연결). → 해결책 `addFlashAttribute` (개념 7) |
| 3-2-③-2 flash 1차 | `writeReview`에 `RedirectAttributes` 추가·`Model` 제거, redirect 직전 `addFlashAttribute("msg", "정상적으로 리뷰가 등록 되었습니다.")`, `detail.jsp` otherwise 안 표 위에 `<c:if test="${not empty msg}">` | ✅ **코드 통과** (컴파일 OK). 👍 담는 시점(저장 성공 후·redirect 직전), `not empty`, 위치(표 밖·error 아닐 때만), 일반적인 이름 `msg`. 💅 변수 오타 `rediectAttr` → `redirectAttr`(STS `Alt+Shift+R` 이름 바꾸기) / 🟡 `write.jsp` 내용 칸 아직 `<input type="textarea">` (2번째 지적) / 브라우저 확인 3가지 대기 |
| 3-2-③-2 flash 2차 | `redirectAttr`로 이름 수정, `write.jsp` 제목 `type="text"`·내용 `<textarea>` | ✅ **통과**. 남은 🟡·💅 모두 반영, DB에 rno 15·16 작성 확인 → **3-2-③ 작성 + 완료 메시지 완료** |
| 3-3 Q1. 작성자 비교에 필요한 두 `mno`는 어디서? | 세션 | 🟡 절반 정답. **요청한 사람** = 세션 `loginUser.mno` ✅ / **리뷰 쓴 사람** = **DB**에서 `selectByRno(rno)`로 다시 조회한 `mno` (폼으로 온 `review.mno`는 사칭 가능 → 믿지 않음) |
| 3-3 Q2. 권한 검사를 Controller가 아니라 Service에 두는 이유? | 모르겠어 | → 개념 8 (Service = 규칙의 주인, 호출하는 곳이 늘어도 한 곳에서 지켜짐) |
| 3-3 단계 A 1차 (`ForbiddenException` + Service `deleteReview`·`updateReview`) | 예외 `extends RuntimeException`(본문 비어 있음) / delete: `selectByRno` → null이면 `IllegalArgumentException` → `mno !=` 이면 `ForbiddenException` → `delete` / update: `review.setMno(mno)` 후 바로 `update` | 👍 `exception` 패키지·`RuntimeException`, delete 흐름 3단계 정확(`selectByRno` 선택, DB 원본으로 비교, `int`끼리 `!=` OK). ❌ **update에 검사 없음** → 로그인한 누구나 남의 리뷰 수정 가능 (UPDATE SQL의 WHERE는 `rno`뿐이라 `setMno`는 아무것도 막지 못함) / ❌ 예외에 **메시지 생성자 없음** → `e.getMessage()`가 null / 🟡 공통 흐름 `private` 메서드로 / 🟡 `log.warn` 없음 / 💅 파라미터 순서 통일(delete는 rno·mno, update는 mno·review), 이름 `loginMno`, 남은 주석 2줄 / 🤔 imageName 질문 미답 |
| 3-3 단계 B 1차 (삭제 Controller + detail 버튼) | `POST /{rno}/delete`, 로그인 검사, try에서 `deleteReview(rno, loginUser.getMno())`, catch 2개 → `addFlashAttribute("fail")`, 그 뒤 `addFlashAttribute("success", "삭제 완료")` → `redirect:/review/list` / detail.jsp 맨 아래 POST 폼 + 버튼 | 👍 URL·`@PostMapping`·`@PathVariable`, 로그인 검사, mno는 세션에서, 예외 2종 구분, 링크 대신 POST 폼, `${root}` + `${review.rno}`. ❌ **catch 뒤에도 코드가 계속 실행** → 실패해도 "삭제 완료"가 같이 담김 (catch 안에서 `return` 또는 성공 처리를 try 안으로) / ❌ `ForbiddenException`은 상세로 redirect해야 하는데 목록으로 감 / ❌ `list.jsp` 수정 없음 → 메시지가 안 보임 + 이름 `fail`·`success`는 detail.jsp(`msg`)와도 안 맞음 → 이름 정하고 받는 JSP와 맞추기 / 🟡 form 태그 `method="POST">>` → `>` 글자가 화면에 출력 / 🟡 폼이 `<c:choose>` 밖 → 없는 리뷰(error)일 때도 버튼이 나오고 action이 `/review//delete` → otherwise 안으로 / 💅 `rediAttr` → `redirectAttr`(write와 통일), 파라미터 사이 공백 2칸, 폼 `name` 불필요, `confirm` 선택 / 🤔 POST 이유·imageName 미답 |
| 3-3 단계 B 2차 | catch 2개 안에 `return "/+rno";` 추가 | 👍 ❌1 방향 맞음(catch 안 `return` → 실패 시 "삭제 완료" 안 담김). ❌ **`"/+rno"`는 전부 따옴표 안 = 글자 그대로** (변수 아님) + `redirect:` 없음 → **뷰 이름**으로 해석 → `/WEB-INF/views//+rno.jsp` 찾다 404. 3-2-③ 2차 `"redirect:/review/{rno}"`와 같은 실수 → `"redirect:/review/" + rno` 모양(writeReview 맨 아래 참고) / ❌ `IllegalArgumentException`도 상세로 보냄 → 없는 리뷰는 목록으로 / ❌3 `list.jsp`·flash 이름(`fail`/`success` vs `msg`) 그대로 / 🟡4 `>>`·🟡5 폼 위치 그대로 / 💅 그대로 |
| 3-3 단계 B 3차 | flash 이름 `msg`로 통일, `"redirect:/"+rno`, list.jsp `<c:if test="${empty msg}">`, detail.jsp `>>` 수정·폼을 `<c:otherwise>` 안으로 | 👍 변수를 따옴표 밖 + `redirect:`, `msg` 통일(detail.jsp 그대로 재사용), 🟡4·5 해결. ❌ `"redirect:/"+rno` → `/3` (`/review` 빠짐) → 매핑 없음 404 / ❌ `IllegalArgumentException`은 아직 상세로 → 목록으로 / ❌ list.jsp `empty msg` = **조건 반대** (메시지 없을 때만 빈 `<p>` 출력, 있을 때 숨김) → `not empty` / 💅 실패 메시지도 초록색(선택), `rediAttr`·공백 2칸·`name` 그대로 |
| 3-3 단계 B 4차 | `IllegalArgumentException` → `"redirect:/list"`, `ForbiddenException` → `"redirect:/review"+rno`, list.jsp `not empty msg` | 👍 list.jsp 조건 해결, 예외별 갈 곳 구분. DB에서 rno 12~16 삭제됨 → 본인 삭제 성공 경로는 동작한 듯. ❌ `/list` → `/review` 빠짐 (404) / ❌ `"/review"+rno` → `/review3` (`/` 하나 빠짐, 404). redirect 주소 = 앱 루트부터 전체 경로, 성공 줄 `"redirect:/review/list"`·writeReview 줄과 같은 모양으로 |
| 3-3 단계 B 5차 | `"redirect:/review/list"`, `"redirect:/review/"+rno` | ✅ **코드 통과** (컴파일 OK). 예외별 갈 곳·redirect 전체 경로 모두 맞음. 💅 `rediAttr`·공백 2칸·폼 `name` 그대로(선택) / 브라우저 확인 4가지 대기 |
| 3-3 단계 B 브라우저 확인 | 4가지 모두 통과 (본인 삭제·남의 글 차단·로그아웃 상태·F5) | ✅ **삭제 기능 완료**. DB 확인: rno 1·2(mno=1) 그대로 = test 계정의 삭제가 Service에서 막힘, AUTO_INCREMENT 23 = 새 리뷰를 써서 삭제까지 확인. F5 이유 설명은 대기 |
| 3-3 단계 B-④ `<c:if>` 1차 | `<c:if test="review.mno == {sessionScope.loginUser.mno}">` | 👍 비교할 두 값(리뷰 작성자 mno ↔ 세션 loginUser mno) 정확, `sessionScope.`로 어디서 꺼내는지 명시. ❌ **`$` 없음** → EL이 아니라 그냥 **글자** → `test`가 글자를 boolean으로 바꾸면 항상 `false` → 버튼이 **늘 숨겨짐** (에러 없음). 확인 2·3번은 우연히 통과, 1번(본인)에서만 드러남 / ❌ 식 전체를 `${ }` **하나**로 감싸야 함 (일부만 `{}`로 감싸는 문법 없음) |
| 3-3 단계 B-④ `<c:if>` 2차 | `test="${review.mno} == ${sessionScope.loginUser.mno}"` | 👍 `$` 붙임. ❌ `${ }`가 **두 개** → 각각 계산된 뒤 사이의 ` == `는 글자로 남아 **이어 붙인 문자열** `"1 == 1"`이 됨 → boolean 변환 시 `"true"`가 아니면 `false` → 여전히 버튼이 늘 숨겨짐. `==`(비교)는 `${ }` **안**에 있어야 계산됨 → 식 전체를 `${ }` 하나로 |
| 3-3 단계 B-④ `<c:if>` 3차 | `test="${review.mno == sessionScope.loginUser.mno}"` | ✅ **통과**. 연산자까지 `${ }` 하나 안 → true/false. 교훈: `${ }` 밖은 전부 글자, "보여야 하는 경우"(본인)도 꼭 확인 |
| Q. 로그아웃 상태에서 `${sessionScope.loginUser.mno}`는? | 다른 값이 됨 | 🟡 방향 맞음, 더 정확히: EL은 **null에 안전** → `loginUser`가 없으면 `.mno`에서 NPE 대신 **`null`** (화면 출력은 빈 문자열) → `1 == null` = `false` → 버튼 숨김. Java였다면 `loginUser.getMno()`에서 NullPointerException |
| Q. 삭제 직후 F5에서 일어난 일과 이유? | "삭제 완료" 문구가 사라짐, flash에 넣었기 때문 | ✅ 정답. 보충: flash = **redirect 다음 요청 한 번**만 살아 있고 꺼내면 지워짐 → F5 = `GET /review/list` 새 요청 → 이미 소비돼 없음. 그리고 PRG 덕분에 마지막 요청이 GET이라 **삭제가 다시 실행되지 않음**(이미 지운 rno로 재삭제 X) |
| 3-3 단계 C Q. 삭제는 POST 폼인데 "수정" 버튼은 링크(GET)여도 되는 이유? | 삭제는 데이터 변경이 발생, "수정" 버튼은 수정 폼으로 이동일 뿐 | ✅ 정답. 보충: 기준은 "버튼 이름"이 아니라 **그 요청이 DB를 바꾸는가**. 수정 링크 = 폼 **보여 주기**(조회) → GET, 실제 저장은 폼 제출 = `POST /review/{rno}/edit`. 💅 삭제 요청은 내용을 담지 않고 경로의 `rno`만 보냄 |
| 3-3 단계 B Q. 삭제를 GET 링크가 아니라 POST 폼으로 만드는 이유? | 데이터가 변경되는 요청이기 때문에 | ✅ 정답 (핵심). 보충: GET = "조회만 하는 안전한 요청"이라는 약속 → 브라우저 미리 불러오기·검색 로봇·주소 공유·방문 기록·`<img src="…/delete">` 한 줄로도 요청될 수 있음 → 삭제가 의도치 않게 실행. 로그아웃을 POST로 만든 이유와 같음 |
| 3-3 Q. 사진 있는 리뷰를 수정하면 `image_name`은? | `""`이 된다 | 🟡 절반 정답 (사진이 사라진다는 결론은 맞음). 다만 **`""`가 아니라 `null`**: 폼에 이미지 입력칸 자체가 없으면 파라미터가 아예 안 옴 → 바인딩이 setter를 안 부름 → 필드 기본값 `null` 그대로 (`""`는 입력칸이 있는데 비워서 보냈을 때) → UPDATE가 `image_name = NULL`로 덮어씀 → DB에서 사진 연결 끊김 + 디스크에 파일만 남음(고아 파일). 해결은 M5에서: UPDATE에서 `image_name` 빼기 / `<set>`+`<if>` 동적 SQL / Service에서 `checkReviewMno`가 돌려준 원본의 `imageName` 채우기 |
| 3-3 단계 A ❌1 (update 검사) | (튜터에게 작성 요청) | → 튜터 작성: `review.getRno()`로 `selectByRno` → null이면 `IllegalArgumentException` → 원본 `mno` ≠ 요청자면 `ForbiddenException` → `update`. `setMno(mno)` 줄 삭제(UPDATE SQL이 mno를 안 써서 의미 없음). 나머지(🟡 private 메서드·warn, 💅)는 교육생 몫. ❌2는 교육생이 같은 때 해결(`ForbiddenException(String msg)` + `super(msg)`, delete에 "접근 할 수 없습니다.") → update도 같은 메시지로 맞춤, 컴파일 OK |
| 3-3 단계 A 🟡 private 메서드 1차 | `private Review checkReviewMno(int rno, int mno)`: 조회 → null → 작성자 검사 → 원본 반환 | 👍 private, 검사 순서, **원본 반환**(단계 C 수정 폼 미리 채우기·M5 사진 파일 삭제에 재사용 가능). ❌ **만들기만 하고 호출하지 않음** → delete·update에 6줄 중복 그대로 (뽑은 목적 = 중복 제거) / 🟡 `log.warn` 아직 없음 / 💅 이름이 "무엇을 하는지"(mno 비교)보다 "왜"(작성자 확인) 쪽이면 더 좋음 (`checkOwner`, `findOwnedReview`), 파라미터 `loginMno`, 73행 끝 탭 공백 |
| 단계 C 퀴즈. 리뷰 1 수정은 멀쩡했는데 왜 버그? 테스트 데이터는? | (교육생 요청으로 튜터가 답함) | 결과가 맞아 보인 건 **rno 1 = mno 1이라는 우연** 때문. 코드는 "rno를 mno 자리에" 넘기는 틀린 일을 하고 있었고, 두 값이 같아서 안 드러났을 뿐 (UPDATE도 2번 실행). 데이터가 바뀌면(리뷰 2) 500. → **"동작한다 ≠ 맞다"**. 테스트 데이터는 **서로 다른 값끼리 헷갈릴 수 없게** 고른다: rno와 mno가 겹치지 않게, 성공 케이스 하나만이 아니라 다른 사람 글·없는 번호·두 번째 데이터까지. 테스트 코드의 고정값(1, 1, 1…)도 같은 함정 |
| M3 셀프 체크 Q1. 등록 후 forward 말고 redirect인 이유? | forward면 F5 때 리뷰가 계속 등록됨 | ✅ 정답. 보충: forward는 서버 안에서 JSP로 넘길 뿐이라 브라우저의 마지막 요청은 여전히 `POST /review/write` → F5 = POST 재전송. redirect는 브라우저가 `GET /review/{rno}`를 새로 보냄 → 마지막 요청이 GET이 되어 F5해도 조회만 (PRG) |
| M3 셀프 체크 Q2. redirect 후 Model 값이 사라지는 이유? flash는? | Model은 requestScope → redirect는 클라이언트의 새 요청이라 남는 게 없음. flash는 session을 써서 한 번만 사용 | ✅ 정답 (정확함). 보충: flash는 redirect 직전 세션에 잠깐 보관 → 다음 요청에서 Model로 옮겨지고 세션에서 바로 지워짐 → 그래서 F5하면 메시지가 사라짐 |
| M3 셀프 체크 Q3. 권한 검사를 Controller가 아니라 Service에 두는 이유? | 서비스가 하는 일이기 때문 | 🟡 방향은 맞지만 "왜 서비스 일인지"가 빠짐. ① **문이 여러 개**: 삭제·수정 폼·수정 저장 3곳 + 나중에 다른 Controller·REST API → Service는 모두가 지나가는 **한 곳**, Controller에 두면 한 곳만 빠뜨려도 뚫림 ② **비즈니스 규칙**(누가 고칠 수 있나)이지 HTTP 처리가 아님 ③ **DB 원본으로 비교**해야 함(`checkReviewMno`가 DAO로 조회) ④ 웹 없이 단위 테스트 가능. `<c:if>`는 버튼만 숨길 뿐 주소창 입력은 못 막음(브라우저 확인 3번) |
| 3-3 단계 A 2차 | delete·update에서 중복 6줄 삭제 → `checkReviewMno(...)` 호출 한 줄 + DAO 한 줄 (update는 `review.getRno()`) | ✅ **단계 A 통과** (컴파일 OK). 검사 규칙이 한 곳에만 있음. 남은 선택: 🟡 `log.warn`, 💅 이름(`checkOwner`)·`loginMno`·파라미터 순서·42~43행 주석 / 🤔 imageName 질문 미답 |

### 🛠️ 트러블슈팅

**`signUp_duplicateEmail` 실패 — `Expected IllegalArgumentException to be thrown, but nothing was thrown` (2026-11-17)**
- 원인(추정): 테스트가 DB에 `kim@matzip.com`이 있다고 가정 → 지금 DB에 없어서 중복 예외가 안 남 (Workbench `SELECT email FROM member;`로 확인)
- 해결 방향: DB 데이터에 기대지 말고 `@BeforeEach`의 `setMem` 이메일로 재가입해 중복 상황을 테스트 안에서 만든다

### ❓ 내가 한 질문

- `@ModelAttribute`의 역할은? → ① **바인딩**: 기본 생성자로 객체 생성 → 파라미터 이름과 같은 setter 호출 (이름 안 맞으면 조용히 null, 타입 안 맞으면 400) ② **Model에 자동 추가**: 클래스 이름 첫 글자 소문자(`member`) → 실패 후 폼에서 `${member.email}`로 입력값 유지. `@RequestParam`은 값 하나·없으면 400·Model 추가 X. 객체 파라미터는 생략해도 같은 동작. (메서드에 붙이면 공통 Model 값, 참고만)

- 회원가입 URL을 `GET /signUp-form` + `POST /signup`으로 나누는 게 좋나, `/signup`으로 합치는 게 좋나? → **합치기 권장**: ① 자원 하나에 URL 하나, 보여 주기/처리는 HTTP 메서드가 구분 (REST) ② 실패 시 뷰를 바로 반환하면 주소창에 POST URL이 남음 → 합쳐 두면 늘 같은 주소, 다시 열면 GET 폼 ③ 로그인과 같은 규칙 ④ URL은 소문자 + kebab-case (`signUp` 대문자 섞임 → 오타 404). 고칠 곳: `MemberController.java:24`, `index.jsp:13` (`signup.jsp` action은 이미 `/member/signup`)

- 없는 이메일과 틀린 비밀번호의 예외를 분리해야 하나? → **화면 메시지는 하나로** (나누면 계정 열거 공격: 가입된 이메일을 골라낼 수 있음). 원인 구분은 **서버 로그**(`warn`, 이메일만, 비밀번호는 절대 X)에서. 구조: `if` 둘로 나눠 로그만 다르게 + 같은 메시지로 `throw` (지금처럼 한 줄도 OK). 회원가입의 "이미 가입된 이메일"도 노출이지만 실서비스는 시도 횟수 제한·이메일 인증으로 보완

- 상세 링크에 `${status.count}`(목록의 줄 번호)를 써야 하나? → **아니다, `${item.rno}`(DB의 리뷰 번호)**. `status.count`는 이번 화면에서 몇 번째 줄인지(1, 2, 3…)일 뿐 → 검색 조건·삭제·정렬에 따라 같은 리뷰도 줄 번호가 바뀜. `rno`는 그 리뷰의 고유 번호(PK), 어디서 봐도 같음 → `selectDetail(rno)`의 `WHERE r.rno = #{rno}`와 짝. 비유: 줄 번호 = 대기표 순서, rno = 주민번호

- `insert`(`useGeneratedKeys="true" keyProperty="rno"`)의 반환값 `int`에는 무엇이? → **영향받은 행 수**(성공 시 `1`), 새 rno가 **아니다**. 새 rno는 **넘겨준 `Review` 객체의 `rno` 필드**에 MyBatis가 setter로 채움 → insert 호출 뒤 같은 객체에서 `getRno()`. 반환값 int는 update·delete와 같은 규칙(바뀐 행 수: 0이면 대상 없음). 흔한 실수: 반환값을 rno로 써서 늘 `/review/1`로 redirect

- `resultMap`을 쓴 mapper 메서드의 반환 타입은? → **`resultMap`의 `type`(= `Review`)**. `resultMap`은 "어떻게 채울지" 설명서일 뿐이고 결과 객체는 `Review` 그대로(`writer`가 채워진 Review). 한 건이면 `Review`, 여러 건이면 `List<Review>` (`resultType`일 때와 같은 규칙). `ReviewDao.selectDetail(int rno)`는 이미 `Review` → Service 상세 메서드도 `Review` 반환

### ⏭️ 남은 일

- [x] 3-1 로그인·로그아웃 (세션에 `loginUser`) ✅ (2026-11-17)
  - [x] 개념 질문 2개 ✅
  - [x] ① `MemberService.login(String email, String password)` → `Member` ✅ (2차 리뷰 통과) (요구사항 제시: guard clause + 같은 메시지?, `equals`, 반환 전 password 비우기, info 로그)
  - [x] `MemberServiceTest` ✅ (튜터 작성, 초록 5개): 성공(mno 같음·password null) / 없는 이메일 / 틀린 비밀번호 (`assertThrows`)
  - [x] ② `MemberController` ✅ (1차 통과, 로그아웃은 POST)
  - [x] ③ `member/login.jsp` + `index.jsp` 로그인 상태 표시 ✅ (요구사항 제시: signup.jsp 복사, input name = @RequestParam, `<c:choose>` + `${empty loginUser}`, 로그아웃은 POST 폼, 브라우저 확인 4가지)
- [ ] 3-2 리뷰 목록(검색 포함)·상세·작성·수정·삭제 Controller ← 진행 중 (화면 하나씩 관통)
  - [x] 개념 Q1: 상세는 `/review/3`(`@PathVariable`), 검색은 쿼리 스트링인 이유? ✅ (모르겠어 → 설명: 경로 = 어떤 것, 쿼리 = 어떻게 볼지)
  - [x] ① `ReviewService`(search) + `ReviewController` `GET /review/list`(`@ModelAttribute ReviewSearch`) + `review/list.jsp`(GET 검색 폼 + `<c:forEach>` + 0건 표시) + index 링크 → 브라우저 4가지 (빈 minRating `""` → Integer?) ✅ (2차 통과, 브라우저 5가지 통과)
  - [x] ② 상세 `GET /review/{rno}` ✅ (3차 통과, 브라우저 4가지) (Service `getDetail` → `selectDetail` 재사용, 없는 번호 처리, `@PathVariable int rno`, `review/detail.jsp`에 `${review.writer.name}`, 목록 제목 → 상세 링크)
  - [x] ③ 작성 `GET·POST /review/write` (작성자 = loginUser) ✅ (2026-11-18, 3차 통과, F5 중복 없음 확인, textarea 반영)
  - [x] ③-2 작성 후 "등록 완료" 메시지 `addFlashAttribute` ✅ (2026-11-18, 2차 통과)
  - [x] ④ 수정·삭제 → 3-3 작성자 검사와 함께 ✅
    - [x] 단계 A: `ForbiddenException` + Service `deleteReview`·`updateReview` + private `checkReviewMno` ✅ (2026-11-19)
    - [x] 단계 B: `POST /review/{rno}/delete` + 상세 삭제 버튼 + list.jsp `msg` ✅ (2026-11-19, 5차 통과, 브라우저 4가지)
    - [x] 단계 B-④: 삭제 버튼을 작성자에게만 (`<c:if>`, 보안 X·화면 편의) ✅ (3차 통과)
    - [x] 단계 C: 수정 `GET·POST /review/{rno}/edit` + `edit.jsp` ✅ (2026-11-22, 브라우저 5가지 통과)
      - (2026-11-22) ③ POST 1차 리뷰: 잘함 = 로그인 검사·`setRno` 위치·예외별 redirect, `redirect:/review/{rno}`(경로 변수 자동 치환). 고칠 것 = 🔴 try 뒤 `updateReview(rno, review)` 두 번째 호출(mno 자리에 rno → rno=mno일 땐 우연히 통과, 아니면 Forbidden 미처리 500) / 🟡 "수정 완료" flash 없음 / 💅 안 쓰는 `Model`, `eidtReview` 오타, TODO 주석. ⑤ 수정 링크 통과
      - (2026-11-22) 교육생 요청으로 튜터가 수정: 두 번째 `updateReview` 삭제 → "수정 완료" flash, `Model` 제거, `editReview` 이름, TODO 삭제 (변수명 통일은 안 함). 컴파일 OK, 브라우저 5가지 확인 남음(리뷰 2 수정 포함)
- [x] (M5로 미룸) 수정 시 `image_name`이 `null`로 덮어써지는 문제 → ✅ M5 5-2c에서 해결 (2026-11-22)
- [x] `@RequestParam` / `@ModelAttribute` / `@PathVariable` 바인딩 ✅ (로그인·검색·상세/수정/삭제에서 사용)
- [x] 등록·수정·삭제 후 PRG + `addFlashAttribute` ✅ (2026-11-22)
- [x] 작성자 본인 검사를 Service에 → 커스텀 예외 ✅ (2026-11-19)
- [x] 로그인 검사를 각 Controller 메서드에 직접 (M4 AOP 동기) ✅ (write·delete·edit GET/POST 5군데 중복)
- [x] (M2에서 미룸) `ReviewDaoTest` selectDetail·update·delete ✅ (2026-11-22, 교육생 요청으로 튜터 작성: `insertReview()` 헬퍼, association 확인, update는 다시 조회해 비교, delete 후 `assertNull`, 없는 번호 = 행 수 0 → 전체 16개 초록, DB 롤백 확인)
- [ ] (선택, M3 범위 밖으로 미룸) `@MybatisTest` + `@Mapper` 실험, 괄호 빨간불 실험
- [x] 셀프 체크 ✅ (2026-11-22, Q1·Q2 정답, Q3 🟡 보충): redirect 이유 / redirect 후 Model 값이 사라지는 이유 / 권한 검사를 Service에 두는 이유

### 📌 다음 세션 시작점

**M3 완료 ✅ (2026-11-22) → M4 챕터로 이동** (아래 M4의 📌 참고)

(이하 2026-11-19 기록) M3 **3-2-④ 단계 C: 리뷰 수정** 리뷰부터. 삭제 + 작성자 검사(Service `checkReviewMno` + `ForbiddenException`) 완료.
- 요구사항은 이미 제시함 (🙋 표 "단계 C Q" 근처 대화): ① Service `getReviewForEdit(rno, mno)` = `checkReviewMno` 반환값 그대로 ② `GET /review/{rno}/edit`: 로그인 검사 → 원본을 Model `review` → `review/edit`, 예외는 삭제와 같은 규칙(권한 → 상세, 없음 → 목록, flash `msg`) ③ `POST /review/{rno}/edit`: `Review` 바인딩 + 경로 `rno`를 `setRno` → `updateReview` → "수정 완료" + 상세 redirect ④ `edit.jsp`(write 복사, value 미리 채우기, textarea는 태그 사이) ⑤ `detail.jsp` 같은 `<c:if>` 안에 수정 링크
- **(2026-11-21) 데스크탑 이동**: 옛 PC 초안은 커밋 안 돼서 못 가져옴 → 교육생 요청으로 **튜터가 ①·②(GET)·④ 초안 작성**(인터페이스 `getReviewForEdit`, `editForm`, `edit.jsp`, 컴파일 OK). **남은 것: ③ POST `/{rno}/edit`(컨트롤러 맨 아래 `TODO`) + ⑤ `detail.jsp` 수정 링크 → 교육생 작성**. 새 PC DB엔 `test@gmail.com` 없음(mno 1·5·6만) → 남의 글 확인은 5·6번 계정으로
- (옛 PC 기록) **교육생이 작성 시작한 상태 (아직 미검토, 컴파일 OK)**: `ReviewServiceImpl.getReviewForEdit` 있음(인터페이스에는 아직 없는 듯), `GET /{rno}/edit`가 `selectDetail` + null 검사 사용 중(→ `getReviewForEdit`로 작성자 검사해야 함, `selectDetail`은 null이면 이미 예외), `return "/review/edit"` 앞 `/`, `edit.jsp` 초안(14줄), POST·수정 링크는 아직 없음
- 브라우저 확인 4가지: 본인 수정(값 채워짐·"수정 완료") / test 계정이 주소창 `/review/1/edit` → 상세 + "접근 할 수 없습니다." / `/review/9999/edit` → 목록 + 찾을 수 없음 / 수정 직후 F5
- 단계 C 뒤: M3 남은 일(셀프 체크 3개, M2에서 미룬 `ReviewDaoTest` selectDetail·update·delete) → M3 마무리·Notion 업로드
- 테스트 계정: `test@gmail.com`(mno=24). 리뷰는 rno 1·2(mno=1)만 남음
- 선택 💅(미반영): Service `log.warn`, `checkReviewMno` 이름·`loginMno`·파라미터 순서, `rediAttr` 이름, 42~43행 주석

---

## M4. 트랜잭션 + AOP — ✅ 완료 (2026-11-22) · [Notion](https://app.notion.com/p/4206412b0acd813daefbec847cc89f06)

### ✅ 한 일

- 4-1 회원 탈퇴: `ReviewDao.deleteByMno`, `MemberDao.delete`(+XML), `MemberService.deleteAccount`(리뷰 → 회원, `log.info`)
- 4-2 `MemberWithdrawTest`로 트랜잭션 없는 탈퇴의 실패 재현 (리뷰만 지워짐 🔴)
- 4-3 `@Transactional` 한 줄로 같은 테스트 🟢 (PLAN 완료 기준 앞부분 달성)
- 4-5 로그인 검사 중복 → Aspect / Interceptor / Filter 비교 토론 (👀), 셀프 체크 2문제 정답
- 4-4 `spring-boot-starter-aspectj` + `ExecutionTimeAspect`(`@Around`, try/finally) → Service 메서드마다 실행 시간 로그 (PLAN 완료 기준 뒷부분 달성)

### 📚 배운 개념

- **횡단 관심사를 어디에 둘까 (4-5)**: Filter(서블릿 단계, Spring 밖, 인코딩·CORS) → **Interceptor**(DispatcherServlet ↔ Controller 사이, URL 기준, request/response 사용 → 로그인 검사에 적합) → **AOP**(빈의 메서드 기준, HTTP와 무관 → 트랜잭션·실행 시간·Service 로깅). "웹 요청 단위로 막는 일 = Interceptor, 비즈니스 메서드 단위로 감싸는 일 = AOP"
- **XML의 SQL은 컴파일 때 검사되지 않는다**: `DELETE *`, `WHEN` 같은 오타는 쿼리를 처음 실행할 때 `BadSqlGrammarException`으로 드러난다 → DAO·Service 테스트가 필요한 이유
- **`@Test`만 테스트다**: `@BeforeEach`/`@AfterEach`는 각 테스트의 준비·정리. 정리에는 assert를 넣지 않고, 테스트가 실패해도 실행된다
- **트랜잭션 실험 테스트에는 `@Transactional`을 붙이지 않는다**: 붙이면 Service가 테스트 트랜잭션에 참여(REQUIRED)하고 rollback은 테스트 끝에 한 번 → 차이가 안 보인다. 대신 진짜 commit + `@AfterEach` 수동 정리 (샘플 FW_07 `MemberServiceTest`와 같은 구조)
- **commit/rollback은 Connection의 메서드**다. 트랜잭션이 없으면 DAO 호출마다 Connection을 따로 받고 auto-commit으로 바로 확정된다. 그래서 2번째 작업이 실패해도 1번째 작업은 이미 commit된 상태다 → 탈퇴하면 리뷰만 지워지고 회원은 남는다.
- **하나의 업무 = 하나의 Connection = 하나의 트랜잭션**. 업무 단위를 맡는 곳은 Service라서 트랜잭션도 Service에 건다. DAO는 쿼리 하나 단위다.
- `review.mno` FK에 `ON DELETE CASCADE`가 없다 → **리뷰를 먼저** 지워야 회원을 지울 수 있다 (순서가 바뀌면 FK 위반 예외가 난다).

### 🙋 내 답변과 피드백

| 질문 | 내 답 | 피드백 |
|---|---|---|
| Q1. 탈퇴 중 리뷰 삭제는 성공하고 회원 삭제에서 예외가 나면 DB 상태는? | 회원은 안 지워지고 리뷰만 지워짐 | ✅ 상태는 정답. 🟡 "왜"를 보충: DAO마다 Connection이 따로이고 각각 auto-commit이라 리뷰 삭제는 이미 확정됐다 (commit/rollback = Connection의 일) |
| Q2. 트랜잭션은 DAO와 Service 중 어디에? | Service. 회원 삭제라는 비즈니스 로직을 묶어서 원자적으로 수행해야 함 | ✅ 정답. "원자성"이라는 용어도 정확함. DAO = 쿼리 1개, Service = 업무 1개 |
| `@Param("pw") String password`일 때 XML에서 `#{password}` vs `#{pw}`? | `#{pw}` | ✅ 정답. 🟡 이유 보충: XML이 보는 이름은 Java 변수명이 아니라 `@Param`에 적은 **Map의 key**다 (변수명은 컴파일하면 사라질 수 있음) |
| 탈퇴 시 `memberDao.delete`가 0(없는 회원)이면? | (a) 그냥 둔다 (이유는 말하지 않음) | ✅ 타당한 선택. 튜터 보충: 탈퇴는 로그인한 본인만 호출하니 회원이 없을 일이 거의 없고, 이미 없다면 "없게 만든다"는 목적은 이미 이뤄진 상태다(삭제의 멱등성). 다음엔 이유도 한 줄 같이! |
| 4-3 Q1. `@Transactional`을 인터페이스 / Impl 클래스 / `deleteAccount` 메서드 중 어디에? | 메서드 위. 클래스에 붙이면 testBefore·testAfter에도 적용되기 때문 | 🟡 선택(메서드)은 OK, 이유는 대상이 섞임. 질문은 **`MemberServiceImpl`** 클래스였음 → 클래스에 붙이면 Impl의 public 메서드 전부(`signUp`·`login`·`deleteAccount`)에 적용된다. 테스트 클래스와는 무관. (테스트 클래스에 붙이면 안 된다는 감각 자체는 4-2와 맞음.) 메서드 위 = 여러 DAO를 묶어야 하는 업무에만 정확히 건다. 인터페이스는 비추천(Spring 문서도 구현 클래스 권장) |
| 4-3 Q2. 붙이고 `1/0` 그대로 두면 테스트 결과는? | 모르겠어 | 설명: proxy가 트랜잭션 시작 → 리뷰 삭제(아직 commit 전) → `1/0` = RuntimeException → proxy가 **rollback** 후 예외를 **그대로 다시 던짐** → `assertThrows` 통과 + 회원·리뷰 `assertNotNull` 통과 → **초록불**. 예외는 여전히 나지만 DB는 탈퇴 전 그대로 = 원자성 |
| 4-3 `1/0`이 있을 때 `회원 탈퇴 완료` 로그가 안 찍힌 이유는? | 1/0에서 에러가 나서 rollback되었기 때문 | 🟡 반만 맞음. 로그가 안 찍힌 진짜 이유 = 예외가 나면 **그 줄에서 메서드 실행이 멈추고** 아래 줄(`memberDao.delete`, `log.info`)은 실행되지 않는다 (Java 예외의 흐름). rollback은 메서드가 끝난 **뒤에** proxy가 하는 일이고, 되돌리는 것은 **DB 변경**뿐이다. 이미 실행된 Java 코드나 찍힌 로그는 되돌리지 않는다. 증거: `@Transactional`이 없던 4-2에서도 로그는 안 찍혔다 |
| 4-4 Q1. 실행 시간 측정에 `@Around`를 쓰는 이유는? | `@Before`에서 잰 시작 시간을 `@After`로 넘기려면 멤버 변수 같은 저장 공간이 필요 → `@Around`가 적합 | ✅ 정답. 보충: 그 멤버 변수는 실제로 **위험**하다. Aspect는 싱글톤 빈 1개라서 요청(스레드) 여러 개가 같은 필드를 덮어쓴다. `@Around`는 시작 시각을 **지역 변수**로 두므로 호출마다 따로 생긴다 |
| 4-4 Q2. Service 전체에 쓸 Pointcut 지정자는? | `execution(... service 패키지 ...)` (이유는 안 씀) | ✅ 좋은 선택. 계층(패키지) 단위로 한꺼번에 걸 때 `execution`(교안 지정자 가이드). `@within(Service)`도 정답(패키지가 바뀌어도 유지된다는 장점). 너무 넓게(`com.study..*`) 잡으면 Controller·DAO까지 걸린다 |
| `execution(* com.study.matzip.model.*.*(..))`는 `MemberServiceImpl.login`에 걸릴까? | 안 걸림. model 바로 아래 클래스만 보니까 | ✅ 정답. 보충: 마지막 `*`는 **메서드 이름**, 그 앞의 `model.*`이 "model 바로 안의 클래스 한 칸" → `model.service.MemberServiceImpl`은 한 칸 더 깊어서 실패. `model..*.*(..)`이면 걸린다 |
| 4-5 Q1. 로그인 검사를 Aspect로 옮기면 곤란한 점? | 모르겠어 | 설명: ① Aspect는 HTTP를 모른다 → 세션은 `RequestContextHolder`로 우회해서 꺼내야 함 ② 검사 대상(write·delete·edit)과 제외 대상(list·상세)을 메서드 시그니처로 골라야 해서 Pointcut이 지저분해짐(또는 커스텀 애너테이션 필요) ③ 막을 때 Controller 반환형(String 뷰 이름)에 맞춰 `"redirect:..."`를 돌려줘야 해서 Controller 구조에 묶임 |
| 4-5 Q2. DispatcherServlet이 Controller 직전에 끼어드는 장치와 대상 기준은? | 모르겠어 | 답: **HandlerInterceptor**의 `preHandle`. 기준은 **URL 경로**(`addPathPatterns("/review/write", "/review/*/edit", ...)` / `excludePathPatterns`). `request`·`response`를 받으므로 세션 꺼내기·`sendRedirect`가 자연스럽고, `false`를 반환하면 Controller까지 가지 않는다 |
| 셀프 체크 Q1. 같은 클래스의 `cleanUp()`이 `this.deleteAccount()`를 부르면 `@Transactional`이 동작할까? | 안 된다, this는 proxy가 아니라서 | ✅ 정답. 보충: proxy는 **바깥에서 들어오는 호출**만 가로챈다. `cleanUp()`까지는 proxy를 거쳐 들어왔지만(트랜잭션 없음) 그 안의 `this`는 진짜 `MemberServiceImpl`이라 `deleteAccount`의 advice가 실행되지 않음 → 리뷰·회원 삭제가 각각 auto-commit. 대책: 다른 빈으로 분리(교안 8장 추천) / `cleanUp()`에 `@Transactional`. 같은 이유로 `ExecutionTimeAspect`도 내부 호출은 시간을 재지 않는다 |
| 셀프 체크 Q2. `deleteAccount`에서 checked 예외(`throw new Exception`)를 던지면 rollback될까? | 안 된다, RuntimeException이 아니라서 | ✅ 정답. 보충: "rollback 안 됨" = **commit된다**(`completeTransactionAfterThrowing`의 else 분기) → 이미 지운 리뷰가 확정되고 예외는 그대로 던져짐. 대책: `@Transactional(rollbackFor = Exception.class)` 또는 RuntimeException 계열로 감싸 던지기. MyBatis의 DB 오류는 `DataAccessException`(RuntimeException)이라 기본 설정으로 rollback된다. 우리 커스텀 예외(`ForbiddenException`)도 RuntimeException 상속인지 확인해 둘 것 |

### 🛠️ 트러블슈팅

- **(2026-11-22) Aspect를 만들었는데 로그가 안 찍힘** → Pointcut 오타(`service.*...(..)`). 틀린 Pointcut은 **에러가 아니라 "매칭되는 메서드 0개"** 로 조용히 끝날 수 있다. 확인 방법: 테스트 실행 후 Console에서 Aspect 로그가 있는지, 또는 `AopUtils.isAopProxy(memberService)`(교안 4-5)

### ❓ 내가 한 질문

- **(2026-11-22) DAO 메서드 인자가 3개(`exampleFunction(int a, int b, int c)`)면 mapper.xml에서 어떻게 쓰나?**
  → 파라미터마다 `@Param("이름")`(`org.apache.ibatis.annotations.Param`)을 붙이고 XML에서 `#{이름}`으로 쓴다. 여러 개면 MyBatis가 내부적으로 Map으로 묶기 때문에 `parameterType`은 쓰지 않는다. `@Param`이 없으면 `#{arg0}`·`#{param1}` 같은 이름으로만 접근될 수 있다(컴파일 옵션 `-parameters`에 따라 달라짐) → 샘플(FW_07)처럼 **항상 `@Param`을 붙이는 것**이 안전하다. 값이 많거나 의미 있게 묶이면 DTO(예: `ReviewSearch`)로 넘기는 방법도 있다.
- **(2026-11-22) `@Param` 없이 `#{a}`, `#{b}`, `#{c}`로 바로 써도 되나?**
  → **이 프로젝트에서는 된다. 단, 조건이 맞을 때만.** ① MyBatis 설정 `useActualParamName`(기본값 true) ② 컴파일할 때 `-parameters` 옵션으로 변수명이 `.class`에 남아 있을 것. Boot parent pom이 `-parameters`를 켜 주고, 실제로 `javap -v ReviewDao.class`를 보면 `MethodParameters`(`review`, `rno` 등)가 남아 있다. 조건이 깨지면(다른 빌드 설정, 옛 프로젝트, 다른 IDE 설정) 실행할 때 `Parameter 'a' not found. Available parameters are [arg0, arg1, arg2, param1, param2, param3]` 에러가 난다 → 컴파일은 통과하고 **실행할 때 터진다**는 게 위험한 점이다. 그래서 `@Param`을 권장한다.
- **(2026-11-22) `update(Review review)`처럼 인자가 1개일 때도 `@Param`을 권장하나?**
  → **DTO 1개면 붙이지 않는다.** 붙이면 XML에서 `#{title}`이 아니라 `#{review.title}`로 써야 한다 (`@Param`을 붙이면 DTO가 Map 안의 값 하나가 되기 때문). 붙이지 않으면 DTO 자체가 파라미터라 `#{필드명}` → getter로 바로 읽힌다. **기본형·String 1개**(`int mno`)는 사실 이름과 상관없이 그 값 하나가 들어가지만, 의미를 드러내려고 붙여도 된다. 샘플 FW_07 관례: `insert(Member)`·`update(Member)`·`search(SearchCondition)`에는 붙이지 않고, `delete(@Param("mno") int mno)`·`select(@Param("email") String)`에는 붙인다.
  - (튜터 정정) 앞에서 "샘플은 1개여도 전부 붙였다"고 했는데 틀렸다 → 기본형·String에만 붙였다.
- **(2026-11-22) `@Transactional` 내부에서 `@Around`를 쓰는 코드를 볼 수 있나?**
  → `@Around` 애너테이션을 직접 쓰지는 않는다. Spring AOP의 **`MethodInterceptor`**(AOP Alliance) 인터페이스로 만든 **Around Advice**다. 실제 소스(spring-tx 7.0.10): `TransactionInterceptor.invoke(MethodInvocation)` → `TransactionAspectSupport.invokeWithinTransaction`. 그 안의 구조: `createTransactionIfNecessary`(시작) → `try { retVal = invocation.proceedWithInvocation(); }`(진짜 메서드, 소스 주석에도 "This is an around advice") → `catch (Throwable ex) { completeTransactionAfterThrowing(...); throw ex; }`(rollback 후 예외를 다시 던짐) → `commitTransactionAfterReturning`(commit). rollback 여부는 `DefaultTransactionAttribute.rollbackOn` = `ex instanceof RuntimeException || ex instanceof Error` → **checked 예외면 rollback 대신 commit** (`completeTransactionAfterThrowing`의 else 분기). AspectJ 용어와 대응: Pointcut = `TransactionAttributeSourcePointcut`(@Transactional 붙은 메서드), Advice = `TransactionInterceptor`, Advisor(=Aspect) = `BeanFactoryTransactionAttributeSourceAdvisor`, `invocation.proceed()` ≈ `joinPoint.proceed()`. STS에서 `Ctrl+Shift+T` → `TransactionInterceptor`로 열 수 있고, `invoke`에 breakpoint를 걸고 `Debug As > JUnit Test`로 따라가 볼 수 있다.
- **(2026-11-22) `spring-boot-starter-aop`를 지우고 `spring-boot-starter-aspectj`를 추가하면 되나?**
  → 맛집 노트 `pom.xml`에는 `spring-boot-starter-aop`가 **처음부터 없다**(grep 확인) → 지울 것 없이 `aspectj` 하나만 **추가**. 같은 기능의 옛 이름(Boot 3 이하)이라 둘 다 두지 않는다. 버전은 parent가 관리하므로 `<version>` 없이 `groupId`(`org.springframework.boot`)와 `artifactId`만 적는다. 추가 후 `Maven > Update Project`.
- **(2026-11-22) AOP 관련 문서는 어디서 찾나?**
  → ① 교안 Notion [스프링_AOP_정리] (4-3 의존성, 4-4 Aspect 작성, 5-3 execution 표, 6-5 `@Around`, 6-6 🔑 박스) ② 샘플 FW_04 `LoggingAspect`(구조 참고) ③ 공식 문서: Spring Framework Reference > Core > AOP (`docs.spring.io/spring-framework/reference/core/aop.html`, 하위 "@AspectJ support > Declaring a Pointcut / Declaring Advice"), Spring Boot Reference > AOP (`docs.spring.io/spring-boot/reference/features/aop.html`) ④ STS 안에서: 애너테이션에 마우스 올리기(Javadoc), `F3`/`Ctrl+클릭`으로 소스 열기
- **(2026-11-22) `spring-boot-starter-aspectj`에 `<version>4.1.2</version>`을 적어서 추가해도 되나?**
  → 동작은 한다(parent도 4.1.2라 값이 같음). 하지만 `<version>`은 **빼는 게 맞다**. parent(`spring-boot-starter-parent`)가 Boot 관련 의존성 버전을 한 곳에서 관리(dependency management)하므로, 직접 적으면 나중에 parent만 올렸을 때 이 줄만 옛 버전에 남아 **버전이 섞인다**. 다른 starter(`webmvc` 등)에도 버전이 없는 이유. STS도 "Overriding managed version" 경고를 띄운다. (`mybatis-spring-boot-starter`처럼 Boot가 관리하지 않는 라이브러리만 버전을 적는다)
- **(2026-11-22) execution의 `*`, `..` 뜻과 쓰는 법?**
  → 형식 `execution(반환형 [패키지.클래스.]메서드명(파라미터))` — 반환형·메서드명·괄호는 필수, 패키지.클래스는 생략 가능. `*` = 이름/타입 **한 덩어리**(반환형 아무거나, 클래스명 아무거나, `set*` = set으로 시작, 파라미터 자리에선 **정확히 1개**). `..` = **0개 이상**: 패키지 자리에선 하위 패키지 포함, 파라미터 자리에선 개수·타입 무관. `..`은 맨 앞에 못 온다. 예: `execution(* com.study.matzip.model.dao.*.*(..))` = dao 패키지(하위 제외)의 모든 클래스의 모든 메서드, `com.study.matzip..*.*(..)` = matzip 아래 전부(Controller·DAO까지 걸림 ⚠️)
- **(2026-11-22) `*`와 `..`의 차이를 잘 모르겠다**
  → 핵심: `*` = **딱 한 칸**(점과 점 사이 한 덩어리 / 파라미터 정확히 1개), `..` = **몇 칸이든**(0칸 포함, 하위 패키지 전부 / 파라미터 0개 이상). 폴더 비유: `model/*/` = model 바로 아래 폴더 1단계, `model/../` = model 아래 몇 단계든. 맛집 패키지 트리로 비교: `com.study.*.model` ❌(`matzip` 자리만 대체, 실제 경로와 다르면 실패) / `com.study..model` ✅. 파라미터: `(*)`는 `delete(int)` ✅·`login(String,String)` ❌·`findAll()` ❌, `(..)`는 셋 다 ✅
- **(2026-11-22) 실행 시간을 재는 함수는?**
  → ① `System.currentTimeMillis()`: 1970-01-01부터 지금까지 ms(`long`). 교안 5-7·6-6 예시가 이것. 단, "벽시계" 시각이라 서버 시간이 동기화로 바뀌면 값이 튈 수 있다 ② `System.nanoTime()`: ns 단위, **시간 간격 측정 전용**(값 자체는 의미 없음, 차이만 의미). 짧은 메서드(DB 없이 1ms 미만)도 0이 안 나온다. ms로 보려면 1,000,000으로 나눈다 ③ Spring `org.springframework.util.StopWatch`: `start()`/`stop()`/`getTotalTimeMillis()`, 구간 여러 개를 이름 붙여 잴 때 편함. 실습 추천: 교안대로 `currentTimeMillis` 또는 정확히 재려면 `nanoTime`
- **(2026-11-22) Service `deleteAccount(int mno)`의 반환형은 뭘로?**
  → **`void` 추천.** Service 반환값은 "Controller가 다음 화면을 그리는 데 필요한 것"이다. 탈퇴 후 Controller는 세션을 끊고 redirect만 하므로 줄 값이 없다. 실패는 반환값(0, -1)이 아니라 **예외**로 알린다 (M3 `ForbiddenException`과 같은 원칙, 4-2·4-3의 rollback도 예외가 있어야 동작한다). 지운 리뷰 개수 같은 정보는 Service 안의 `log.info`로 남긴다. DAO가 `int`(행 수)를 돌려주는 것과 Service의 반환형은 별개다. 참고: `public deleteAccount(int mno);`처럼 반환형을 비우면 컴파일 에러("Return type for the method is missing")가 난다. 샘플 FW_07 `MemberService.delete`도 `void`.

### ⏭️ 남은 일

- [x] 개념 질문 2개 ✅ (2026-11-22)
- [x] 4-1 회원 탈퇴 ✅ (2026-11-22) = 리뷰 전체 삭제 + 회원 삭제 (DAO: 회원의 리뷰 전체 삭제, 회원 삭제 / Service 탈퇴 메서드) ← 요구사항 제시함 (2026-11-22): `ReviewDao.deleteByMno`, `MemberDao.delete`, `MemberService.withdraw`(이름은 자유), `MemberServiceImpl`에 `ReviewDao` 주입, 리뷰 → 회원 순서, `@Transactional`은 아직 붙이지 않음
  - [x] ① `ReviewDao.deleteByMno` ✅ (2차 통과, `DELETE *` → `DELETE FROM` 수정) (2026-11-22 1차 리뷰): 잘함 = 인터페이스 선언(int 반환), `id`=메서드명, WHERE `mno`, 기존 `delete`와 같은 위치·형식. 🔴 `DELETE *` → MySQL 문법 오류(`DELETE`는 행 단위라 컬럼 목록이 없음, `DELETE *`는 MS Access 문법). XML SQL은 앱이 시작될 때 검사하지 않아서 **처음 실행할 때** `BadSqlGrammarException`이 난다
  - [x] ② `MemberDao.delete` ✅ (2차 통과, `WHEN` → `WHERE`) (2026-11-22 1차 리뷰): 인터페이스 ✅, `id="delete"`·`DELETE FROM member` ✅. 🔴 `WHEN mno = ...` → `WHERE` (WHEN은 CASE문 키워드)
  - [x] ③ `MemberService.deleteAccount` + Impl ✅ (3차 통과: 로그 `mno`로 수정, 행 수 0이면 (a) 그냥 둔다) (2026-11-22 1차 리뷰, 컴파일 OK): ✅ `void`, `ReviewDao` `final` 필드로 주입, 리뷰 → 회원 순서, 행 수를 변수로 받아 둠. 🟡 `log.info` 없음(받아 둔 `delReviewCnt` 활용) / 🟡(선택) `delMemberCnt == 0`이면 없는 회원 → 예외? / 💅 끝의 빈 줄 3개
  - (2026-11-22 2차 리뷰) ✅ 이름 `deleteAccount`(인터페이스·Impl 일치, `@Override` 유지), 빈 줄 정리, `log.info` 추가. 🔴 로그의 `회원번호={}` 자리에 `delMemberCnt`(지운 행 수 = 1)가 들어감 → 파라미터 `mno`여야 함 (변수 이름이 비슷해서 생긴 실수, 로그는 나중에 장애를 추적할 때 보는 증거라 값이 틀리면 안 됨). 보완 2(행 수 0일 때) 선택 답 아직 없음
- [x] 4-2 ✅ (2026-11-22, 🔴 재현 성공) ← 요구사항 제시함 (2026-11-22): 새 테스트 클래스(`@Transactional` 없음, `@BeforeEach` 회원+리뷰 insert / `@AfterEach` deleteByMno→delete 정리), Service 두 DAO 호출 사이에 임시 `1/0`, `assertThrows` 후 회원·리뷰 둘 다 `assertNotNull` 기대 → 빨간불이 정상. 중간에 예외를 일부러 넣어 — (2026-11-22 1차 리뷰) ✅ Service `1/0`(컴파일 OK, Unreachable 피함), 새 클래스·`@Transactional` 없음, 필드 저장, `@BeforeEach`에서 회원 insert. 🔴 `@Test` 메서드가 없음(검증을 `@AfterEach`에 씀 → JUnit이 실행할 테스트가 없음) / 🔴 리뷰 builder에 `.mno(m.getMno())` 없음(review.mno NOT NULL + FK → insert 실패, 회원만 commit돼서 남음) / 🔴 `@AfterEach`는 정리 전용: Service(지금은 항상 예외)가 아니라 DAO `deleteByMno` → `delete` / 🔴 기대값 반대: 실패 후엔 `assertNotNull` / 💅 `ReviewSearch` import 미사용, `1/0` TODO 주석 없음. 튜터는 테스트를 실행하지 않음(지금 실행하면 테스트 회원이 DB에 남음)
    - (2026-11-22) **교육생 요청으로 튜터가 수정**: `@Test deleteAccount_failInMiddle_rollbackAll` 새로 만듦(`assertThrows(ArithmeticException)` → 회원·리뷰1·리뷰2 `assertNotNull` + 메시지), 리뷰 builder에 `.mno(m.getMno())`, `@AfterEach`를 DAO 정리 전용으로(`deleteByMno` → `delete`), import 정리, 클래스 위에 `@Transactional`을 붙이지 않는 이유 주석, Service `1/0` 위에 TODO 주석
    - 실행 결과: `MemberWithdrawTest.java:76 리뷰1이 남아 있어야 한다 ==> expected: not <null>` → **회원은 남았는데 리뷰가 사라짐 = Q1에서 예상한 상태**. 회원 assert(75행)는 통과하고 리뷰에서 실패. 두 번 연속 실행해도 같은 실패(Duplicate 없음) → `@AfterEach` 정리 정상 **트랜잭션 없이 실패 재현** (리뷰만 지워짐) → 테스트로 확인
- [x] 4-3 ✅ (2026-11-22) ← 개념 Q 2개 답함 (2026-11-22). ✅ 교육생이 `deleteAccount` 메서드 위에 `@Transactional`(Spring import) → `1/0` 그대로 둔 채 🟢 통과(튜터 mvnw 재확인, `회원 탈퇴 완료` 로그 안 찍힘). 교육생이 `1/0` 제거(TODO 주석 한 줄은 남음 💅). **교육생 요청으로 튜터가** `deleteAccount_success`(회원·리뷰 `assertNull`) 추가 + 실패 테스트에 `@Disabled("실험용...")`. 전체 `mvnw test` 18개 중 17 통과·1 Skipped, 로그 `회원 탈퇴 완료: 회원번호=46 삭제된 리뷰 수=2`. `@Transactional`로 해결 → 같은 테스트 통과
- [x] 4-4 ✅ (2026-11-22) ← **교육생 요청으로 튜터가 Aspect 수정**: Pointcut `service.*.*(..)`, `public`, `start`는 try 밖 + `try { return proceed(); } finally { end·log.debug }`(catch 없음 → 예외 그대로 전파), `{}` 두 개 + `toShortString()`. 결과: 전체 18개 중 17 통과·1 Skipped, `MemberServiceImpl.signUp(..) 실행 시간 = 8ms` 등. MemberServiceTest에서 signUp 7회·login 3회 = **예외가 난 호출(중복 이메일, 틀린 이메일·비밀번호)도 시간 로그가 찍힘** → finally 효과 확인. ② Aspect 1차 리뷰 (2026-11-22): ✅ `aop` 패키지, `@Aspect @Component @Slf4j`, `ProceedingJoinPoint`·`Object`·`throws Throwable`, 지역 변수 start/end, `proceed()` 결과 받아서 `return`. 🔴 Pointcut `service.*...(..)` → 에러 없이 **아무 메서드에도 안 걸림**(MemberServiceTest 초록, Service 로그는 찍히는데 시간 로그 0줄) — 메서드 이름 자리가 `*`여야 함 / 🟡 예외 시 시간 로그 안 남음 → 교안생 결정: **예외가 나도 측정하겠다** (가이드: `try { proceed·return } finally { end·log }`, `start`는 try 밖, catch로 예외를 삼키지 않기 — 선택: `catch (Throwable e) { 실패 표시 로그; throw e; }`) / 💅 문자열 `+`와 `{}` 섞어 씀 → `{}` 두 개, `"실행 시간"` 앞 공백, 메서드 `public`(교안 관례). ① 의존성 ✅ (2026-11-22, `<version>` 없이 추가, aspectjweaver 1.9.25.1 받음) · 개념 Q 2개 답함, 요구사항 제시 (2026-11-22: pom `spring-boot-starter-aspectj` + Maven Update, `aop` 패키지에 `@Aspect @Component @Slf4j` 클래스, `@Around(execution 직접 작성)`, `ProceedingJoinPoint`·`Object` 반환·`throws Throwable`, `proceed()` 호출·반환, 예외가 나도 시간 로그 → try/finally 고민, 실행 결과 확인) ( 튜터가 교안 [스프링_AOP_정리] 1~6장·FW_04 확인, pom에 `spring-boot-starter-aspectj` 없음) Service 실행 시간 측정 `@Around` Aspect (`spring-boot-starter-aspectj` 의존성 확인)
- [x] 4-5 ✅ (2026-11-22, 👀 설명으로 마무리) ← 토론 시작 (2026-11-22): ReviewController 5곳(56·66·79·101·123행 근처) 같은 4줄 확인, 질문 2개 제시(방금 만든 Aspect로 옮기면 어떤 문제? / 어느 시점에서 막는 게 좋을까). 교안: Notion [Interceptor] 페이지, Spring MVC pt.1 실행 순서. M3의 로그인 검사 중복(write GET·POST, delete, edit GET·POST 5곳) → "한 곳으로 모으는 방법" 토론 (Interceptor는 👀 설명만)
- [x] 셀프 체크 ✅ (2026-11-22, Q1·Q2 정답): 같은 클래스 내부 호출에서 `@Transactional`이 안 먹는 이유 / checked 예외면 롤백될까?
- [x] 완료 기준 ✅: 탈퇴 테스트가 `@Transactional` 없이는 실패, 붙이면 통과 + 로그에 Service 실행 시간
- [ ] (💅 남음) `MemberServiceImpl.deleteAccount`의 `// TODO: 4-3 후 삭제` 주석 지우기 (줄 끝 공백 정리와 함께 커밋 안 된 상태)

### 📌 다음 세션 시작점

**M4 완료 ✅ (2026-11-22) → M5 마무리로 이동**. 커밋 `715bb00`(4-1~4-3), `a9cc062`(4-4)까지, push 안 함.
1. 먼저 정리: `MemberServiceImpl`의 `// TODO: 4-3 후 삭제` 주석 삭제(교육생) → 줄 끝 공백 변경과 함께 커밋, push 여부 확인
2. 튜터: Notion 교안 **Spring MVC pt.2**(업로드·`@ControllerAdvice`)·**DI** 다시 읽기, 샘플 `FW_05_mvc_basic_sol`의 `MvcConfigurer`·`MvcExceptionHandler`·`ControllerTest` 확인
3. M5 첫 개념 질문: "`ReviewService`가 `ImageStorage` 구현체(Local/Fake)를 바꿔도 코드를 안 고쳐도 되는 이유?" → `ImageStorage` 인터페이스 요구사항

#### (지난) M4 시작 시점 기록

**M4 시작** (M3 완료 2026-11-22, 커밋 `982979a`까지 push됨).
1. 튜터: Notion 교안 **[ Spring Framework ] > Spring Transaction** 다시 읽기, 샘플 `FW_07_mybatis_sol`의 `BasicMemberService.delete`·`@Transactional` 전파 테스트 확인(스크래치패드에 압축 해제)
2. 개념 질문부터: "탈퇴 중 리뷰는 지워졌는데 회원 삭제에서 예외가 나면 DB는 어떤 상태?" → 4-1 요구사항 제시
3. 현재 DB(새 PC): 회원 mno 1(`kim@matzip.com`)·5·6, 리뷰 rno 1·2(mno=1). 탈퇴 실험은 **테스트 코드 안에서 회원·리뷰를 만들어** 진행 (실데이터 보호)
- 환경 메모(새 PC): STS `C:\Project\ssafy\IDE\sts-5.0.1.RELEASE`, 워크스페이스는 `C:\Project\Oogie_Spring_Samples`로 열기. 명령줄 Maven은 `JAVA_HOME=C:\Users\woook\.jdks\ms-25.0.5.0.2` 필요. 앱 DB 계정은 `root`(CLAUDE.md의 ssafy 아님)

---

## M5. 마무리 — ✅ 완료 (2026-11-22) · [Notion](https://app.notion.com/p/4206412b0acd81159222f195eb8d5be2)

### ✅ 한 일

- 5-2a 리뷰 작성 시 사진 업로드: multipart 크기 설정, `write.jsp` `enctype` + `name="image"`, Controller `MultipartFile` 파라미터 + 예외별 redirect·flash `msg`, Service `save` → `setImageName` → insert
- 5-2b `WebConfig.addResourceHandlers("/upload/**")` + detail.jsp `<img>` → 사진 표시
- 5-2c 수정 시 사진 유지/교체(Service가 DB 원본 이름 사용), `InvalidImageException`으로 예외 의미 분리, edit.jsp 현재 사진 + 파일 입력
- 5-3 `MvcExceptionHandler`(`@ControllerAdvice(basePackages=controller)`: Forbidden → 403, Exception → 500 + log.error), `views/error/403·404·500.jsp`(404는 BasicErrorController 자동), 삭제 POST의 Forbidden catch 제거
- 5-4 `ReviewControllerTest`(`@WebMvcTest` + `MockMvc` + `@MockitoBean`): 남의 리뷰 삭제 → 403, 비로그인 작성 → 로그인 redirect + Service 미호출
- (M4 정리) `MemberServiceImpl.deleteAccount`의 `// TODO: 4-3 후 삭제` 주석 삭제 (교육생)
- 5-1 `ImageStorage` + `LocalImageStorage`(UUID 저장명, 확장자 화이트리스트, `UncheckedIOException`) + `FakeImageStorage` → 빈 충돌(`NoUniqueBeanDefinitionException`) 재현 → `@Profile("local"/"fake")` + `spring.profiles.active` → `ImageStorageTest` 4개 🟢, 빈 0개(`NoSuchBeanDefinitionException`) 실험

### 📚 배운 개념

- **Controller 테스트 (교안 MVC pt.2 Part 1)**: Controller는 DispatcherServlet을 거쳐 불리므로 직접 호출로는 매핑·바인딩·View·예외 변환을 검증 못 함 → Mock 환경(Tomcat만 가짜, DispatcherServlet~Controller는 진짜). `@WebMvcTest(X.class)` = 웹 계층만(Controller·Advice·WebMvcConfigurer, Service·DAO·DB 제외, Boot 4 import `org.springframework.boot.webmvc.test.autoconfigure`), `MockMvc` = 가짜 요청, `@MockitoBean` = 가짜 빈(`@MockBean`은 Boot 4에서 제거). 대사: `when(..).thenReturn(..)` / void는 `doThrow(..).when(mock).메서드(..)`, 호출 여부 `verify(mock, never())`. 요청 만들기 → `perform` → `andExpect`(Given-When-Then), `sessionAttr`로 로그인한 척. MockMvc는 JSP를 실행하지 않음 → 상태 코드·View 이름·Model·redirect까지만

- **예외 처리 (교안 MVC pt.2 Part 3)**: 처리 안 된 예외·404 → Tomcat이 `/error`로 내부 이동 → `BasicErrorController`가 `error/404.jsp` → `error/4xx.jsp` → `error.jsp` → Whitelabel 순서로 화면 선택(로깅·예외별 처리는 못 함). `@ControllerAdvice` + `@ExceptionHandler` = Controller에서 터진 예외를 한 곳에서(가장 구체적인 타입 우선, `@ResponseStatus` 없으면 200, 반환은 `ModelAndView`). 우선순위: Controller 안 try-catch → Advice → BasicErrorController. `basePackages`로 범위를 한정하면 404·405(Controller 선택 전 실패)는 Advice 밖 → BasicErrorController. 500 화면엔 내부 메시지 대신 고정 문구 + `log.error`. 사용자 입력값을 다시 보여 줄 땐 `<c:out>`(XSS 방지)

- **DI 진화 (교안 DI 4장)**: Step 1 직접 `new`(구체 클래스) → Step 2 인터페이스 선언 + 여전히 `new`(DIP 절반, OCP 위반) → Step 3 생성자로 주입 + 팩토리 → Spring 컨테이너가 빈 생성(싱글톤)·주입 = DI, 제어권 이동 = IoC. 생성자 주입이 권장(불변·누락 방지·테스트에서 `new X(new Fake())` 가능)
- **같은 타입 빈 2개**: `NoUniqueBeanDefinitionException` / 0개: `NoSuchBeanDefinitionException`. 해결: `@Primary`(구현체에, 기본값), `@Qualifier("이름")`(주입받는 쪽, 우선), `@Profile("이름")`(그 프로파일일 때만 빈 등록 → `spring.profiles.active`, 테스트는 `@ActiveProfiles`). `@Autowired` 선택 순서: 타입 → Qualifier → Primary → 이름
- **MultipartFile (교안 MVC pt.2 Part 4)**: `isEmpty`·`getOriginalFilename`·`getContentType`·`getSize`·`transferTo(File)`. 저장명은 UUID(파일명 충돌·경로 조작 방지), 확장자는 Spring `StringUtils.getFilenameExtension`, 폴더 없으면 `Files.createDirectories`, `transferTo`는 **절대 경로**

### 🙋 내 답변과 피드백

| 질문 | 내 답 | 피드백 |
|---|---|---|
| 5-1 Q1. `ReviewServiceImpl`에서 `new LocalImageStorage()`로 직접 만들면 Fake로 바꿀 때 어디를 고치나? | 서비스 코드를 고쳐야 함, Step 2 문제 | ✅ 정답. 인터페이스로 선언해도 `new`가 남아 있으면 DIP 절반 → 교체 때마다 Service 수정(OCP 위반) |
| 5-1 Q2. Local·Fake 둘 다 `@Component`면? 설정만으로 해결하려면? | 모호하다는 에러가 발생 | 🟡 방향 정답. 예외 이름 = `NoUniqueBeanDefinitionException`("expected single matching bean but found 2"). 두 번째 질문(설정만으로)은 답 없음 → **`@Profile`** (`@Primary`·`@Qualifier`는 코드에 붙여 고정) |
| 5-1 단계 B. `@Profile` 설계: ①`!test`/`test` vs ②`local`/`fake` + properties | ② properties 한 줄로 바꾸는 게 확인하기 쉬워서 | ✅ 완료 기준("Profile을 바꾸면 Fake로")을 보이기 좋은 선택. 함정: active를 지우거나 오타 → 빈 0개 → `NoSuchBeanDefinitionException` (실험으로 확인할 것) |
| 5-2 Q1. `enctype` 빠뜨리면 `MultipartFile`에는? | 모르겠어 | 설명: 브라우저가 urlencoded로 **파일 이름 글자만** 보냄 → multipart 요청이 아니라 Spring이 파일 파트를 못 만듦 → `required=false`면 **null**(에러 없이 사진 없는 리뷰로 저장 = 조용한 실패), `required=true`면 `MultipartException`("Current request is not a multipart request") 류 400 |
| 5-2 Q2. 사진 저장을 Controller가 아니라 Service에서? | 모르겠어 | 설명: "리뷰를 쓰면 사진도 저장하고 그 이름을 리뷰에 넣는다" = 업무 규칙 → Service. ① 작성·수정·(나중에) REST API 등 입구가 늘어도 한 곳 ② Controller는 HTTP(파라미터 받기·redirect)만 ③ 사진+DB를 한 업무 단위로(트랜잭션 경계 = Service) ④ Service 테스트에서 Fake로 검증 가능 |
| 5-2 Q3. 사진 저장 후 insert 실패로 rollback되면 파일은? | 사진 파일은 rollback 되지 않음 | ✅ 정답. rollback은 DB만 되돌림 → 디스크에 **고아 파일**이 남음. 대책(👀): 실패 시 catch에서 파일 삭제, 커밋 후 저장(`TransactionSynchronization`), 주기적 정리 배치. 반대로 파일 저장 실패(`UncheckedIOException`)는 RuntimeException이라 `@Transactional`이면 DB도 rollback |
| 5-3 Q1. Advice에 `@ExceptionHandler(ForbiddenException.class)`를 만들면 Controller의 catch 3곳은? | 모르겠어 | 설명: **지워도 된다**. Controller가 안 잡은 예외는 밖으로 던져지고 → DispatcherServlet이 Advice의 핸들러를 찾아 실행 → 403 페이지. Controller에는 성공 흐름만 남음(M4의 "횡단 관심사를 한 곳에"와 같은 생각). 단, catch가 남아 있으면 Controller가 먼저 잡아서 Advice까지 안 감(우선순위 1위 = Controller 안의 try-catch) |
| 5-3 Q2. 없는 주소 요청을 `basePackages` 지정 Advice의 `Exception` 핸들러가 처리? | 모르겠어 | 설명: **처리 못 한다**. 없는 URL은 우리 Controller가 선택되기 전에 실패(정적 리소스 Handler가 `NoResourceFoundException`) → 범위 밖 → `/error` → BasicErrorController → `views/error/404.jsp`. = M5 셀프 체크 Q2의 답 |
| 5-4 Q1. `@WebMvcTest`에서 `@MockitoBean ReviewService`를 빠뜨리면? | 모르겠어 | 설명: `@WebMvcTest`는 Service를 안 올림 → `ReviewController` 생성자에 넣을 `ReviewService` 빈이 0개 → **`NoSuchBeanDefinitionException`**(5-1 ⑤ 실험과 같은 예외) → 테스트 시작도 못 함 |
| 5-4 Q2. DB 없이 "남의 리뷰 삭제" 상황을 만들려면? | 모르겠어 | 설명: 진짜 Service가 DB를 보고 던지던 `ForbiddenException`을 **가짜 Service가 던지게 대사를 정함** → `doThrow(new ForbiddenException("...")).when(reviewService).deleteReview(1, 2)` (void라 `when().thenReturn()` 대신 doThrow 순서). Controller 입장에선 진짜와 구분 불가 → Controller·Advice만 격리 검증 |
| M5 셀프 체크 Q1. `ImageStorage` 구현체가 바뀌어도 `ReviewService`를 안 고치는 이유? | 모르겠어 | 설명: `ReviewServiceImpl`은 **인터페이스 타입**(`ImageStorage`)에만 의존하고 직접 `new` 하지 않음 → 어떤 구현체를 넣을지는 Spring 컨테이너가 `@Profile` 설정을 보고 결정해 **생성자로 주입**(DI). = DIP(둘 다 추상화에 의존) + OCP(새 구현체 추가는 열려 있고 Service 수정은 닫혀 있음). 5-1에서 properties 한 줄·`@ActiveProfiles`로만 바꿨던 것이 증거 |
| M5 셀프 체크 Q2. 404는 왜 Advice가 아니라 BasicErrorController? | 모르겠어 | 설명: `@ControllerAdvice(basePackages=controller)`는 **그 Controller가 실행되다가 던진 예외**만 담당. 없는 URL은 HandlerMapping에서 맞는 Controller를 못 찾아 정적 리소스 Handler가 `NoResourceFoundException` → **우리 Controller가 실행된 적 없음** → 범위 밖 → Tomcat이 `/error`로 → BasicErrorController가 `error/404.jsp` 선택. 405도 같은 이유 |

### 🛠️ 트러블슈팅

- **(2026-11-22) 에러 JSP 3개를 `MatzipNote/src/`(403·404·500.jsp)에 만듦** → ViewResolver는 `/WEB-INF/views/` + 이름 + `.jsp`(=`src/main/webapp/WEB-INF/views/error/403.jsp`)만 찾음, `src/` 바로 아래는 웹 경로도 클래스패스도 아님 → 그대로 두면 Advice는 500(뷰 못 찾음)·404는 Whitelabel. `views/error/` 폴더를 만들고 옮기기. → ✅ 옮김 확인(2026-11-22), `src/` 바로 아래 남은 파일 없음. 내용은 아직 STS 기본 틀(제목 "Insert title here", body 비어 있음) STS: Package Explorer에서 `src/main/webapp`이 안 보이면 Project Explorer 사용, 파일은 드래그로 이동

- **(2026-11-22) 업로드 후 500: `Unresolved compilation problem: The local variable rno may not have been initialized`**
  - 원인: `int rno;` 선언 → try 안에서만 대입 → catch 2개가 **비어 있어서** 예외가 나면 rno에 값이 없는 채로 아래 `"redirect:/review/" + rno`까지 내려감. Java의 **확정 대입(definite assignment)** 규칙 위반 = 컴파일 에러
  - 왜 실행은 됐나: STS(Eclipse 컴파일러)는 에러가 있어도 클래스를 만들고, 그 줄이 실행될 때 `java.lang.Error`를 던짐. Maven(`mvnw`)이면 빌드 자체가 실패. → STS의 빨간 X(Problems 탭)를 무시하고 실행하지 않기
  - 해결 방향: catch 안에서 **바로 return**(flash msg + 작성 폼으로 redirect) — M3 삭제의 "catch 뒤 계속 실행" 실수와 같은 패턴. 성공 코드는 try 안 또는 catch가 모두 return한 뒤

### ❓ 내가 한 질문

- **(2026-11-22) 저장 폴더가 없으면 만드는 방법?**
  → `java.nio.file.Files.createDirectories(Path)`: 중간 폴더까지 전부 만들고, **이미 있으면 아무 일도 안 함**(예외 없음). `createDirectory`(s 없음)는 부모가 없거나 이미 있으면 예외 → 쓰지 않기. `Path`는 `Path.of(문자열)`(또는 `Paths.get`)로 만들고, 파일 경로는 `폴더Path.resolve(저장명)` → `transferTo(Path)`도 가능. checked `IOException`을 던지므로 try-catch. 호출 시점: 저장할 때마다(간단) 또는 빈 생성 직후 한 번 `@PostConstruct`(DI 교안 생명주기). 옛 방식 `new File(경로).mkdirs()`는 실패해도 예외 없이 `false`만 반환
- **(2026-11-22) `MvcExceptionHandler`는 어디에 작성하나?**
  → `src/main/java/com/study/matzip/config`(샘플 FW_05도 `configure` 패키지에 `MvcConfigurer`와 나란히). 조건은 하나: `@ControllerAdvice`가 품은 `@Component` 덕분에 **컴포넌트 스캔 범위(= `MatzipNoteApplication`이 있는 `com.study.matzip` 아래)** 안이면 어디든 빈으로 등록됨. 적용 대상은 위치가 아니라 `basePackages`가 정함. JSP는 `src/main/webapp/WEB-INF/views/error/` (prefix `/WEB-INF/views/` + `"error/403"`)
- **(2026-11-22) 핸들러는 메서드로 작성하나?**
  → 네. Advice **클래스** 1개 안에 `@ExceptionHandler`가 붙은 **메서드**를 예외 종류별로 여러 개. 메서드 구성: `@ExceptionHandler(예외.class)`(무엇을) + `@ResponseStatus(HttpStatus.XXX)`(상태 코드, 없으면 200) + 반환형 `ModelAndView`(데이터+뷰 이름) + **파라미터 = 그 예외 객체**(Spring이 던져진 예외를 넣어 줌 → `e.getMessage()`, `log.error(.., e)`). 메서드 이름은 자유. Controller 메서드처럼 Spring이 대신 호출(직접 부르지 않음) = IoC. 클래스 애너테이션 ✅(`@ControllerAdvice(basePackages=...)`, `@Slf4j`)
- **(2026-11-22) `@Value`는 어떻게 쓰나?**
  → `org.springframework.beans.factory.annotation.Value`(Lombok `@Value`와 다름 ⚠️). `@Value("${키}")` = properties 값을 빈에 넣음(`${}`가 없으면 글자 그대로). 기본값 `${키:기본값}`, 키가 없고 기본값도 없으면 앱 시작 실패(`Could not resolve placeholder`). 타입은 자동 변환(int, List 등 — FW_03 `service.manager.age`, `service.roles`). 위치: ① 필드(샘플 FW_05 `SimpleService`·`MvcConfigurer`, `final` 아님) ② 생성자 파라미터. ⚠️ `@RequiredArgsConstructor` + `final` 필드에 `@Value`를 붙이면 Lombok이 만든 생성자 파라미터로 **복사되지 않아** 주입 실패 → 필드 방식(비final) 또는 생성자를 직접 작성. 빈(Spring이 만든 객체)에서만 동작, `new`로 만든 객체에선 null
- **(2026-11-22) `@Value("#{app.upload.path}")`로 쓰는 것 아닌가?**
  → ❌ `#{ }`는 **SpEL**(Spring Expression Language, 식을 계산), `${ }`는 **프로퍼티 자리표시자**(properties 값 읽기). `#{app.upload.path}`는 "`app`이라는 이름의 빈/변수의 upload.path 속성"으로 해석 → 앱 시작 시 `SpelEvaluationException: EL1008E: Property or field 'app' cannot be found` 류 에러. properties 값은 `${app.upload.path}`. (둘을 섞으면 `#{'${app.upload.path}'.trim()}`처럼 쓸 수 있지만 지금은 필요 없음.) JSP의 `${ }`(EL)와도 다른 문법. 💅 필드는 `private`
- **(2026-11-22) `writeReview(Review review)`의 `review`에 업로드한 사진이 자동으로 안 들어가나?**
  → **지금 구조로는 안 들어간다.** `@ModelAttribute` 바인딩은 **파라미터 이름 = DTO의 setter 이름**으로 짝지음(파일 파트도 대상). `Review`에는 `String imageName`(저장명)만 있고 `MultipartFile` 필드가 없음 → `name="image"` 파트는 짝이 없어 무시됨. (`name="imageName"`으로 보내면 `MultipartFile`→`String` 타입 불일치 → 바인딩 에러) `Review`에 `MultipartFile image` 필드를 추가하면 자동 바인딩은 **가능**하지만 비추천: `Review`는 DB 한 행을 담는 DTO(MyBatis·세션·JSP까지 감)인데 웹 업로드 전용 객체가 섞임. 실무에선 폼 전용 DTO(예: `ReviewForm`)를 따로 만들기도 함. 이번엔 **Controller 파라미터로 따로 받는** 방식(교안 23장과 같음)

### ⏭️ 남은 일

- (2026-11-22) **교육생 요청: M5부터는 교안을 미리 보지 않았으므로 튜터가 교안 내용을 먼저 설명한 뒤 질문** → 5-1 교안 설명(DI Step 1~3 → Spring, 같은 타입 빈 2개, `@Primary`·`@Qualifier`·`@Profile`) 후 확인 질문으로 다시 진행
- [x] 5-1 🔍 `ImageStorage` 인터페이스로 DI 체감 ✅ (2026-11-22, ⑤ 실험: properties 주석 처리 → `NoSuchBeanDefinitionException`(빈 0개) 확인 후 `local`로 복구) — **단계 A ✅ (2026-11-22)**: `ReviewServiceImpl`에 `private final ImageStorage imageStorage` 추가 → `ReviewDaoTest` 7개 전부 Error, `NoUniqueBeanDefinitionException: ... expected single matching bean but found 2: fakeImageStorage,localImageStorage` (빈 이름 = 클래스 이름 첫 글자 소문자). 원인 사슬: reviewController(생성자 파라미터 0) → reviewServiceImpl(파라미터 1) → ImageStorage 2개. ReviewDaoTest는 Service를 안 쓰는데도 실패 = `@SpringBootTest`는 컨텍스트 전체를 띄우고 싱글톤 빈을 시작할 때 미리(eager) 만든다. 단계 B(`@Profile` 설계) 질문 제시 → 교육생이 방법 ② 선택, 요구사항 제시: Local `@Profile("local")`·Fake `@Profile("fake")`, `spring.profiles.active=local`, 프로파일 로그 확인, `ImageStorageTest`(`@ActiveProfiles("fake")` + `assertInstanceOf` + `MockMultipartFile`로 save 검증), 0개 실험. → (2026-11-22) 교육생이 ①`@Profile("local")`/`@Profile("fake")` ②`spring.profiles.active=local` 작성 ✅. **교육생 요청으로 튜터가 `ImageStorageTest` 작성**(Fake 주입 확인 `assertInstanceOf`, `fake-cat.png`, 빈 파일·null → null) → 4개 🟢. 같은 실행에서 ImageStorageTest는 `profile is active: "fake"`, ReviewDaoTest는 `"local"` = `@ActiveProfiles`가 클래스 단위로 properties보다 우선. ReviewDaoTest 7개 🟢(충돌 해결). 남은 것: ⑤ properties를 fake로 바꿔 보기 + 주석 처리 실험(빈 0개 예외) → local로 복구 (2026-11-22 `FakeImageStorage` 1차 통과 ✅: null/empty 가드로 인터페이스 약속 지킴, 저장 안 하고 `"fake-" + 원래 이름`, `@Slf4j` 로그. 🟡 생각거리: `image_name`은 VARCHAR(50) → 원래 이름이 길면 Fake만 "Data too long"(Local은 UUID 36자+확장자라 OK) / Fake는 확장자 검사를 안 해서 Local과 동작이 다름. 💅 로그에 `Fake` 표시) (2026-11-22 `LocalImageStorage.save` 1차 리뷰) ✅ properties `.` 제거, Spring `@Value` import, `private`, null/empty 가드, `StringUtils.getFilenameExtension`, UUID 저장명, `createDirectories`의 반환 Path 활용. 🔴 `path.resolve(finalName);` 반환값 버림(Path는 불변 → 새 Path를 돌려줌) → 38행이 **폴더 경로**에 저장 시도 / 🔴 `transferTo`의 `IOException`이 try 밖 → 컴파일 에러 / 🔴 `return null` → 저장명 반환 / 🟡 `printStackTrace` + `new RuntimeException()`(메시지·원인 없음) → `UncheckedIOException("메시지", e)`, 로그는 SLF4J / 🟡 확장자 없으면 `uuid.null` / 💅 `Path path = null` 밖 선언 대신 try 안에 모두. → **교육생 요청으로 튜터가 수정**: `Path target = dir.resolve(finalName)`, 폴더 생성·resolve·`transferTo`를 한 try에, `UncheckedIOException("이미지 저장 실패: ...", e)`, `return finalName`, 확장자 화이트리스트(png/jpg/jpeg/gif, 소문자 비교, 없으면 `IllegalArgumentException`), `@Slf4j` 저장 로그. 컴파일 OK. (중간 확인) `@Value("${app.upload.path}")` 문법 ✅. 🔴 import가 `lombok.Value`(→ `org.springframework.beans.factory.annotation.Value`, 이대로면 컴파일 에러) / 🔴 properties 값 끝의 `.`(`matzip-uploads.`) 제거 / 💅 `private`. 작성 중: `Files.createDirectories(@Value )` → `Path.of(imagePath)`. (2026-11-22) 교육생 요청으로 단계 A 명세를 진행 상황 체크리스트와 함께 다시 제시 (import·`private` 수정 확인, 남은 것: Local ②~⑤, Fake, ReviewServiceImpl 주입, 충돌 확인) ← 개념 질문 제시 (2026-11-22, 튜터가 교안 [Framework - DI] 4장 Step 1~3·8장, [Spring Bean 등록 방법] 11장 `@Primary`·`@Profile` 확인)
  - `LocalImageStorage`(디스크 저장, UUID 파일명) / `FakeImageStorage`(저장 안 하고 이름만 반환)
  - 같은 타입 빈 2개 → `NoUniqueBeanDefinitionException` 확인 → `@Primary` 또는 `@Profile`로 교체
- [x] 5-2 🔍 ✅ (2026-11-22, 5-2c 브라우저 확인: 사진 없이 수정해도 원래 사진 유지 = M3에서 미룬 버그 해결. 확인 Q(hidden으로 원래 이름?)는 답 없이 넘어가 튜터 설명: hidden 값은 F12로 바꿀 수 있음 → 남의 사진 파일명을 내 리뷰에 연결하거나 엉뚱한 값 저장 가능 → "누구의/무엇의 원본"은 폼이 아니라 DB에서 — M3 작성자 mno와 같은 원칙) — (2026-11-22 5-2a 2차 리뷰) ✅ properties 접두사 수정, 업로드 성공: `~/matzip-uploads/5857157e-...png`(1.1MB → 기본 1MB를 넘었는데 저장됨 = `max-file-size` 설정 효과), catch마다 return → 컴파일 OK. 🔴 catch의 `return "review/list"` = redirect가 아닌 **뷰 이름(forward)** → flash는 redirect 때만 전달되므로 메시지 안 보임 + list.jsp에 필요한 목록 데이터 없음 → `redirect:/review/write` / 🟡 flash 키 `error` → M3에서 통일한 `msg` + write.jsp에 `<c:if>` 표시 / 🟡 `UncheckedIOException` 메시지(`이미지 저장 실패: uuid.png`)는 내부 정보 → 사용자용 문구 + 상세는 log / 💅 `int rno = 0` 초기화는 확정 대입 보호를 끔 → catch가 모두 return하니 `int rno;`로도 충분(또는 try 안으로). → **교육생 요청으로 튜터가 수정**: catch 두 곳 `return "redirect:/review/write"` + flash 키 `msg`, `UncheckedIOException`은 `log.error` + 사용자용 문구, `IllegalArgumentException`은 `e.getMessage()` 그대로, `int rno;`(초기값 제거), write.jsp 상단에 `<c:if test="${not empty msg}">`(빨간색). 컴파일 OK. → **5-2a ✅ (2026-11-22, 교육생 브라우저 확인: `.txt` → 폼 + 메시지, 사진 없이 → NULL, 업로드 → image_name = UUID)**. 5-2b 요구사항 제시(`config/WebConfig implements WebMvcConfigurer` + `addResourceHandlers("/upload/**")` + `Path.of(...).toUri()`, detail.jsp `<c:if>` + `<img>`) → **교육생 요청으로 튜터가 작성**: `WebConfig`(`@Configuration`, `@Value("${app.upload.path}")`, `toUri()` + 끝 `/` 보정, 매핑 로그), detail.jsp 표 아래 `<c:if test="${not empty review.imageName}">` + `<img width=300 alt>`. resultMap에 `image_name → imageName` 이미 있음(M2). 컴파일 OK. → **5-2b ✅ (2026-11-22, 교육생 브라우저 확인: 사진 잘 보임)**. Q1·Q2는 답 없이 넘어가 튜터가 설명: Q1 끝 `/` 없으면 `matzip-uploads` + `abc.png` = `matzip-uploadsabc.png` 같은 엉뚱한 경로(또는 폴더가 아니라 파일로 해석) → 404 / Q2 사진 요청은 ResourceHttpRequestHandler가 직접 응답, Controller·Service를 안 거침 → `execution(* ..service.*.*(..))`에 안 걸려 시간 로그 없음. 5-2c(수정 시 image_name null 덮어쓰기) 설명·요구사항 제시 → **교육생 요청으로 튜터가 작성**: Service `updateReview(mno, review, image)` — `checkReviewMno` 반환값(DB 원본)을 받아 새 저장명이 없으면 원본 `imageName` 유지(SQL은 그대로), 커스텀 예외 `InvalidImageException extends IllegalArgumentException`(LocalImageStorage가 던짐) → edit POST에서 **하위 타입 먼저** catch(→ 수정 폼) / `UncheckedIOException`(→ 수정 폼) / `IllegalArgumentException`(찾을 수 없음 → 목록) / Forbidden, write POST도 `InvalidImageException`으로, edit.jsp `enctype` + 현재 사진 미리보기 + 파일 입력 + msg. 전체 테스트 22개 중 21 통과·1 Skipped. 남은 것: 브라우저 확인 3가지 + 확인 질문(hidden으로 원래 이름 보내면 문제?) 답. (5-2a 1차 리뷰, 컴파일 OK) ✅ `enctype`, `name="image"`(DTO 필드명과 다르게), `accept`, `@RequestParam(required=false) MultipartFile image`, Service 시그니처(인터페이스·구현), save → setImageName → insert(if 없이 null 그대로), insert SQL에 `#{imageName}` 이미 있음. 🔴 properties `max-request-size = 50MB` 접두사 없음 → 모르는 키라 **에러 없이 무시**(기본 10MB 유지) → `spring.servlet.multipart.max-request-size` / 🟡(🌟) 이미지 아닌 파일 → `IllegalArgumentException` 미처리 → 500 화면 / 💅 `=` 앞뒤 공백은 허용되지만 파일 안 다른 줄과 통일. ← 교안 MVC pt.2 Part 4 설명 + 확인 Q 3개(2026-11-22), 5-2a 요구사항 제시(multipart 크기 설정, write.jsp `enctype`·`<input type=file name=image accept>`, Controller `@RequestParam(required=false) MultipartFile image`, Service `writeReview(review, image)`에서 `save` → `setImageName` → insert, 이미지 아닌 파일 → `IllegalArgumentException` 처리, DB·폴더 확인). 리뷰 작성 시 사진 업로드(`MultipartFile`) + `addResourceHandlers`로 화면 표시 + (M3에서 미룸) 수정 시 `image_name` null 덮어쓰기 해결
- [x] 5-3 👀 ✅ (2026-11-22, 교육생 브라우저 확인: 남의 리뷰 삭제 → 403 페이지, 없는 주소 → 404 페이지) ← 교안 MVC pt.2 Part 3 설명 + 확인 Q + 짧은 가이드 제시 (2026-11-22). `MvcExceptionHandler`(config 패키지) 1차 리뷰: ✅ 클래스 애너테이션·import·핸들러 2개의 `@ExceptionHandler`+`@ResponseStatus`+`ModelAndView` 반환. 🔴 파라미터(예외 객체) 없음, `return null`(빈 응답), ②에 `log.error` 없음 → **교육생 요청으로 튜터가 완성**: ① `handleForbidden(ForbiddenException e)` `log.warn` + `error/403` + `msg` ② `handleException(Exception e)` `log.error(.., e)` + `error/500` + 사용자용 고정 문구(내부 메시지 노출 X). 컴파일 OK. → **교육생 요청으로 튜터가 에러 JSP 3개 작성**(taglib + root, 403: 안내 + `${msg}` + 목록 링크 / 404: BasicErrorController의 `${path}`를 `<c:out>`으로 이스케이프 + 메인 링크 / 500: 고정 문구 `${msg}`만, 예외 정보 미노출). → **교육생 요청으로 튜터가 삭제 POST의 `catch (ForbiddenException e)` 제거**(이유 주석, `IllegalArgumentException` catch는 유지 → 없는 리뷰는 목록 + msg), 컴파일 OK. 수정 폼·수정 저장의 Forbidden catch 2곳은 그대로(상세 + msg). 남은 것: 브라우저 확인(남의 글 삭제 → 403, 없는 주소 → 404). `@ControllerAdvice` + `@ExceptionHandler`: 권한 예외 → 403 페이지, 그 외 → 500, `error/404.jsp`
- [x] 5-4 👀 ✅ (2026-11-22, **교육생 요청으로 튜터가 `ReviewControllerTest` 작성**: `@WebMvcTest(ReviewController.class)` + `MockMvc` + `@MockitoBean ReviewService` / ① `doThrow(ForbiddenException).when(reviewService).deleteReview(1, 2)` + `sessionAttr("loginUser")` → `status().isForbidden()`, `view().name("error/403")`, `model().attribute("msg", ...)` = Controller + Advice 함께 검증 ② 비로그인 `POST /review/write` → `redirectedUrl("/member/login")` + `verify(reviewService, never()).writeReview(any(), any())`. 2개 🟢, 전체 24개 중 23 통과·1 Skipped. DBConfig의 `@MapperScan`은 slice에서 제외돼 문제 없음) ← 교안 MVC pt.2 Part 1 설명 + 확인 Q + 가이드(삭제 남의 글 → 403 / 비로그인 작성 → 로그인 redirect) 제시 (2026-11-22). Boot 4 import `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`(샘플 FW_05 확인). `@WebMvcTest` + `MockMvc` + `@MockitoBean`으로 리뷰 Controller 테스트 1~2개
- [x] 셀프 체크 ✅ (2026-11-22, 두 문제 모두 "모르겠어" → 튜터 설명으로 정리): `ReviewService`는 `ImageStorage` 구현체가 바뀌어도 왜 코드를 고칠 필요가 없을까? / 404는 왜 `@ControllerAdvice`가 아니라 BasicErrorController가 처리할까?
- [x] 완료 기준 ✅: 사진 있는 리뷰가 화면에 보이고, `@Profile`을 바꾸면 `FakeImageStorage`로 동작. 남의 리뷰 삭제 시 403 페이지, MockMvc 테스트 통과

### 📌 다음 세션 시작점

**맛집 노트 M1~M5 전체 완료 ✅ (2026-11-22)** → (2026-11-22) 교육생 결정으로 **M6 Interceptor, M7 REST API 추가** (PLAN.md 참고). 다음 세션: M6 시작 — 튜터가 Notion [Interceptor] 교안을 먼저 설명(M5부터의 방식) → 확인 질문 → `LoginCheckInterceptor` 요구사항. 전체 테스트 24개(23 통과, 1 Skipped = M4 롤백 실험용 `@Disabled`).
- 선택 과제(원하면): ① M2·M3에서 미룬 `@MybatisTest` + `@Mapper` 실험 ② 로그인 검사 5곳을 `HandlerInterceptor`로 모으기(`WebConfig.addInterceptors`) ③ 수정 폼·수정 저장의 `catch (ForbiddenException)` 정리(Advice로 일원화) ④ 고아 사진 파일 정리(수정·삭제 시 옛 파일 삭제) ⑤ 셀프 체크 다시 풀어 보기(M4·M5)
- 환경 메모: `spring.profiles.active=local`(업로드는 `~/matzip-uploads`), 명령줄 Maven은 `JAVA_HOME=C:/Users/woook/.jdks/ms-25.0.5.0.2`

---

## M6. Interceptor — ⏸️ 보류 (2026-11-22, 교육생 결정으로 넘어감)

### ✅ 한 일

### 📚 배운 개념

### 🙋 내 답변과 피드백

| 질문 | 내 답 | 피드백 |
|---|---|---|

### 🛠️ 트러블슈팅

### ❓ 내가 한 질문

- **(2026-11-22) Interceptor가 뭔지부터 설명해 줄래?**
  → "Controller 앞에 서 있는 **검문소**". DispatcherServlet이 요청에 맞는 Controller 메서드를 찾은 뒤, **부르기 직전**(`preHandle`)과 끝난 뒤(`postHandle`·`afterCompletion`)에 먼저 들르는 Spring 객체. 통과(`true`)면 Controller로, 막으면(`false`) Controller는 실행조차 안 됨. 왜 필요? 지금 Controller 5곳에 같은 "로그인 했나?" 코드가 복사돼 있음(횡단 관심사) → 검문소 하나에 모으고 어느 주소에 세울지(`addPathPatterns`)만 정하면 됨. 비교: Servlet Filter(같은 아이디어, 하지만 Spring 바깥·DispatcherServlet 앞) / AOP(빈 메서드 단위, HTTP 모름) / `@ControllerAdvice`(Controller가 **던진 예외** 처리 = 나간 뒤) vs Interceptor(Controller **들어가기 전** 검사). 등록은 `WebConfig.addInterceptors`
- **(2026-11-22) Interceptor도 AOP 느낌인가?**
  → **생각(목적)은 같다, 동작 방식은 다르다.** 공통: 횡단 관심사를 한 곳에 모으고 대상 코드는 손대지 않음 = AOP의 "관점" 아이디어(교안 AOP 1장 호텔 팻말 비유). 차이: Spring AOP는 **프록시**가 빈 메서드 호출을 가로챔(Pointcut = 메서드 시그니처, HTTP 모름) / Interceptor는 프록시 없이 **DispatcherServlet이 직접** 등록된 목록을 순서대로 호출(HandlerExecutionChain, 대상 = URL 패턴, request·response·handler 사용). 그래서 Interceptor는 Controller 앞에만, AOP는 아무 빈에나. `afterCompletion`은 `@After`, `preHandle`의 false는 `@Before`에서 예외로 막는 것과 비슷

### ⏭️ 남은 일

- [ ] 6-1 🔍 (2026-11-22 1차 리뷰, 컴파일 OK) ✅ `@Component` + `implements HandlerInterceptor`, `preHandle`만, jakarta import, `getSession(false)`, `getContextPath()` + redirect + `return false`. 🔴 `session == null`만 검사 → 로그아웃 후에도 JSP가 기본으로 세션을 만들어서(`<%@ page session="true"%>` 기본값) **거의 항상 통과** → `loginUser` 속성까지 확인 / 🔴 `/Member/login`(대문자 M) → URL은 대소문자 구분 → 404 / 💅 주석 처리된 `return` 줄 삭제. (참고: WebConfig `addPathPatterns("/review/..")`는 Pointcut의 `..` 문법이라 ANT 패턴에선 글자 그대로 → 아무 주소에도 안 걸림) `LoginCheckInterceptor`(`preHandle`: 세션 `loginUser` 없으면 `sendRedirect(contextPath + "/member/login")` + `return false`) ← 교안 설명 + 확인 Q 1개 + 요구사항 제시 (2026-11-22, 교안 [Interceptor] 1~6·8장 확인)
- [ ] 6-2 🔍 `WebConfig.addInterceptors` 등록 + `addPathPatterns("/review/write", "/review/*/edit", "/review/*/delete")`
- [ ] 6-3 🔍 `ReviewController` 5곳의 `if (loginUser == null)` 블록 삭제 (`loginUser`를 꺼내는 줄은 mno 때문에 유지) → 전체 테스트 + 브라우저 확인
- [ ] 셀프 체크: `preHandle`이 false를 반환하면 무엇을 직접 해 줘야 할까?
- [ ] 완료 기준: Controller에 로그인 검사 코드 없이 비로그인 작성·수정·삭제 → 로그인 화면, 목록·상세는 그대로, 전체 테스트 통과

### 📌 다음 세션 시작점

**M6 보류 (2026-11-22)**: 교육생이 "Interceptor 파트는 넘어가겠다"고 결정. 개념 설명(검문소 비유, AOP와 비교)까지 완료, 구현은 1차 리뷰에서 멈춤.
- 현재 코드 상태: `config/LoginCheckInterceptor`(🔴 `session == null`만 검사, `/Member/login` 대문자) + `WebConfig.addInterceptors("/review/..")`(어느 주소에도 안 걸림) → **실제로는 아무것도 막지 않아 앱 동작에 영향 없음**. Controller 5곳의 로그인 검사는 그대로 동작. 전체 테스트 24개(1 Skipped) 통과 확인
- (2026-11-22) **교육생 요청으로 되돌림**: `LoginCheckInterceptor.java` 삭제, `WebConfig` M5 상태로 복구(`git checkout`), 컴파일 OK
- 다시 할 때: ① `loginUser` 속성까지 검사 ② `/member/login` 소문자 ③ 패턴 `/review/write`, `/review/*/edit`, `/review/*/delete` ④ Controller 5곳 정리

---

## M7. REST API — ✅ 완료 (2026-11-22) · [Notion](https://app.notion.com/p/4206412b0acd8132a470d0ea33650126)

### ✅ 한 일

- 7-1 `restcontroller/ReviewRestController`(교육생 작성): `GET /api/review/{rno}` → 200, `POST /api/review`(`@RequestBody`, 세션 확인 401, 작성자 mno는 세션) → 201 + 저장된 리뷰. `/review` → `/api/review`로 주소 충돌(`Ambiguous mapping`) 해결
- 7-2 `config/RestExceptionHandler`(클래스 부분 교육생, 메서드 튜터): 404·500 `ProblemDetail`
- 7-3 `ReviewRestControllerTest`(튜터) 4개 → Jackson 3 역직렬화 버그 발견·수정

### 📚 배운 개념

- **REST API (교안 REST API 7·9·11·13·14·16장)**: 화면 대신 데이터(JSON), URL = 자원(명사·복수형), HTTP 메서드 = 행위, "화면·세션 이야기 하지 말자". `@RestController` = `@Controller` + `@ResponseBody`(객체 → Jackson → JSON). `@RequestBody`(JSON body → 객체) ↔ `@ModelAttribute`(폼): Content-Type과 짝(틀리면 필드 null 또는 415). `ResponseEntity`로 상태 코드(ok 200, CREATED 201, UNAUTHORIZED 401)
- **API 에러**: `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`, `basePackageClasses`(그 클래스의 패키지 전체, 문자열보다 안전) + `@Order(HIGHEST_PRECEDENCE)`, 반환 `ProblemDetail`(RFC 7807/9457: type·title·status·detail·instance, Content-Type `application/problem+json`, status가 응답 코드). `basePackages`는 이름 앞부분 일치 → 하위 패키지 포함 → API는 형제 패키지 `restcontroller`
- **Advice 없이 예외가 나면**: Tomcat → `/error` → BasicErrorController → 브라우저(Accept: text/html)는 `error/500.jsp`, JSON 클라이언트는 기본 JSON(500) — 둘 다 원하는 결과 아님
- **`@ExceptionHandler` 동작**: `@Component`를 품은 Advice가 스캔으로 빈 등록 → `ExceptionHandlerExceptionResolver`가 시작 시 목록화 → 예외 시 범위 + 가장 가까운 예외 타입으로 골라 Spring이 호출 (Interceptor와 달리 별도 등록 불필요)
- **Jackson 3(Boot 4) 역직렬화**: `@AllArgsConstructor` + `-parameters` → 생성자 기반 생성 → JSON에 없는 기본형(int)은 null → `FAIL_ON_NULL_FOR_PRIMITIVES` 실패. GET(직렬화)은 무관, POST(역직렬화)만. 테스트로만 발견 가능했음(브라우저는 GET만)

### 🙋 내 답변과 피드백

| 질문 | 내 답 | 피드백 |
|---|---|---|
| M7 셀프 체크: 화면용 `@ControllerAdvice`와 API용 `@RestControllerAdvice`를 왜 나눌까? | ("다 했어요" — 답 없이 마무리) | 설명: 같은 예외라도 **받는 쪽이 원하는 형식이 다름**. 화면(브라우저)은 에러 **페이지**(HTML, `ModelAndView`), API(앱·JS)는 에러 **JSON**(`ProblemDetail`). 하나로 섞으면 API에 HTML이 가거나 화면에 JSON이 감 → 오늘 본 `/api/review/9999` → `500.jsp`. 그래서 패키지(`controller` / `restcontroller`)로 범위를 나누고 각자 다른 Advice가 담당 |
| M7 Q. `ReviewRestController`를 `controller.api`(하위 패키지)에 만들면? | 모르겠어 | 설명: `basePackages`는 **하위 패키지까지** 포함(M4 Pointcut의 `..`과 같은 개념) → 화면용 `MvcExceptionHandler`도 API Controller를 담당하게 됨 → 두 Advice가 동시에 해당, 화면용이 먼저 잡으면 API인데 **HTML 에러 페이지**(`error/500`)가 나감(교안 12장 "에러가 HTML로?"). 해결: 형제 패키지 `com.study.matzip.restcontroller`에 두고 `basePackageClasses`로 범위 분리(+ 필요하면 `@Order(Ordered.HIGHEST_PRECEDENCE)`) |

### 🛠️ 트러블슈팅

- **(2026-11-22) `POST /api/review`가 항상 500: `HttpMessageNotReadableException: JSON parse error: Cannot map \`null\` into type \`int\``** (튜터가 작성한 `ReviewRestControllerTest`가 발견)
  - 원인: Boot 4는 **Jackson 3**(`tools.jackson`). `Review`에 Lombok `@AllArgsConstructor`가 있고 `-parameters`로 파라미터 이름이 남아 있어서, Jackson이 setter 대신 **모든 필드를 받는 생성자**로 객체를 만듦 → JSON에 없는 `rno`(int) 자리에 null → 기본 설정 `FAIL_ON_NULL_FOR_PRIMITIVES`(true)로 실패. 그 예외를 `RestExceptionHandler`의 `Exception` 핸들러가 500으로 바꿈(원래 의미는 400)
  - GET 상세(직렬화: 객체 → JSON)는 문제없음, POST(역직렬화: JSON → 객체)만 실패
  - 해결(교육생 선택 A, 튜터 적용): `application.properties`에 `spring.jackson.deserialization.fail-on-null-for-primitives=false` → 없는 int는 0. 대안 B(`int`→`Integer`), C(테스트 JSON만 맞추기, 버그 은폐라 ❌)

- **(2026-11-22) `Ambiguous mapping. Cannot map 'reviewRestController' method ... There is already 'reviewController' bean method`** → 두 Controller가 같은 HTTP 메서드 + 같은 URL(`GET /review/{rno}`)을 매핑. HandlerMapping이 하나를 고를 수 없어 **앱 시작 시점에 실패**(빈 2개 `NoUnique`와 같은 종류의 "모호함"). API는 `/api/...`로 주소 공간을 분리

### ❓ 내가 한 질문

- **(2026-11-22) 예시 힌트 주세요** → 빈칸(`___`) 뼈대로 제공: ReviewRestController(클래스 애너테이션 3개, GET은 `ResponseEntity.ok`, POST는 세션 확인 → 401 / `setMno` → `writeReview(review, null)` → 201), RestExceptionHandler(`@RestControllerAdvice(basePackageClasses=..)` + `@Order`, 반환형 `ProblemDetail`, `forStatusAndDetail`), 테스트(`when().thenReturn/thenThrow`, `jsonPath("$.title").value(..)`)
- **(2026-11-22) `@ExceptionHandler`에 대해 설명해 줘**
  → "이 예외가 나면 이 메서드가 처리한다"는 **예외 전용 핸들러 메서드** 표시. Controller 밖으로 던져진 예외를 DispatcherServlet이 받아 → 맞는 `@ExceptionHandler`를 찾아 **Spring이 대신 호출**(= Controller 메서드처럼 IoC). 위치: Controller 안(그 Controller만) / `@ControllerAdvice`·`@RestControllerAdvice` 안(여러 Controller, 우리 방식). 선택 규칙: 예외 상속 구조에서 **가장 가까운 타입**(Forbidden 핸들러 > Exception 핸들러), 우선순위 Controller 안 try-catch > Controller 안 핸들러 > Advice > BasicErrorController. 파라미터: 처리할 예외 객체(+ request 등), 반환: 화면용 `ModelAndView`/뷰 이름, API용 `ProblemDetail`/`ResponseEntity`. try-catch와의 차이: try-catch는 메서드마다 복붙, 핸들러는 한 곳(횡단 관심사). 우리 프로젝트: `MvcExceptionHandler`(Forbidden→403, Exception→500), `RestExceptionHandler`(IllegalArgument→404, Exception→500)
- **(2026-11-22) `RestExceptionHandler`는 어디에 등록되고 어떻게 쓰이나?**
  → **따로 등록하는 코드가 없다.** ① 등록: `@RestControllerAdvice` 안에 `@ControllerAdvice` → 그 안에 `@Component` → `@SpringBootApplication`의 컴포넌트 스캔(`com.study.matzip` 아래)이 찾아 **빈으로 등록**(5-1 `@Component`와 같은 원리). ② 준비: 앱 시작 시 Spring MVC의 `ExceptionHandlerExceptionResolver`가 모든 Advice 빈과 그 안의 `@ExceptionHandler` 메서드 목록을 모아 둠. ③ 사용: Controller가 예외를 던지면 `DispatcherServlet` → Resolver가 "이 Controller가 범위(`basePackageClasses`)에 드는 Advice" 중 **가장 가까운 예외 타입**의 메서드를 골라 호출 → 반환한 `ProblemDetail`을 JSON으로. 대비: Interceptor는 `@Component`만으론 안 되고 `WebConfig.addInterceptors`에 직접 등록해야 했음(어느 URL에 세울지 정해야 하므로). Advice는 범위를 애너테이션 속성으로 적어 두니 자동
- **(2026-11-22) API Advice가 없을 때 `/api/review/9999`에서 500.jsp가 보이는 건 어디서 설정된 것?**
  → `MvcExceptionHandler`가 아님: `basePackages = "com.study.matzip.controller"`는 클래스 이름 **앞부분 일치**로 판단 → `com.study.matzip.restcontroller.ReviewRestController`는 `...controller`로 시작하지 않아 범위 밖. 실제 경로: 처리할 Advice가 없어 예외가 Tomcat까지 → `/error`로 내부 이동 → **BasicErrorController** → 브라우저는 `Accept: text/html`이라 `errorHtml()` → 5-3에서 만든 **`views/error/500.jsp`** 파일을 상태 코드로 찾아 사용(파일을 둔 것 자체가 설정). 그래서 `${msg}`가 비어 있음(Advice가 아니라 넣어 주는 사람이 없음). JSON 클라이언트(Accept: application/json)였다면 BasicErrorController의 `error()`가 기본 JSON(`timestamp, status, error, path`)을 줌 → 그래도 404가 아닌 500 → RestExceptionHandler가 필요한 이유

### ⏭️ 남은 일

- [x] 7-1 🔍 ✅ (2026-11-22 2차 통과: `/api/review`로 주소 분리 → 앱 시작 OK(MatzipNoteApplicationTests 통과), `.body(review)`로 201 + 새 rno. 💅 REST 규칙상 복수형 `/api/reviews` 권장(PLAN·테스트 힌트와 통일)) (2026-11-22 1차 리뷰, 컴파일 OK) ✅ 패키지 `restcontroller`, `@RestController`·`@RequiredArgsConstructor`, `@PathVariable`·`@RequestBody`, `ResponseEntity.ok`, 세션 확인 → 401, 작성자 mno는 세션에서, `writeReview(review, null)`, 201. 🔴 `@RequestMapping("/review")` → 화면용 `ReviewController`의 `GET /review/{rno}`와 **같은 주소** → 앱 시작 실패 `Ambiguous mapping. Cannot map 'reviewRestController' method`(컴파일은 통과, MatzipNoteApplicationTests Error로 확인) → `/api/reviews` / 🟡 201인데 `.build()`로 본문 없음 → 새 rno를 클라이언트가 모름 → `.body(review)` `restcontroller/ReviewRestController`(`@RestController`, `/api/reviews`): `GET /{rno}` → `ResponseEntity.ok(review)`, `POST`(`@RequestBody Review`, 세션 `loginUser` 없으면 401) → 201 Created ← 교안 [REST API] 7·9·11·13·14·16장 설명 + 확인 Q + 명세 제시 (2026-11-22)
- [x] 7-2 🔍 ✅ (2026-11-22) 교육생이 직접 작성하겠다고 해서 튜터 작성본 삭제 → **교육생이 클래스 부분 작성**(`@Slf4j`, `@RestControllerAdvice(basePackageClasses = ReviewRestController.class)`, `@Order(Ordered.HIGHEST_PRECEDENCE)`, import ✅) → 메서드는 교육생 요청으로 튜터가 채움(`noReview(IllegalArgumentException e)` → 404 `ProblemDetail`, `unexpectedError(Exception e)` → log.error + 500 고정 문구). 앱 시작 OK. (삭제 전 튜터 작성본 기록: `@RestControllerAdvice(basePackageClasses = ReviewRestController.class)` + `@Order(HIGHEST_PRECEDENCE)`, `IllegalArgumentException` → 404 `ProblemDetail`(log.warn), `Exception` → 500 고정 문구 + log.error. 앱 시작 OK. 👀 한계: JSON 형식이 틀리면 `HttpMessageNotReadableException` → Exception 핸들러가 500으로 바꿈(원래는 400) → 실무에선 400 핸들러 추가) `config/RestExceptionHandler`(`@RestControllerAdvice(basePackageClasses = ReviewRestController.class)`): `IllegalArgumentException` → 404 `ProblemDetail`, `Exception` → 500(고정 문구)
- [x] 7-3 🔍 ✅ (2026-11-22, **교육생 요청으로 튜터가 작성**: ① 상세 200 + `content().contentType(JSON)` + `jsonPath` ② 없는 번호 404 + `application/problem+json` + `$.status`·`$.detail` ③ 작성 201 + JSON의 mno=99(사칭)를 무시하고 세션 mno=2 ④ 비로그인 401 + Service 미호출. 처음 ③④가 500 → Jackson 3 역직렬화 버그 발견·수정 후 4개 🟢, 전체 28개 중 27 통과·1 Skipped) `ReviewRestControllerTest`(`@WebMvcTest`): 상세 200 + `jsonPath("$.title")`, 없는 번호 404 + `jsonPath("$.status")`
- [ ] 셀프 체크: 화면용 `@ControllerAdvice`와 API용 `@RestControllerAdvice`를 왜 나눌까?
- [ ] 완료 기준: `/api/reviews`로 JSON 조회·작성, 없는 리뷰는 ProblemDetail 404, jsonPath 테스트 통과

### 📌 다음 세션 시작점

**M7 완료 ✅ (2026-11-22) → 맛집 노트 실습 종료** (M1~M5, M7 완료 / M6 Interceptor 보류). 전체 테스트 28개(27 통과, 1 Skipped = M4 롤백 실험용 `@Disabled`).
- 선택 과제: ① M6 Interceptor 다시(📌 M6 챕터의 고칠 4가지) ② API 목록·수정·삭제(`GET /api/review`, `PUT`, `DELETE` 204) ③ JSON 형식 오류 → 400 핸들러(`HttpMessageNotReadableException`) ④ URL 복수형 `/api/reviews` ⑤ 👀 Swagger(springdoc)·CORS·RestClient 교안 읽기 ⑥ `@MybatisTest`/`@Mapper` 실험
