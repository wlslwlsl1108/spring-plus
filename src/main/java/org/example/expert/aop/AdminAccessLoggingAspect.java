package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect    // AOP 선언! -> 여러 Advice / Pointcut 담는 모듈
@Component
@RequiredArgsConstructor
public class AdminAccessLoggingAspect {

    private final HttpServletRequest request;

    // [1-5] AOP 수정 완료
    // - @After -> @Before : changeUserRole() 메서드 실행 전 동작 완료
    // - 메서드명 수정 (AOP 의도에 맞도록 Before로 수정)
    @Before("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    // @Before : 어드바이스 -> AOP 의 핵심 역할!
    // execution : Pointcut -> 어떤 메서드 실행 시점에 적용할지 지정하는 부분
    public void logBeforeChangeUserRole(JoinPoint joinPoint) {
    // -> 실제 실행될 부가 기능(로깅 로직)
    // JoinPoint : 현재 실행 중인 메서드의 시그니처/인자 정보 접근 가능
        String userId = String.valueOf(request.getAttribute("userId"));
        String requestUrl = request.getRequestURI();
        LocalDateTime requestTime = LocalDateTime.now();

        log.info("Admin Access Log - User ID: {}, Request Time: {}, Request URL: {}, Method: {}",
                userId, requestTime, requestUrl, joinPoint.getSignature().getName());
    }
}
