package org.example.expert.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// 로깅 위한 Lombok 어노테이션 (log.info, log.error..)
@Slf4j
// 스프링 빈으로 등록 -> SecurityConfig 에서 필터 체인에 주입
@Component
// final 필드 자동 주입 생성자 생성
@RequiredArgsConstructor
// Spring Security 필터 -> 요청마다 한 번만 실행
// OncePerRequestFilter : HTTP 요청 1개당 딱 1번 실행되는 필터 -> " 중복 실행 방지 "
public class JwtAuthenticationFilter extends OncePerRequestFilter {
// " JWT 기반 권한 체크를 가능하게 해주는 필터 "
// 1. JWT 검증 (유효한지, 만료되었는지)
// 2. SecurityContextHolder 에 인증 정보 저장
// -> Spring Security 가 인증된 사용자로 인식 가능

    // JWT 파싱/검증 유틸
    private final JwtUtil jwtUtil;
    // JSON 변환 (에러 응답.. )
    private final ObjectMapper objectMapper;

    @Override
    // Spring Security가 각 요청 처리 시 반드시 호출하는 메서드!
    protected void doFilterInternal(
            // 들어온 요청 객체 : 헤더, URI, 파라미터 등 확인 가능
            HttpServletRequest httpRequest,
            // 나가는 응답 객체 : 에러코드, JSON 메세지 등 보낼 수 있음
            @NonNull HttpServletResponse httpResponse,
            // 다음 필터 실행 연결
            // chain : 필터 체인 연결하는 역할
            // chain.doFilter(request, response) 호출해야 다음 필터 or 컨트롤러로 진행됨
            @NonNull FilterChain chain
            ) throws ServletException, IOException {
        // 요청 헤더에서 Authorization 헤더 가져옴
        // JWT 기반 인증 -> Authorization : Bearer <jwt토큰> 일반적으로 이 형태로 전달됨
        String authorizationHeader = httpRequest.getHeader("Authorization");

        // Authorization 헤더 없거나 "Bearer " 로 시작하지 않으면 JWT 인증 없다고 판단 -> 다음 필터로 그냥 넘김
        // 즉, /open, /auth/login 같은 불필요 엔드포인트에서도 정상 작동 가능!
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        // "Bearer..."에서 실제 JWT 토큰만 추출
        // ex. "Bearer abc.def.ghi"  ->  "abc.def.ghi"
        String jwt = jwtUtil.substringToken(authorizationHeader);

        // JWT 검증 + SecurityContext 에 인증 객체 생성 로직 실행
        if (!processAuthentication(jwt, httpRequest, httpResponse)) {
            // 실패하면 에러 응답 전송 -> 체인 중단
            return;
        }

        // 성공하면 다음 필터로 진행
        chain.doFilter(httpRequest, httpResponse);
    }

