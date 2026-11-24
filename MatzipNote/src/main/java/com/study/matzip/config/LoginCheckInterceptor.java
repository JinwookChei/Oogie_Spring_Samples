package com.study.matzip.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession(false);
		if(session == null || session.getAttribute("loginUser") == null) {
			log.info("로그인정보가 존재하지 않습니다 - {}", request.getRequestURI());
			response.sendRedirect(request.getContextPath() + "/member/login");
			return false; 
		}
		return true;
	}
}
