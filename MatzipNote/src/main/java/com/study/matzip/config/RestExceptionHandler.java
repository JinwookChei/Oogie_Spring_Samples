package com.study.matzip.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study.matzip.restcontroller.ReviewRestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackageClasses = ReviewRestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

	// ① 없는 리뷰: Service가 던진 IllegalArgumentException("찾을 수 없는 리뷰 입니다.") → 404 JSON
	//    ProblemDetail의 status가 그대로 응답 상태 코드가 되므로 @ResponseStatus는 필요 없다
	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail noReview(IllegalArgumentException e) { // Spring이 던져진 예외 객체를 넣어 준다
		log.warn("API 요청 실패: {}", e.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}

	// ② 그 밖의 모든 예외(최후의 보루) → 500 JSON
	//    e.getMessage()에는 SQL·클래스명 같은 내부 정보가 있을 수 있어 고정 문구만 보내고, 원인은 로그로만
	@ExceptionHandler(Exception.class)
	public ProblemDetail unexpectedError(Exception e) {
		log.error("[RestController에서 처리되지 않은 오류]", e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
	}
}