    private boolean processAuthentication(
            String jwt,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            // JWT 해독하여 Claims(정보, 페이로드) 추출
            // sub : userId
            // email : 이메일
            // userRole : 권한
            // exp : 만료 시간
            Claims claims = jwtUtil.extractClaims(jwt);

            // 현재 SecurityContext 에 인증이 없을 때만 Authentication 설정
            // SecurityContext 는 Spring Security 의 핵심
            // 현재 스레드(ThreadLocal)에 저장된 인증 정보 꺼낼 수 o
            // 인증 안된 상태면, setAuthentication(claims) 호출 -> 인증 객체 생성 / 저장
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(claims);
            }
            return true;
        } catch (ExpiredJwtException e) {
            // JWT 만료될 경우
            log.info("JWT 만료: userId={}, URI={}",
                    e.getClaims().getSubject(),
                    request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException e) {
            // 시그니처 위조, 형식 오류, 지원되지 않는 토큰 등.. 일 경우
            log.error("JWT 검증 실패 [{}]: URI={]",
                    e.getClass().getSimpleName(),
                    request.getRequestURI(),
                    e);
            sendErrorResponse(response, HttpStatus.BAD_REQUEST, "인증이 필요합니다.");
        } catch (Exception e) {
            // 그 외 예상치 못한 오류 경우
            log.error("예상치 못한 오류: URI={}",
                    request.getRequestURI(),
                    e);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 오류가 발생했습니다.");
        }
        // 실패 시 false 반환
        return false;
    }

    private void setAuthentication(Claims claims) {
        // JWT Claims 에서 사용자 식별 정보 추출
        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));

        // 추출한 정보 기반으로 AuthUser 객체 생성
        AuthUser authUser = new AuthUser(userId, email, userRole);
        // Spring Security 인증 객체 생성 (JwtAuthenticationToken)
        Authentication authenticationToken = new JwtAuthenticationToken(authUser);
        // SecurityContextHolder 에 인증 저장 -> 이후 컨트롤러 @AuthenticationPrincipal 등에서 사용 가능
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    // JWT 검증 실패 시 JSON 형식의 에러 응답 내려줌
    private void sendErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        // 에러 응답 JSON 포맷으로 내려주기
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.name());
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
/*
    [ JwtAuthenticationFilter 주요 목적 ]

    1. JWT 토큰 검증
        - 요청 들어올 때마다 Authorization 헤더 확인 + JWT 유효성 검사
        - 위조, 만료된 토큰 -> 바로 차단

    2. Spring Security 인증 컨텍스트 설정
        - 검증된 토큰에서 사용자 정보 꺼내 Authentication 객체 생성
        - SecurityContextHolder 에 넣어,
          Spring Security 가 인증된 사용자로 인식하게 함
        - 이후 권한 체크에 활용됨 -> hasRole, hasAuthority

    3. Stateless 인증 (세션 x)
        - 서버에 세션 저장 x -> 매 요청마다 토큰 검증
        - 확장성 증가

-----------------------------------------------------------------------------------

    [ JwtAuthenticationFilter 필요한 이유 ]

    1. Spring Security 는 세션 기반 로그인(formLogin) 사용
        -> 서버가 세션 들고 있다가 요청마다 세션 검증

    2. JWT 인증 방식 = 서버가 세션 저장 X
        -> 대신 매 요청마다 토큰 검증

    3. 위 역할을 해주는 것이 JwtAuthenticationFilter

-----------------------------------------------------------------------------------

    [ JwtAuthenticationFilter 없다면? ]

    1. 토큰 있어도 SecurityContext 비어있음
        -> 권한 체크 전부 실패 (@PreAuthorize, hasRole())

    2. 사실상 JWT 인증 무용지물
        -> JWT 발급받아도 전혀 사용 불가

-----------------------------------------------------------------------------------

    [ SecurityContextHolder 에 왜 넣어야 하나? ]

    - Spring Security 는 모든 권한 검사 로직을
      SecurityContextHolder 안의 Authentication 객체를 기준으로 함
          => 이 작업 안해주면 권한이 있어도 익명 사용자(anonymous) 취급

    (흐름)
       1. 컨트롤러에 요청 도착
       2. Security 필터 체인 실행
       3. JwtAuthenticationFilter가 토큰 보고 Authentication 객체 생성
       4. SecurityContextHolder.getContext().setAuthentication(authentication)
       5. 이제 Security가 이 인증 정보 꺼내 권한 검사 (hasRole, permitAll, authenticated) 수행

-----------------------------------------------------------------------------------

    [ JwtFilter(서블릿 필터)  vs  JwtAuthenticationFilter(시큐리티 필터) ]
        => 둘 다 JWT 다루지만, 목적/실행 위치 다음

    [ JwtFilter(서블릿 필터) ]

        => 그냥 "요청에 붙어 있는 토큰 검사하는 필터"

        - javax.servlet.Filter 구현체
        - DispatcherServlet 실행 전후 아무데서나 걸 수 있음
        - 단순히 request.setAttribute("userId", ...) 같은 값 주입 정도 가능
        - Spring Security 권한 시스템과는 연결 x


    [ JwtAuthenticationFilter(시큐리티 필터) ]

        => "JWT를 Spring Security 권한 시스템과 연결해주는 다리 역할 필터"

        - OncePerRequestFilter (Security Filter Chain 안에 포함됨)
        - Security 흐름에 공식적으로 참여
        - JWT 검증 후 SecurityContext 에 인증 주입
        - 권한 체크(@PreAuthorize, hasAuthority)가 정상 동작하게 해줌


-----------------------------------------------------------------------------------

 */
