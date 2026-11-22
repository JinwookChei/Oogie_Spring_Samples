package com.study.matzip.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.study.matzip.exception.ForbiddenException;

import lombok.extern.slf4j.Slf4j;

// controller 패키지의 Controller가 "실행되다가" 던진 예외만 담당한다.
// 404(없는 URL)·405는 Controller가 선택되기 전에 실패 → 여기 안 옴 → BasicErrorController → error/404.jsp
@ControllerAdvice(basePackages = "com.study.matzip.controller")
@Slf4j
public class MvcExceptionHandler {

	// ① 권한 없음 (남의 리뷰 수정·삭제): 더 구체적인 타입이라 아래 Exception 핸들러보다 우선 선택된다
	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN) // 없으면 에러 화면인데 200 OK로 나간다
	public ModelAndView handleForbidden(ForbiddenException e) { // Spring이 던져진 예외 객체를 넣어 준다
		log.warn("권한 없는 요청: {}", e.getMessage()); // 예상한 상황 → warn, 스택 트레이스는 생략

		ModelAndView mav = new ModelAndView("error/403"); // /WEB-INF/views/error/403.jsp
		mav.addObject("msg", e.getMessage());              // ForbiddenException의 메시지는 사용자용 문구
		return mav;
	}

	// ② 그 밖의 모든 예외 (최후의 보루): 예상하지 못한 오류
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView handleException(Exception e) {
		log.error("[Controller에서 처리되지 않은 오류]", e); // 원인 추적용: 스택 트레이스까지 서버 로그에만

		ModelAndView mav = new ModelAndView("error/500");
		// e.getMessage()는 SQL·클래스명 같은 내부 정보일 수 있어 화면에 넘기지 않는다
		mav.addObject("msg", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
		return mav;
	}
}
