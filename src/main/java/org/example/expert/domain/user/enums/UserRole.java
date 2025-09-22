package org.example.expert.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    // [2-9] Spring Security는 "ROLE_" prefix 를 요구하므로 변경
    // 권한 문자열은 반드시 "ROLE_"로 시작
    ROLE_USER(Authority.USER),
    ROLE_ADMIN(Authority.ADMIN);

    private final String userRole;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 UerRole"));
    }

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}

/*
        [ UserRole 변경 이유 ]

        1. Spring Security는 "ROLE_" prefix 를 요구

             - hasRole("USER") 같은 체크를 하면,
                내부적으로는 "ROLE_USER" 라는 문자열을 찾음
             - 즉, 권한 문자열은 반드시 "ROLE_"로 시작!

        2. 기존 방식(ADMIN, USER)은 prefix 없기 때문에
            @PreAuthorize("hasRole('USER')") 같은 어노테이션 동작 x

             => Security 가 기대하는 문자열로 매칭시켜주기 위해!!
 */