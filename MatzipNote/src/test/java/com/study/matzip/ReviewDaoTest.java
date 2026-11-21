package com.study.matzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.study.matzip.model.dao.MemberDao;
import com.study.matzip.model.dao.ReviewDao;
import com.study.matzip.model.dto.Member;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@Transactional // 각 테스트가 끝나면 rollback → @BeforeEach에서 넣은 회원까지 DB에 남지 않는다
public class ReviewDaoTest {

	// 테스트 클래스는 생성자 주입 대신 필드에 @Autowired (그래서 @RequiredArgsConstructor는 필요 없음)
	@Autowired
	private ReviewDao reviewDao;

	@Autowired
	private MemberDao memberDao;

	private int mno; // 리뷰의 FK로 쓸 회원 번호

	// 매 테스트 전에 실행. @Transactional 테스트의 같은 트랜잭션 안에서 돌기 때문에 이것도 rollback된다.
	// 기존 DB 데이터(kim@matzip.com 등)에 기대지 않고, 테스트가 쓸 회원을 직접 만든다.
	@BeforeEach
	public void setUp() {
		Member member = Member.builder()
				.email("review-test@test.com")
				.password("1111")
				.name("리뷰테스터")
				.build();
		memberDao.insert(member);
		mno = member.getMno(); // useGeneratedKeys + keyProperty="mno" 덕분에 채워진 PK
	}

	@Test
	public void insertTest() {
		Review review = Review.builder()
				.mno(mno)
				.title("테스트 제목")
				.content("테스트 본문")
				.rating(5)
				.build(); // rno·createdAt은 DB가 만든다 → 넣지 않는다

		int result = reviewDao.insert(review);

		assertEquals(1, result);            // 반환값 = 영향받은 행 수 (PK가 아님!)
		assertTrue(review.getRno() > 0);    // PK는 넘겨준 객체 안에 채워진다 (keyProperty="rno")
		log.debug("insert된 rno: {}", review.getRno());
	}

	/**
	 * 회귀 테스트: 키워드 조건의 괄호 (content OR title) 가 빠지면 실패해야 한다.
	 *
	 * 괄호 없음: rating >= 4 AND content LIKE ... OR title LIKE ...
	 *         = (rating >= 4 AND content LIKE ...) OR title LIKE ...   ← 함정 리뷰가 섞임
	 * 괄호 있음: rating >= 4 AND (content LIKE ... OR title LIKE ...)  ← 함정 리뷰가 빠짐
	 */
	@Test
	public void searchTest() {
		// 함정 리뷰: 별점 4 미만(A 거짓) + 제목에만 "너무"(C 참)
		Review trap = Review.builder()
				.mno(mno)
				.title("너무 짜요")
				.content("다시는 안 가요")
				.rating(1)
				.build();
		reviewDao.insert(trap);

		ReviewSearch rs = ReviewSearch.builder()
				.keyword("너무")
				.minRating(4)
				.build();

		List<Review> results = reviewDao.search(rs);
		results.forEach(r -> log.debug("검색 결과 - rno: {}, rating: {}, title: {}", r.getRno(), r.getRating(), r.getTitle()));

		// ① 함정 리뷰가 결과에 없어야 한다
		assertFalse(results.stream().anyMatch(r -> r.getRno() == trap.getRno()),
				"별점 1점 리뷰가 섞여 나옴 → 키워드 조건의 괄호를 확인하세요");

		// ② 나온 리뷰는 모두 별점 4 이상이어야 한다
		assertTrue(results.stream().allMatch(r -> r.getRating() >= 4));
	}

	// <where> + <if> 의 다른 경우: 조건을 하나도 안 주면 WHERE 자체가 빠진다 (DEBUG 로그의 Preparing 확인)
	@Test
	public void searchWithoutConditionTest() {
		Review review = Review.builder()
				.mno(mno)
				.title("아무 제목")
				.content("아무 본문")
				.rating(2)
				.build();
		reviewDao.insert(review);

		List<Review> results = reviewDao.search(new ReviewSearch()); // keyword·minRating 모두 null

		assertNotNull(results);
		// 조건이 없으니 방금 넣은 리뷰도 반드시 나와야 한다 (기존 DB 데이터에 기대지 않는 검증)
		assertTrue(results.stream().anyMatch(r -> r.getRno() == review.getRno()));
	}

	// 아래 테스트들이 공통으로 쓰는 준비: 리뷰를 하나 넣고, PK가 채워진 객체를 돌려준다
	private Review insertReview() {
		Review review = Review.builder()
				.mno(mno)
				.title("원래 제목")
				.content("원래 본문")
				.rating(2)
				.build();
		reviewDao.insert(review);
		return review;
	}

	// JOIN + resultMap의 <association>이 writer(Member)를 채우는지 확인
	@Test
	public void selectDetailTest() {
		Review inserted = insertReview();

		Review detail = reviewDao.selectDetail(inserted.getRno());

		assertNotNull(detail);
		assertEquals(inserted.getRno(), detail.getRno());
		assertNotNull(detail.getWriter(), "writer가 null → resultMap의 association을 확인하세요");
		assertEquals(mno, detail.getWriter().getMno());       // writer_mno 별칭 → writer.mno
		assertEquals("리뷰테스터", detail.getWriter().getName()); // @BeforeEach에서 만든 회원
	}

	@Test
	public void updateTest() {
		Review review = insertReview();

		// 원래 값과 "확실히 다른" 값으로 바꾼다 (같은 값이면 update가 안 돼도 테스트가 통과해 버림)
		review.setTitle("바뀐 제목");
		review.setContent("바뀐 본문");
		review.setRating(5);

		int result = reviewDao.update(review);

		assertEquals(1, result); // 영향받은 행 수

		// 들고 있는 객체가 아니라 DB에서 다시 꺼낸 값으로 확인해야 "DB에 반영됐는지"를 알 수 있다
		Review updated = reviewDao.selectByRno(review.getRno());
		assertEquals("바뀐 제목", updated.getTitle());
		assertEquals("바뀐 본문", updated.getContent());
		assertEquals(5, updated.getRating());
	}

	@Test
	public void deleteTest() {
		Review review = insertReview();

		int result = reviewDao.delete(review.getRno());

		assertEquals(1, result);
		assertNull(reviewDao.selectByRno(review.getRno())); // 다시 조회하면 없어야 한다
	}

	// 없는 번호: 예외가 아니라 "영향받은 행 수 0"으로 나타난다 → Service가 먼저 조회해서 막는 이유
	@Test
	public void updateAndDeleteNotFoundTest() {
		Review ghost = Review.builder()
				.rno(-1)
				.title("없는 리뷰")
				.content("없는 리뷰")
				.rating(3)
				.build();

		assertEquals(0, reviewDao.update(ghost));
		assertEquals(0, reviewDao.delete(-1));
		assertNull(reviewDao.selectDetail(-1));
	}
}
