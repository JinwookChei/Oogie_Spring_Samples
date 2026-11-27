package com.study.matzip.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.study.matzip.exception.ForbiddenException;
import com.study.matzip.exception.RecordNotFoundException;
import com.study.matzip.restcontroller.ReviewRestController;

import lombok.extern.slf4j.Slf4j;

// ============================================================================
// M8 예외 처리 다시 보기 — REST API(JSON)용 예외 처리 담당
// 학습 교안: guides/M8_예외처리.md · Notion [REST API] 12~14장
//
// 지금은 일부러 비워 둔 상태다. (애너테이션도 없음 → 아직 Advice가 아니다)
// 8-1에서 "Advice가 없으면 API 에러가 어떻게 나가는지" 먼저 관찰한 뒤, 8-6에서 직접 채운다.
// ============================================================================

// TODO: 8-6-1. 이 클래스를 restcontroller 패키지 담당 + JSON으로 응답하는 Advice로 만든다.
//              - 패키지는 문자열 대신 "클래스로" 지정하는 방법 (교안 13장 표: 왜 더 안전한가?)
//              - 화면용 Advice보다 먼저 적용되도록 우선순위 지정 (교안 14장)

@Slf4j
@RestControllerAdvice(basePackageClasses = ReviewRestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

	// 모든 메서드는 표준 오류 응답 객체(ProblemDetail)를 반환한다 → Content-Type: application/problem+json

	// TODO: 8-6-2. RecordNotFoundException → 404
	@ExceptionHandler(exception = RecordNotFoundException.class)
	public ProblemDetail handleRecordNotFoundException(RecordNotFoundException e) {
		log.warn("찾을 수 없음: {}", e.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}
	
	// TODO: 8-6-3. ForbiddenException → 403
	@ExceptionHandler(exception = ForbiddenException.class)
	public ProblemDetail handleForbiddenException(ForbiddenException e) {
		log.warn("권한 없음: {}", e.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
	}

	// TODO: 8-6-4. JSON이 아닌 형식으로 보낸 요청(Content-Type 불일치) → 415 (교안 12장 "어색한 점 찾기")
	//              8-6-5를 먼저 만들면 이 요청이 몇 번으로 바뀌는지 테스트로 확인해 보기
	//              → 실험 결과: 500. Resolved Exception = HttpMediaTypeNotSupportedException (Spring 표준 예외)
	//                Exception 핸들러가 삼키지 않도록 더 가까운 담당 메서드를 둔다 (교육생 요청으로 튜터 작성)
	@ExceptionHandler(exception = HttpMediaTypeNotSupportedException.class)
	public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
		// 클라이언트가 형식을 잘못 보낸 것 → 서버 장애가 아니므로 warn
		log.warn("지원하지 않는 Content-Type: {}", e.getContentType());
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
				"JSON(application/json) 형식으로 보내 주세요.");
	}

	// TODO: 8-6-5. 그 밖의 모든 예외 → 500, detail은 고정 문구 (내부 정보 숨김), 로그는 스택 트레이스까지
	@ExceptionHandler(exception = Exception.class)
	public ProblemDetail handleException(Exception e) {
		// 개발자용: error 레벨 + 마지막 인자로 예외 객체 e → 스택 트레이스까지 로그에 남는다 (튜터 작성)
		log.error("API 처리 중 예상하지 못한 오류: {}", e.getMessage(), e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
	}

	// TODO: 8-6-6. (선택) 커스텀 에러 코드: ProblemDetail에 "code" 필드를 추가해 보기 (교안 14장 "한 발 더")
}
