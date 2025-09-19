package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GetTodoQueryDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Optional<Todo> findByIdWithUser(Long todoId) {

        QUser user = QUser.user;
        QTodo todo = QTodo.todo;

        Todo result = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}

/*
    [ 별도 QueryDSL 레포를 둔 이유 ]

    1. 인터페이스에서 DSL 쿼리 작성 불가
    2. 서비스 계층은 비즈니즈 로직만 담당 -> 쿼리 작성 X

    => 별도 QueryDSL 레포(class) 생성하여 책임 분리


 */