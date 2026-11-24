package com.study.matzip.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring MVC 기본 설정에 "덧붙이기" 위한 설정 클래스 (필요한 메서드만 오버라이드)
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final LoginCheckInterceptor loginChecker;
	
	// LocalImageStorage와 같은 키 → 저장하는 폴더 = 보여 주는 폴더
	@Value("${app.upload.path}")
	private String uploadPath;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Windows 역슬래시 문제를 피하려고 문자열 이어 붙이기 대신 Path → URI (file:///C:/Users/.../)
		String location = Path.of(uploadPath).toUri().toString();
		// toUri()는 폴더가 실제로 있을 때만 끝에 / 를 붙인다 → 없으면 직접 붙임 (끝의 / 가 없으면 파일을 못 찾음)
		if (!location.endsWith("/")) {
			location += "/";
		}
		log.info("업로드 폴더 매핑: /upload/** → {}", location);

		registry.addResourceHandler("/upload/**")   // 화면에서 쓰는 URL
				.addResourceLocations(location);     // 실제 디스크 위치
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loginChecker).addPathPatterns(
				"/review/write", "/review/*/edit", "/review/*/delete"
				);
	}
}
