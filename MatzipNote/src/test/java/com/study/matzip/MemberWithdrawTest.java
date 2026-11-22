package com.study.matzip;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.study.matzip.model.dao.MemberDao;
import com.study.matzip.model.dao.ReviewDao;
import com.study.matzip.model.dto.Member;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.service.MemberService;

import lombok.extern.slf4j.Slf4j;

// @Transactional을 붙이지 않는다.
// 테스트에 @Transactional이 있으면 Service도 테스트의 트랜잭션에 참여하고(REQUIRED),
// rollback은 테스트가 끝날 때 한 번만 일어난다. 그러면 테스트 메서드 안에서는
// Service의 @Transactional 유무와 상관없이 "리뷰가 지워진 상태"가 보여서 차이를 확인할 수 없다.
// → 여기서는 진짜로 commit되게 두고, @AfterEach에서 직접 정리한다.
@Slf4j
@SpringBootTest
public class MemberWithdrawTest {

	@Autowired
	MemberService memberService;

	@Autowired
	MemberDao memberDao;

	@Autowired
	ReviewDao reviewDao;

	Member m;
	Review review1;
	Review review2;

	// 준비: 테스트용 회원 1명 + 그 회원의 리뷰 2개 (실제 데이터에 기대지 않는다)
	@BeforeEach
	void testBefore() {
		m = Member.builder()
				.email("aaa@gmail.com")
				.name("바나나")
				.password("1234")
				.build();
		memberDao.insert(m); // useGeneratedKeys 덕분에 insert 후 m.mno가 채워진다

		review1 = Review.builder()
				.mno(m.getMno()) // review.mno는 NOT NULL + FK → 작성자를 꼭 지정
				.content("테스트1 중")
				.rating(3)
				.title("테스트1 제목")
				.build();

		review2 = Review.builder()
				.mno(m.getMno())
				.content("테스트2 중")
				.rating(5)
				.title("테스트2 제목")
				.build();

		reviewDao.insert(review1);
		reviewDao.insert(review2);
	}

	// 정상 탈퇴: 예외 없이 끝나면 commit → 회원과 그 회원의 리뷰가 모두 사라진다
	@Test
	void deleteAccount_success() {
		memberService.deleteAccount(m.getMno());

		assertNull(memberDao.selectByEmail(m.getEmail()), "회원이 지워져야 한다");
		assertNull(reviewDao.selectByRno(review1.getRno()), "리뷰1이 지워져야 한다");
		assertNull(reviewDao.selectByRno(review2.getRno()), "리뷰2가 지워져야 한다");
		// 이후 @AfterEach가 이미 없는 데이터를 또 지우지만 행 수 0으로 끝날 뿐 예외는 없다 (삭제의 멱등성)
	}

	// 검증: 탈퇴 도중 예외 → 업무가 실패했으니 DB는 탈퇴 전과 같아야 한다 (원자성)
	// 4-2(🔴 @Transactional 없음) / 4-3(🟢 @Transactional) 실험 기록.
	// Service의 deleteAccount에 리뷰 삭제와 회원 삭제 사이 `int tmp = 1 / 0;`을 넣었을 때만 의미가 있다.
	@Disabled("실험용: Service에 1/0을 다시 넣고 @Disabled를 지우면 재현된다")
	@Test
	void deleteAccount_failInMiddle_rollbackAll() {
		assertThrows(ArithmeticException.class, () -> memberService.deleteAccount(m.getMno()));

		assertNotNull(memberDao.selectByEmail(m.getEmail()), "회원이 남아 있어야 한다");
		assertNotNull(reviewDao.selectByRno(review1.getRno()), "리뷰1이 남아 있어야 한다");
		assertNotNull(reviewDao.selectByRno(review2.getRno()), "리뷰2가 남아 있어야 한다");
	}

	// 정리 전용 (assert 없음): Service가 아니라 DAO를 직접, FK 때문에 리뷰 → 회원 순서
	// 테스트가 실패해도 실행되므로 테스트 회원이 DB에 남지 않는다
	@AfterEach
	void testAfter() {
		reviewDao.deleteByMno(m.getMno());
		memberDao.delete(m.getMno());
	}
}
