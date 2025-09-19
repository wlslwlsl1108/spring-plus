package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.GetTodoQueryDSLRepository;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
// [1-1] " readonly = true " 가 전체 메서드에 적용되면 안됨 (무분별 사용 금지)
// -> 읽기 전용에만 사용!
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;
    // [2-8] QueryDSL 적용 완료 -> QueryDSL 레포지토리 의존성 주입
    private final GetTodoQueryDSLRepository getTodoQueryDSLRepository;

    // todo 생성
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    // todo 조회
    // [1-3] weather 검색 기능 추가 (if 문 추가)
    // [1-3] 수정일 기준 기간 검색 기능 추가 (else if 추가)
    @Transactional(readOnly = true)
    public Page<TodoResponse> getTodos(
            String weather,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Todo> todos;

        // URL 쿼리 파라미터 검증 //
        // 1. URL 에서 weather 검색 시
        if (weather != null && !weather.isEmpty()) {
            todos = todoRepository.findByWeather(weather, pageable);
        // 2. URL 에서 기간 검색 시 (startDate ~ endDate)
        } else if (startDate != null && endDate != null) {
            todos = todoRepository.findByModifiedAt(startDate, endDate, pageable);
        // 3. 전체 조회
        //    즉, 날씨 조건도 기간 조건도 작성하지 않았을 경우!
        } else {
            todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);
        }

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    // [2-8] QueryDSL 적용 완료
    // 기존 TodoRepository(JPQL)가 아닌 getTodoQueryDSLRepository(QueryDSL) 사용
    // 특정 todo 조회
    @Transactional(readOnly = true)
    public TodoResponse getTodo(long todoId) {

        Todo todo = getTodoQueryDSLRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }
}
