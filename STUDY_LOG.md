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

## M3. MVC 요청 처리 — 🟡 진행 중

### ✅ 한 일

- M3 시작 (2026-11-17)
- **3-1 로그인·로그아웃 완료** ✅ (2026-11-17): `MemberService.login`(통합 실패 메시지, password null) + `MemberServiceTest` 5개(튜터 작성) + `MemberController`(`/member/signup`·`/member/login` GET/POST 통합, `POST /member/logout` + `invalidate()`) + `login.jsp` + `index.jsp`(`<c:choose>`, 로그아웃 POST 폼) → 브라우저 확인 4가지 통과
- **3-2-① 리뷰 목록(검색) 완료** ✅ (2026-11-17): `ReviewService`/`ReviewServiceImpl`(search 위임) + `ReviewController` `GET /review/list`(`@ModelAttribute ReviewSearch`, `addAttribute("reviewList")`) + `review/list.jsp`(GET 검색 폼, `<c:choose>` 0건, `<c:forEach>` + `varStatus`) + index 링크 → 2차 통과, 브라우저 확인 5가지 통과 (조건 없음·키워드만·별점만·둘 다·빈 별점)
- **3-2-② 리뷰 상세 완료** ✅ (2026-11-17): Service `selectDetail`(DAO `selectDetail` JOIN 재사용, null → `IllegalArgumentException`) + `GET /review/{rno}`(`@PathVariable int rno`, `IllegalArgumentException`만 catch → `error`) + `review/detail.jsp`(`<c:choose>`, `${review.writer.name}`) + 목록 제목 링크(`item.rno`) → 3차 통과, 브라우저 4가지 통과 (클릭·없는 번호·`abc` 400·`/list` 우선)

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

### 🛠️ 트러블슈팅

**`signUp_duplicateEmail` 실패 — `Expected IllegalArgumentException to be thrown, but nothing was thrown` (2026-11-17)**
- 원인(추정): 테스트가 DB에 `kim@matzip.com`이 있다고 가정 → 지금 DB에 없어서 중복 예외가 안 남 (Workbench `SELECT email FROM member;`로 확인)
- 해결 방향: DB 데이터에 기대지 말고 `@BeforeEach`의 `setMem` 이메일로 재가입해 중복 상황을 테스트 안에서 만든다

### ❓ 내가 한 질문

- `@ModelAttribute`의 역할은? → ① **바인딩**: 기본 생성자로 객체 생성 → 파라미터 이름과 같은 setter 호출 (이름 안 맞으면 조용히 null, 타입 안 맞으면 400) ② **Model에 자동 추가**: 클래스 이름 첫 글자 소문자(`member`) → 실패 후 폼에서 `${member.email}`로 입력값 유지. `@RequestParam`은 값 하나·없으면 400·Model 추가 X. 객체 파라미터는 생략해도 같은 동작. (메서드에 붙이면 공통 Model 값, 참고만)

- 회원가입 URL을 `GET /signUp-form` + `POST /signup`으로 나누는 게 좋나, `/signup`으로 합치는 게 좋나? → **합치기 권장**: ① 자원 하나에 URL 하나, 보여 주기/처리는 HTTP 메서드가 구분 (REST) ② 실패 시 뷰를 바로 반환하면 주소창에 POST URL이 남음 → 합쳐 두면 늘 같은 주소, 다시 열면 GET 폼 ③ 로그인과 같은 규칙 ④ URL은 소문자 + kebab-case (`signUp` 대문자 섞임 → 오타 404). 고칠 곳: `MemberController.java:24`, `index.jsp:13` (`signup.jsp` action은 이미 `/member/signup`)

- 없는 이메일과 틀린 비밀번호의 예외를 분리해야 하나? → **화면 메시지는 하나로** (나누면 계정 열거 공격: 가입된 이메일을 골라낼 수 있음). 원인 구분은 **서버 로그**(`warn`, 이메일만, 비밀번호는 절대 X)에서. 구조: `if` 둘로 나눠 로그만 다르게 + 같은 메시지로 `throw` (지금처럼 한 줄도 OK). 회원가입의 "이미 가입된 이메일"도 노출이지만 실서비스는 시도 횟수 제한·이메일 인증으로 보완

- 상세 링크에 `${status.count}`(목록의 줄 번호)를 써야 하나? → **아니다, `${item.rno}`(DB의 리뷰 번호)**. `status.count`는 이번 화면에서 몇 번째 줄인지(1, 2, 3…)일 뿐 → 검색 조건·삭제·정렬에 따라 같은 리뷰도 줄 번호가 바뀜. `rno`는 그 리뷰의 고유 번호(PK), 어디서 봐도 같음 → `selectDetail(rno)`의 `WHERE r.rno = #{rno}`와 짝. 비유: 줄 번호 = 대기표 순서, rno = 주민번호

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
  - [ ] ③ 작성 `GET·POST /review/write` (작성자 = loginUser) ← 다음 할 일 (개념 Q: 작성자 mno를 폼에서 받으면 안 되는 이유)
  - [ ] ④ 수정·삭제 → 3-3 작성자 검사와 함께
- [ ] `@RequestParam` / `@ModelAttribute` / `@PathVariable` 바인딩
- [ ] 등록·수정·삭제 후 PRG + `addFlashAttribute`
- [ ] 작성자 본인 검사를 Service에 → 커스텀 예외
- [ ] 로그인 검사를 각 Controller 메서드에 직접 (M4 AOP 동기)
- [ ] (M2에서 미룸) `ReviewDaoTest` selectDetail·update·delete, `@MybatisTest` + `@Mapper` 실험, 괄호 빨간불 실험
- [ ] 셀프 체크: redirect 이유 / redirect 후 Model 값이 사라지는 이유 / 권한 검사를 Service에 두는 이유

### 📌 다음 세션 시작점

M3 3-2-③ 리뷰 작성(`GET·POST /review/write`, 작성자 = 세션 `loginUser`) 개념 Q 답부터 (3-2-② 상세 완료·커밋됨, a428610 이후 커밋 3개 push 안 함). 순서: `ReviewService` → 목록(검색) → 상세(`@PathVariable`) → 작성 → 수정 → 삭제. M2에서 미룬 `ReviewDaoTest` selectDetail·update·delete도 이때
