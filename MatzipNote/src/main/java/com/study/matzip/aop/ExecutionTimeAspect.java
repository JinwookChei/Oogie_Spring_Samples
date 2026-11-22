package com.study.matzip.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect     // 하나 이상의 advice를 포함
@Component  // 빈으로 등록해야 Spring AOP가 이 Aspect를 적용한다
@Slf4j
public class ExecutionTimeAspect {

	// pointcut: service 패키지 바로 안의 모든 클래스(*)의 모든 메서드(*), 파라미터 무관(..)
	@Around("execution(* com.study.matzip.model.service.*.*(..))")
	public Object checkExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		// finally에서도 읽어야 하므로 try 밖에 선언 (지역 변수 → 호출마다 따로 생겨 스레드끼리 섞이지 않음)
		long start = System.currentTimeMillis();
		try {
			// 진짜 Service 메서드 실행. return해도 메서드를 빠져나가기 직전에 finally가 먼저 실행된다
			return joinPoint.proceed();
		} finally {
			// 성공이든 예외든 항상 실행 (= @After). catch로 예외를 잡지 않으므로 예외는 그대로 호출한 쪽으로 전파된다
			long end = System.currentTimeMillis();
			log.debug("{} 실행 시간 = {}ms", joinPoint.getSignature().toShortString(), end - start);
		}
	}
}
