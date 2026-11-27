package com.study.matzip.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.study.matzip.exception.ForbiddenException;
import com.study.matzip.exception.RecordNotFoundException;

import lombok.extern.slf4j.Slf4j;

// ============================================================================
// M8 예외 처리 다시 보기 — 화면(JSP)용 예외 처리 담당
// 학습 교안: guides/M8_예외처리.md · Notion [Spring MVC pt.2] Part 3 (13~19장)
//
// 지금은 일부러 비워 둔 상태다. (애너테이션도 없음 → 아직 Advice가 아니다)
// 8-1에서 "Advice가 없으면 어떻게 되는지" 먼저 관찰한 뒤, 8-3에서 직접 채운다.
// 예전(M5) 코드는 git에 남아 있다 → 다 작성한 뒤에만 비교해 보기 (교안 8-7)
// ============================================================================

// TODO: 8-3-1. 이 클래스를 "controller 패키지의 Controller만" 담당하는 화면용 Advice로 만든다.
//              (범위를 안 정하면 어떤 일이 생기는지 교안 17장 ⚠️ 박스를 먼저 읽기)
@ControllerAdvice(basePackages="com.study.matzip.controller")
@Slf4j
public class MvcExceptionHandler {
	
//	│ 8-3-2        │ handleForbidden │ ForbiddenException e      │
//	├──────────────┼─────────────────┼───────────────────────────┤
//	│ 8-3-3        │ handleException │ 가장 넓은 예외 타입 e     │
//	├──────────────┼─────────────────┼───────────────────────────┤
//	│ 8-4-3 (나중) │ handleNotFound  │ RecordNotFoundException e │
	
	
	// TODO: 8-3-2. ForbiddenException → 상태 코드 403, View error/403, 사용자용 메시지(msg) 전달, warn 로그
	@ExceptionHandler(exception = ForbiddenException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ModelAndView handleForbidden(ForbiddenException e) {
		log.warn("충돌: {}", e.getMessage());
		ModelAndView mav = new ModelAndView("error/403");
		mav.addObject("msg", e.getMessage());
		return mav;
	}
	
	
	// TODO: 8-4-3. (커스텀 예외를 만든 뒤) RecordNotFoundException → 상태 코드 404, View error/404, msg 전달
	@ExceptionHandler(exception = RecordNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ModelAndView handleNotFound(RecordNotFoundException e) {
		log.warn("찾을 수 없음: {}", e.getMessage());
		ModelAndView mav = new ModelAndView("error/404");
		mav.addObject("msg", e.getMessage());
		return mav;
	}
	
	
	// TODO: 8-3-3. 그 밖의 모든 예외(최후의 보루) → 상태 코드 500, View error/500
	//              - 화면의 msg는 고정 문구 (e.getMessage()를 화면에 넘기지 않는다 — 왜?)
	//              - 원인 추적용 로그는 스택 트레이스까지
	@ExceptionHandler(exception = Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView handleException(Exception e) {
		// 개발자용: error 레벨 + 마지막 인자로 예외 객체 e → 스택 트레이스까지 로그에 남는다
		log.error("예상하지 못한 오류: {}", e.getMessage(), e);
		ModelAndView mav = new ModelAndView("error/500");
		// 사용자용: 고정 문구 (e.getMessage()에는 SQL 같은 내부 정보가 들어 있을 수 있다)
		mav.addObject("msg", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
		return mav;
	}
	
	// TODO: 8-3-4. 실험: 상태 코드 지정을 잠깐 빼고 테스트를 돌려 보기 → 몇 번이 나오나? (교안 Part 3 Q5)
	
}
