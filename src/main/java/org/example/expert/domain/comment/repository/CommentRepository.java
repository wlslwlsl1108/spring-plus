package org.example.expert.domain.comment.repository;

import org.example.expert.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // [2-7] N+1 문제 해결 완료!
    // DISTINCT : LEFT JOIN 시, 한 유저가 여러개의 댓글과 조인되면서 댓글이 중복으로 나올 수 있기 때문에 사용
    // FETCH JOIN : 연관관계 맺어진 테이블의 데이터를 한 번의 쿼리로 가져오기 때문에 사용
    // @Query("SELECT c FROM Comment c JOIN c.user WHERE c.todo.id = :todoId") => N+1 문제 발생
    @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.todo.id = :todoId")
    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);
}
