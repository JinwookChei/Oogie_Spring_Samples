package com.study.matzip;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.study.matzip.model.dto.Member;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.service.ReviewService;
import com.study.matzip.restcontroller.ReviewRestController;

// 웹 계층만: ReviewRestController + RestExceptionHandler(@RestControllerAdvice)
// Service는 가짜(@MockitoBean) → DB 없이 "JSON 응답"만 검증
@WebMvcTest(ReviewRestController.class)
public class ReviewRestControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	ReviewService reviewService;

	// ① 상세 조회 → 200 + 리뷰 JSON
	@Test
	void detail_ok() throws Exception {
		// Given: selectDetail(1)이 불리면 이 리뷰를 돌려줘
		Review review = Review.builder().rno(1).mno(1).title("국밥집").content("맛있어요").rating(5).build();
		when(reviewService.selectDetail(1)).thenReturn(review); // 값을 반환하는 메서드 → when().thenReturn()

		mockMvc.perform(get("/api/review/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)) // HTML이 아니라 JSON
				.andExpect(jsonPath("$.rno").value(1))                         // $ = JSON의 맨 바깥
				.andExpect(jsonPath("$.title").value("국밥집"))
				.andExpect(jsonPath("$.rating").value(5));
	}

	// ② 없는 리뷰 → RestExceptionHandler가 404 ProblemDetail(JSON)로 바꾼다 (500.jsp가 아님)
	@Test
	void detail_notFound() throws Exception {
		when(reviewService.selectDetail(999))
				.thenThrow(new IllegalArgumentException("찾을 수 없는 리뷰 입니다."));

		mockMvc.perform(get("/api/review/999"))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON)) // ProblemDetail의 Content-Type
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("찾을 수 없는 리뷰 입니다."));
	}

	// ③ 작성: JSON 본문(@RequestBody) + 로그인 세션 → 201, 작성자는 JSON이 아니라 세션의 mno
	@Test
	void write_created() throws Exception {
		Member loginUser = Member.builder().mno(2).email("user@matzip.com").name("작성자").build();
		// 클라이언트가 보낸 JSON에 mno=99를 넣어도(사칭 시도) 세션의 mno=2가 들어가야 한다
		String json = """
				{"mno": 99, "title": "새 맛집", "content": "또 갈 거예요", "rating": 4}
				""";

		mockMvc.perform(post("/api/review")
						.contentType(MediaType.APPLICATION_JSON) // Content-Type과 @RequestBody는 짝
						.content(json)
						.sessionAttr("loginUser", loginUser))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("새 맛집"))
				.andExpect(jsonPath("$.mno").value(2));
	}

	// ④ 로그인 없이 작성 → 401, Service는 불리지 않아야 한다
	@Test
	void write_notLoggedIn_unauthorized() throws Exception {
		mockMvc.perform(post("/api/review")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\": \"제목\", \"content\": \"내용\", \"rating\": 5}"))
				.andExpect(status().isUnauthorized());

		verify(reviewService, never()).writeReview(any(), any());
	}
}
