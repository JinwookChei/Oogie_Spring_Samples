package com.study.matzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	
	@Autowired
	MemberService ms;
	
	 @Test
	    void signUp_success() {
	        Member member = Member.builder()
	                .email("new@matzip.com")
	                .password("1111")
	                .name("새회원")
	                .build();

	        // when: try 없이 그냥 호출
	        //  예외가 나면 JUnit이 알아서 실패(빨간 막대)로 처리 → 성공 케이스에 try-catch는 필요 없다
	        int mno = ms.signUp(member);

	        // then: Service가 돌려준 새 회원 번호 확인
	        //  @Transactional 덕분에 테스트가 끝나면 rollback → 몇 번 실행해도 결과가 같다
	        log.debug("가입된 회원: {}", member);
	        assertNotEquals(0, mno);
	        assertEquals(mno, member.getMno());   // 반환값 = keyProperty로 채워진 객체의 mno
	    }

	    @Test
	    void signUp_duplicateEmail() {
	        // given: schema.sql로 이미 들어가 있는 회원의 이메일
	        Member member = Member.builder()
	                .email("kim@matzip.com")
	                .password("1234")
	                .name("김맛집")
	                .build();

	        // when & then: 예외가 "나야" 통과
	        //  () -> ...  : 람다. "이 코드를 실행하면"이라는 뜻으로 assertThrows에 코드 덩어리를 넘긴다
	        //  예외가 안 나면(= 중복 검사가 빠지면) 빨간 막대 → 이 테스트가 중복 검사 규칙을 지켜 준다
	        IllegalArgumentException e = assertThrows(
	                IllegalArgumentException.class,
	                () -> ms.signUp(member));

	        // assertThrows는 던져진 예외를 돌려준다 → 메시지까지 확인 가능
	        log.debug("예외 메시지: {}", e.getMessage());
	        assertTrue(e.getMessage().contains("이미 가입된 이메일"));
	    }
}
