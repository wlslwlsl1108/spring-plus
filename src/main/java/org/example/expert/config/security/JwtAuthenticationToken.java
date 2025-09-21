package org.example.expert.config.security;

import org.example.expert.domain.auth.dto.AuthUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;

// Spring Security -> 인증 객체는 모두 Authentication 인터페시으 구현 필요!
// AbstractAuthenticationToken (Authentication 의 추상클래스) : 권한/인증 여부 관리 로직 들어있음
// -> 우리는 필요한 메서드 구현만 하면 됨
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
// " JWT 인증 끝난 사용자 정보를
//    Spring Security가 이해할 수 있는 인증 객체(Authentication)로 바꿔주는 "어댑터" 역할 "

    // AuthUser : 우리가 만든 DTO
    // 토큰 검증 후, 추출한 사용자 정보 여기 담아둠
    // authUser = " 인증된 사용자 정보 "
    private final AuthUser authUser;

    // 생성자에서 Authentication 객체 완성하는 과정
    public JwtAuthenticationToken(AuthUser authUser) {
        // 부모 AbstractAuthenticationToken에 권한(Authorities) 넘겨줌
        // Security 가 hasRole, hasAuthority 검사할 때 이 값 사용
        // AuthUser 내부에서 List.of(new SimpleGrantedAuthority(role.name()))로 권한 보관하고 있음
        super(authUser.getAuthorities());
        // 실제 사용자 정보 저장
        this.authUser = authUser;
        // Security는 Authentication 객체를 "이미 인증 완료된 사용자" 로 간주
        setAuthenticated(true);
    }

    @Override
    // "비밀번호 같은 자격 증명 정보" 리턴
    // JWT 인증에서는 토큰 검증 끝났으니 추가 자격 증명 필요X -> null 반환
    public Object getCredentials() {
        return null;
    }

    // "현재 인증된 사용자(Principal)" 리턴
    // 우리는 authUser 그대로 반환
    // -> 이걸 컨트롤러에서 @AuthenticationPrincipal AuthUser authUser 이런식으로 꺼낼 수 있음
    @Override
    public Object getPrincipal() {
        return authUser;
    }
}

/*
    [ JwtAuthenticationToken 주요 목적 ]

    1. JWT 검증 끝난 사용자 정보를 Security 표준 객체로 변환
        - Security는 Authentication 객체만 인식
           -> 우리가 AuthUser 감싸서 넣어주는 것

    2. 권한(Authorities) 보관
        - super(authUser.getAuthorities())를 통해
           Role/Authority 정보를 넘겨 Security의 권한 체크에 활용

    3. 컨트롤러/서비스에서 사용자 정보 쉽게 접근
        - SecurityContextHolder.getContext().getAuthentication().getPrincipal()
            -> AuthUser 반환

 */
