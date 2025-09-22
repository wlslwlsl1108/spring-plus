package org.example.expert.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.entity.Timestamped;
import org.example.expert.domain.user.enums.UserRole;
// [2-9] Spring Security 연동
import org.springframework.security.core.GrantedAuthority;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    // [1-2] User 정보에 nickname 추가 (중복 가능) + 생성자 추가
    private String nickname;

    public User(String email, String password, UserRole userRole, String nickname) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.nickname = nickname;
    }

    private User(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }

    // [2-9] AuthUser 가 authorities 기반 구조로 바뀌었기 때문에
    // GrantedAuthority → String → UserRole 변환 과정 거침
    public static User fromAuthUser(AuthUser authUser) {
        UserRole userRole = authUser.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(UserRole::of)
                .orElse(UserRole.ROLE_USER);
        return new User(authUser.getId(), authUser.getEmail(), userRole);
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void updateRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
