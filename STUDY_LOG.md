# STUDY_LOG.md — 맛집 노트 학습 기록

실습하면서 직접 한 작업, 튜터와 대화하며 배운 개념, 내가 답한 질문과 피드백, 겪은 문제와 해결법을 챕터(마일스톤)별로 기록한다.
챕터가 끝나면 이 기록을 Notion에 옮긴다.

---

## M1. 프로젝트 세팅 — 🟡 진행 중

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
- 테스트 데이터를 스크립트에 넣을 때: 테이블을 새로 만들면 AUTO_INCREMENT가 1부터 다시 시작 → 리뷰의 `mno` 값 결정에 영향. 일부러 실패시키는 insert(rating 6)는 스크립트에 넣지 않음 (실행이 멈춤)

### 🙋 내 답변과 피드백

| 질문 | 내 답 | 피드백 |
|---|---|---|
| 어떤 의존성이 Tomcat을 데려오고, 왜 설치가 필요 없나? | Spring Web에 내장 Tomcat이 들어 있어 | ✅ 정답. 추가로 "Spring이 main()에서 Tomcat을 직접 만들어 켠다(주도권 역전)"까지 |
| 왜 MyBatis만 버전을 적어야 하나? | MyBatis는 Spring Boot에 없기 때문 | 🟡 방향은 맞음. 정확히는 "Boot의 **버전 관리 목록(BOM)**에 없기 때문". Lombok·MySQL도 Spring이 만든 건 아니지만 목록에 있어서 생략 가능 |
| 의존성만 추가했는데 실행이 왜 실패하나? | DataSource url이 없다고 나와 | ✅ 정답. 직접 MyBatis를 빼고 실행해 보는 실험까지 함 👏 |

### 🛠️ 트러블슈팅

**`Cannot access session scope outside of a scoping block` (pom.xml, Maven Configuration Problem)**
- 증상: STS에서만 뜨고, 명령줄 `mvnw compile`은 정상 → STS 내장 Maven(m2e) 쪽 문제
- 해결 과정에서 발견한 실수: 컴파일 플러그인을 두 번 선언 / `spring-boot-maven-plugin`을 실수로 지움 / `Active Maven Profiles`에 `pom.xml`이 들어 있음
- 해결: `pom.xml` 정리 + `Alt + F5` → **Force Update of Snapshots/Releases**

### ❓ 내가 한 질문

- 버전 목록이 `<parent>` 몇 줄밖에 안 보이는데 어디 있나? → 부모 pom에 있음 (상속)
- 의존성은 어떻게 추가하나? → `pom.xml`의 `<dependencies>` 안에 `<dependency>` 블록 추가 후 저장 + `Alt + F5`
- 라이브러리는 어디서 가져오나? / 인터넷 창고에서 가져오면 내 창고에도 자동 저장되나? → 그렇다 (Maven Central → `.m2`)
- DataSource가 뭔가? → 커넥션 풀의 대출 창구 인터페이스
- 강의 자료가 없으면 properties 키는 어디서 찾나? → STS 자동 완성 → 공식 문서 → Properties 클래스 (개념 8)
- rating을 1~5만 허용하려면? → `CHECK` 제약 조건 (개념 9)
- `schema.sql`은 어디에 저장하나? → `src/main/resources/scripts/` (개념 9)
- `CREATE DATABASE` 이름은 뭐로? → `application.properties` url의 DB 이름과 같게 (개념 7)
- url의 `serverTimezone=Asia/Seoul`은 뭔가? → 서버 시간대를 드라이버에 알려 주는 옵션 (개념 7)
- 그걸 왜 따로 쓰나? → 서버의 시간대 이름이 애매해서 표준 이름으로 확정 + Spring이 아니라 드라이버 옵션이라 url에 붙임 (개념 7)

### ⏭️ 남은 일

- [x] 1-1 `pom.xml` 의존성 추가 ✅
- [x] 1-2 `application.properties` 작성 → 실험 C ✅
- [ ] 1-3 DB와 테이블 만들기
  - [x] `schema.sql` 작성·저장, 재실행 가능, 컬럼 길이 수정 완료
  - [ ] **테스트 데이터 insert** (회원 1명 → 리뷰 1~2개, 하나는 image_name 없이) — 스크립트 마지막에 추가
  - [ ] **rating 6 insert 실험** (Workbench에서만, 스크립트엔 넣지 않음) → 에러 메시지 확인
- [ ] 1-4 메인 화면 Controller + `index.jsp` + DataSource 주입 테스트
- [ ] 미답 질문: 실험 C에서 `matzip` DB가 없는데도 앱이 뜬 이유를 한 문장으로

### 📌 다음 세션 시작점 (2026-11-12 종료)

1-3의 **테스트 데이터 insert**부터 이어서 한다. 그다음 rating 6 실험 → 1-4.
