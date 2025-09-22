package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    // [2-9] Spring Security = enum(UserRole)으로 권한 체크 x
    // 컬렉션 사용 시 여러 권한 체크 가능
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUser(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        // [2-9] UserRole → SimpleGrantedAuthority 변환
        this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
    }
}

/*
      [ this.authorities = List.of(new SimpleGrantedAuthority(userRole.name())); ]

      - SimpleGrantedAuthority : Spring Security 에서 가장 기본적인 권한(Authority) 표현 클래스
      - SecurityContext에 저장되는 Authentication = 내부적으로 getAuthorities() 구현 필요
           -> 이 때, 반환되는 건 Collection<? extends GrantedAuthority> 타입! (enum 아님)

         => 그러므로 UserRole → SimpleGrantedAuthority 로 변환해줘야 권한 체크 가능!
 */