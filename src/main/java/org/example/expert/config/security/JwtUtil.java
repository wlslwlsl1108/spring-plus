package org.example.expert.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

// 클래스 내부에서 사용할 로거 자동생성 + 로그 카테고리명(JwtUtil) 지정
@Slf4j(topic = "JwtUtil")
// 스프링 빈으로 등록 -> 어디서든 @Autowired 또는 생성자 주입으로 사용 가능
@Component
public class JwtUtil {
// " JWT 생성 / 파싱 / 검증 전담 유틸리티 "

    // JWT는 보통 Authorization: Bearer <토큰> 형식으로 전달됨
    // 이 상수로 "Bearer " 접두사를 관리
    private static final String BEARER_PREFIX = "Bearer ";
    // 토큰 만료 시간 = 1시간(60분)으로 설정
    // 이후 setExpiration()에 사용됨
    private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

    // application.properties나 application.yml 에서 jwt.secret.key 값 읽어옴
    // -> JWT 서명 검증에 사용할 "비밀키"
    @Value("${jwt.secret.key}")
    private String secretKey;
    // 실제 JWT 서명/검증에 쓰일 Key 객체
    // -> init()에서 secretKey로 초기화됨
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // 스프링이 빈 생성 + 의존성 주입 끝난 뒤 실행
    // secretKey(Base64 인코딩된 문자열)를 디코딩 -> Key 객체 생성
    // -> 이후 signWith(key, algorithm) 에서 사용됨
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // JWT 토큰 생성 메서드
    // 파라미터 : userId, email, userRole, nickname -> Claims에 저장
    public String createToken(Long userId, String email, UserRole userRole, String nickname) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        // JWT 표준 Claim인 sub에 userId 저장
                        .setSubject(String.valueOf(userId))
                        // 커스텀 Claim 추가
                        .claim("email", email)          // 이메일 저장
                        .claim("userRole", userRole)    // 권한 저장
                        .claim("nickname", nickname)    // 닉네임 저장
                        // 만료시간 설정
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        // 발급시간 기록
                        .setIssuedAt(date)
                        // HS256 알고리즘으로 서명
                        .signWith(key, signatureAlgorithm)
                        // 문자열 토큰 완성 -> 앞에 "Bearer " 붙여 최종 반환
                        .compact();
    }

    // Authorization 헤더에서 토큰 꺼낼 때 "Bearer " 제거
    // "Bearer abc.def.ghi" → "abc.def.ghi"
    // 형식이 잘못되면 ServerException 예외 던짐
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new ServerException("Not Found Token");
    }

    // 토슨 파싱 후 Claims(내용부: payload) 추출
    // 내부적으로 서명 검증까지 함께 수행
    // 실패 시 io.jsonwebtoken 예외 발생 (ExpiredJwtException 등)
    // 성송하면 {sub, email, userRole, nickname, exp, iat} 들어있는 Claims 반환
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

/*
    [ JwtUtil 주요 목적 ]

    1. JWT 생성
        - 사용자 정보 담은 Token 발급 -> createToken

    2. JWT 파싱
        - 헤더에서 "Bearer " 제거 -> substringToken

    3. JWT 검증 / 정보 추출
        - 토큰 유효성 검증 + 사용자 정보 꺼내기 -> extractClaims

 */