package com.study.matzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.study.matzip.model.dto.Member;
import com.study.matzip.model.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@Transactional
public class MemberServiceTest {

	// 로그인 실패 메시지 (없는 이메일 / 틀린 비밀번호 모두 같아야 함 → 계정 열거 방지)
	private static final String LOGIN_FAIL_MESSAGE = "이메일 또는 비밀번호가 틀렸습니다.";

	@Autowired
	MemberService ms;

	private Member setMem;   // 모든 테스트가 쓰는 "이미 가입된 회원"
	private int setMno;      // 그 회원의 가입 번호

	// 매 테스트 전에 실행. @Transactional 테스트와 같은 트랜잭션이라 끝나면 함께 rollback된다.
	// 기존 DB 데이터(kim@matzip.com 등)에 기대지 않고 필요한 회원을 테스트가 직접 만든다.
	// 준비 단계라 assert는 두지 않는다 (가입 검증은 signUp_success의 몫)
	@BeforeEach
	public void setUp() {
		setMem = Member.builder()
				.email("new@matzip.com")
				.password("1111")
				.name("새회원")
				.build();

		setMno = ms.signUp(setMem);
	}

	@Test
	void signUp_success() {
		Member member = Member.builder().email("signUp@matzip.com").password("2222").name("새회원").build();

		// when: try 없이 그냥 호출
		// 예외가 나면 JUnit이 알아서 실패(빨간 막대)로 처리 → 성공 케이스에 try-catch는 필요 없다
		int mno = ms.signUp(member);

		// then: Service가 돌려준 새 회원 번호 확인
		// @Transactional 덕분에 테스트가 끝나면 rollback → 몇 번 실행해도 결과가 같다
		log.debug("가입된 회원: {}", member);
		assertNotEquals(0, mno);
		assertEquals(mno, member.getMno()); // 반환값 = keyProperty로 채워진 객체의 mno
	}

	@Test
	void signUp_duplicateEmail() {
		// given: setUp()에서 이미 가입한 회원과 "같은 이메일"로 한 번 더 가입
		//  → 중복 상황을 테스트 안에서 직접 만든다 (DB에 kim@matzip.com이 있는지에 기대지 않음)
		Member member = Member.builder().email(setMem.getEmail()).password("1234").name("중복회원").build();

		// when & then: 예외가 "나야" 통과
		// () -> ... : 람다. "이 코드를 실행하면"이라는 뜻으로 assertThrows에 코드 덩어리를 넘긴다
		// 예외가 안 나면(= 중복 검사가 빠지면) 빨간 막대 → 이 테스트가 중복 검사 규칙을 지켜 준다
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> ms.signUp(member));

		// assertThrows는 던져진 예외를 돌려준다 → 메시지까지 확인 가능
		log.debug("예외 메시지: {}", e.getMessage());
		assertTrue(e.getMessage().contains("이미 가입된 이메일"));
	}

	@Test
	void login_success() {
		// when: setUp()에서 가입한 회원의 이메일·비밀번호로 로그인 (값을 다시 쓰지 않고 setMem에서 꺼냄)
		Member mem = ms.login(setMem.getEmail(), setMem.getPassword());

		// then
		assertNotNull(mem);                     // ① null 검사가 맨 앞: null이면 아래 줄에서 NPE 대신 여기서 친절하게 실패
		assertEquals(setMno, mem.getMno());     // ② assertEquals(기대값, 실제값) 순서
		assertNull(mem.getPassword());          // ③ 세션에 넣을 객체라 비밀번호는 비워져 있어야 함
	}

	@Test
	void login_wrongEmail() {
		// try-catch로 잡으면 예외가 나든 안 나든 항상 초록 → assertThrows로 "예외가 나야 통과"를 표현
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ms.login("unsigned@matzip.com", setMem.getPassword()));

		assertEquals(LOGIN_FAIL_MESSAGE, e.getMessage());
	}

	@Test
	void login_wrongPassword() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ms.login(setMem.getEmail(), "9999"));

		// login_wrongEmail과 "같은 메시지"인지 확인 → 실패 이유를 밖에서 구분할 수 없어야 한다
		assertEquals(LOGIN_FAIL_MESSAGE, e.getMessage());
	}
}
