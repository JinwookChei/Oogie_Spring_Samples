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

## M2. 회원가입 관통 + MyBatis CRUD — 🟡 진행 중

### ✅ 한 일

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

### ⏭️ 남은 일

- [ ] 2-1 회원가입 한 줄 관통
  - [x] `Member` DTO ✅
  - [x] `MemberDao` + `member.xml` (insert + PK 받아오기, selectByEmail) + `@MapperScan` ✅
  - [x] `MemberDaoTest` (`@SpringBootTest` + `@Transactional`): insert / 있는 이메일 / 없는 이메일 → 통과, rollback 확인 ✅
    - [ ] (보강, 미룸) `assertNotNull(m1)`, **`createdAt` 검증**, `Member.builder()`, 미사용 import 정리
  - [x] `MemberService` (interface + impl), 생성자 주입, 이메일 중복 검사 ✅
  - [ ] `MemberController` + JSP(`member/signup.jsp`는 튜터 제공) → 브라우저 가입 → DB 확인 ← 진행 중
- [ ] 2-2 리뷰 CRUD + `@MybatisTest`
  - [ ] DAO 테스트 통과 후 실험: `@MapperScan` 주석 처리 → 두 DAO에 `@Mapper` → 같은 테스트 통과 확인 / (선택) 섞어 쓰면 `@Mapper` 쪽이 등록 안 되는 것 확인 → 하나로 정리

### 📌 다음 세션 시작점 (2026-11-14)

2-1 `MemberController` 작성부터. (2-2에서 `@MybatisTest`로 바꿀 때 주의: 슬라이스 테스트는 별도 `@Configuration`의 `@MapperScan`을 안 읽을 수 있음 → `@Mapper` 실험과 연결)
