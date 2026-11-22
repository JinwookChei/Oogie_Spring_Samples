package com.study.matzip;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.study.matzip.controller.ReviewController;
import com.study.matzip.exception.ForbiddenException;
import com.study.matzip.model.dto.Member;
import com.study.matzip.model.service.ReviewService;

// 웹 계층만 로딩(web slice): ReviewController + @ControllerAdvice(MvcExceptionHandler) + WebMvcConfigurer(WebConfig)
// Service·DAO·DB는 로딩하지 않는다 → Tomcat 없이, DB 없이 Controller만 검증
@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

	@Autowired
	MockMvc mockMvc; // 가짜 브라우저: 요청을 DispatcherServlet(진짜)에 흘려 보낸다

	// @WebMvcTest는 Service를 올리지 않음 → 이게 없으면 ReviewController 생성 시 NoSuchBeanDefinitionException
	@MockitoBean
	ReviewService reviewService; // 대역 배우: 대사를 정하지 않으면 아무것도 안 함(null/0 반환)

	// ① 남의 리뷰 삭제 → Controller는 ForbiddenException을 잡지 않음 → MvcExceptionHandler → 403 + error/403
	@Test
	void delete_othersReview_forbidden() throws Exception {
		// Given: mno=2로 로그인한 사용자 + "rno=1을 mno=2가 지우려 하면 ForbiddenException" 대사
		Member loginUser = Member.builder().mno(2).email("other@matzip.com").name("다른사람").build();
		// deleteReview는 void → when().thenReturn() 대신 doThrow().when() 순서
		doThrow(new ForbiddenException("작성자가 아니어서 접근 할 수 없습니다."))
				.when(reviewService).deleteReview(1, 2);

		// When: 로그인한 척(sessionAttr) 하고 POST /review/1/delete
		mockMvc.perform(post("/review/1/delete").sessionAttr("loginUser", loginUser))
				.andDo(print()) // 요청·Handler·Model·View·응답을 Console에 출력 (디버깅용)
				// Then: Advice가 상태 코드와 화면을 바꿨는지
				.andExpect(status().isForbidden())
				.andExpect(view().name("error/403"))
				.andExpect(model().attribute("msg", "작성자가 아니어서 접근 할 수 없습니다."));
	}

	// ② 로그인 없이 작성 → 로그인 화면으로 redirect, Service는 한 번도 불리지 않아야 한다
	@Test
	void write_notLoggedIn_redirectToLogin() throws Exception {
		// Given: 세션에 loginUser 없음 (아무것도 하지 않음)

		// When
		mockMvc.perform(post("/review/write")
						.param("title", "제목")      // 파라미터는 항상 문자열
						.param("content", "내용")
						.param("rating", "5"))
				// Then
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/member/login"));

		// 로그인 검사에서 막혔으니 저장(Service 호출)까지 가면 안 된다
		verify(reviewService, never()).writeReview(any(), any());
	}
}
