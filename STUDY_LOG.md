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
| C | 있음 | 있음 | (진행 예정) |

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

### ⏭️ 남은 일

- [ ] MyBatis 다시 넣고 `application.properties` 작성 → 실험 C
- [ ] 1-3 DB와 테이블 만들기
- [ ] 1-4 메인 화면 + DataSource 테스트
