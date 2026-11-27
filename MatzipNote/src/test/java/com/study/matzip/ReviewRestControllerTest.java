package com.study.matzip;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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

import com.study.matzip.exception.ForbiddenException;
import com.study.matzip.exception.RecordNotFoundException;
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
		// 8-4-5: 진짜 Service가 "없음"을 RecordNotFoundException으로 던지게 바뀜 → 가짜 Service도 맞춘다 (교육생 요청으로 튜터 수정)
		when(reviewService.selectDetail(999))
				.thenThrow(new RecordNotFoundException("찾을 수 없는 리뷰 입니다."));

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

	// ======================================================================
	// M8 목표 테스트 (튜터 세팅, 2026-11-24)
	// 처음에는 ②⑤⑥이 빨간 불이다 (Advice를 비워 두었기 때문). 8-4·8-6을 마치면 초록 불이 되어야 한다.
	// ②는 8-4에서 커스텀 예외를 만든 뒤, 가짜 Service가 던지는 예외를 바꿔 줘야 한다 (교육생 수정).
	// ======================================================================

	// ⑤ 예상 못 한 오류 → 500 ProblemDetail, detail에 내부 정보(SQL 등)가 들어가면 안 된다 (교안 14장 ⚠️)
	@Test
	void detail_unexpectedError_500() throws Exception {
		when(reviewService.selectDetail(1))
				.thenThrow(new RuntimeException("SQLSyntaxErrorException: Table 'matzip.review' doesn't exist"));

		mockMvc.perform(get("/api/review/1"))
				.andDo(print())
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.detail", not(containsString("SQL"))));
	}

	// ⑥ JSON이 아니라 form 형식으로 작성 요청 → 415 ProblemDetail (교안 12장 "어색한 점 찾기")
	//    힌트: "모든 예외 → 500" 핸들러만 있으면 이 테스트는 몇 번을 받을까?
	@Test
	void write_formContentType_415() throws Exception {
		mockMvc.perform(post("/api/review")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("title", "제목")
						.param("content", "내용")
						.param("rating", "5"))
				.andDo(print())
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(415));

		verify(reviewService, never()).writeReview(any(), any());
	}

	// ⑦ TODO: 8-6-3. (교육생 작성) 남의 리뷰 관련 API에서 ForbiddenException → 403 ProblemDetail
	//    지금 API에는 수정·삭제가 없다 → 상세(selectDetail)가 ForbiddenException을 던지게 가짜 Service를 설정해서 확인해도 된다
	//    (교육생 요청으로 튜터 작성. 테스트 ②를 본뜸)
	@Test
	void detail_forbidden_403() throws Exception {
		// Given: 가짜 Service가 "남의 리뷰"라고 거절한다 (M10에서 PUT·DELETE를 만들면 실제로 이 길을 탄다)
		when(reviewService.selectDetail(1))
				.thenThrow(new ForbiddenException("작성자가 아니어서 접근 할 수 없습니다."));

		// When: GET /api/review/1
		mockMvc.perform(get("/api/review/1"))
				.andDo(print())
				// Then: RestExceptionHandler.handleForbiddenException → 403 ProblemDetail(JSON)
				.andExpect(status().isForbidden())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.detail").value("작성자가 아니어서 접근 할 수 없습니다."));
	}
}
