package org.example.expert.config.security;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

// 스프링 설정 클래스임을 표시
@Configuration
// final 필드 자동 생성자로 주입
@RequiredArgsConstructor
// Spring Security 웹 보안 활성화
@EnableWebSecurity
// @Secured, @PreAuthorize 같은 어노테이션 기반 접근 제어 활성화
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    // JWT 인증 필터 (직접 구현한 커스텀 필터 주입)
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 비밀번호 암호화를 위한 Bean 등록 -> BCrypt 알고리즘 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security 핵심 보안 설정 (필터 체인)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 비활성화 -> JWT 방식은 세션 없으므로 필요X
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 생성하지 않고, STATELESS(무상태) 정책 적용 => JWT 사용을 위함
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT 필터를 SecurityContextHolderAwareRequestFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, SecurityContextHolderAwareRequestFilter.class)
                // JWT 사용 시 불필요한 기능들 비활성화 //
                .formLogin(AbstractHttpConfigurer::disable)  // [SSR] 서버가 로그인 HTML 폼 렌더링
                .anonymous(AbstractHttpConfigurer::disable)  // 미인증 사용자를 익명 처리
                .httpBasic(AbstractHttpConfigurer::disable)  // [SSR] 인증 팝업
                .logout(AbstractHttpConfigurer::disable)     // [SSR] 서버가 세션 무효화 후 리다이렉트
                .rememberMe(AbstractHttpConfigurer::disable) // 서버가 쿠키 발급하여 자동 로그인

                .authorizeHttpRequests(auth -> auth
                        // '/auth/**' 경로는 모두 접근 허용 (회원가입, 로그인 ..)
                        .requestMatchers("/auth/**").permitAll()
                        // '/test' 는 ADMIN 만 허용
                        .requestMatchers("/test").hasAuthority(UserRole.Authority.ADMIN)
                        // '/open' 은 아무나 접근 가능
                        .requestMatchers("/open").permitAll()
                        // 다른 요청들은 authentication 필요 -> 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )
                // SecurityFilterChain 객체 빌드
                .build();

    }
}

