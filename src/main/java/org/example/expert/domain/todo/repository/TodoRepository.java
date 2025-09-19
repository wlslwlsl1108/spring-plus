package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    // [1-3] 할 일 검색 시 weather 조건으로 검색 가능
    @Query("SELECT t FROM Todo t WHERE t.weather = :weather")
    Page<Todo> findByWeather(@Param("weather") String weather, Pageable pageable);

    // [1-3] 할 일 검색 시 수정일 기준으로 기간 검색 가능
    // - 기준 필드 : modifiedAt (수정일 기준) -> JPQL 쿼리 작성 필요
    // - 검색 파라미터 : startDate, endDate (쿼리 실행 시 전달) -> 기간 검색이므로 필요!
    // URL 쿼리 파라미터 이름을 내가 설정 가능!
    // 쿼리 안 필드(modifiedAt)만 바꾸면 생성일 기준으로 검색도 가능.
    @Query("SELECT t FROM Todo t WHERE t.modifiedAt BETWEEN :startDate AND :endDate")
    Page<Todo> findByModifiedAt(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
