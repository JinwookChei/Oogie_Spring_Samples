package com.study.matzip.config;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 1. 어떤 요청이 들어왔는지 (9-4에서 로그를 요청별로 구분하는 기준)
		log.debug("요청 시작: {} {}", request.getMethod(), request.getRequestURI());
		request.setAttribute("startTime", System.currentTimeMillis());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
		long startTime = (long)request.getAttribute("startTime");
		long endTime = System.currentTimeMillis();
		// 2. 처리 시간은 정상 요청에도 매번 남는 기록용 로그 → error가 아니라 debug
		log.debug("요청 끝: {} {} - 수행시간: {}ms", request.getMethod(), request.getRequestURI(), endTime - startTime);

		// 3. 아무도 처리하지 못한 예외일 때만 ex가 채워진다 (Advice가 처리하면 null)
		//    → 서버 고장이니 error, 마지막 인자에 ex를 넘겨 스택 트레이스까지 남긴다
		if (ex != null) {
			log.error("요청 처리 중 예외: {} {}", request.getMethod(), request.getRequestURI(), ex);
		}
	}
}
