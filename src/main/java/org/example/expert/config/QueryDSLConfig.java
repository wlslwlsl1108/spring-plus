package org.example.expert.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// [2-8] QueryDSL 적용 완료
// QueryDSL 전용 환경 설정 클래스
// 프로젝트 전역에서 쓸 수 있는 JPAQueryFactory 빈 등록
// 이게 없으면, 서비스/레포 에서 JPAQueryFactory 를 바로 주입 받을 수 없고,
// new 해서 직접 만들어야 함
// 유지보수성/확장성 위해 Config 로 Bean 등록해주는 게 표준!
@Configuration
public class QueryDSLConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}

