package org.example.expert.domain.todo.controller;

import org.example.expert.config.security.JwtAuthenticationFilter;
import org.example.expert.config.security.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    // 참고) @WithMockUser : Spring Security 테스트용 어노테이션
    // -> 가짜 사용자(mock user) 만들어서 테스트 환경에서 인증된 사용자처럼 동작하게 해줌
    // -> @WebMvcTest, @SpringBootTest 같은 통합 테스트에서 컨트롤러 인증/인가 로직 검증 시 사용
    void todo_단건_조회에_성공한다() throws Exception {
        // given
        long todoId = 1L;
        String title = "title";
        AuthUser authUser = new AuthUser(1L, "email", UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail());
        TodoResponse response = new TodoResponse(
                todoId,
                title,
                "contents",
                "Sunny",
                userResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // when
        when(todoService.getTodo(todoId)).thenReturn(response);

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value(title));
    }

    // [1-4] 컨트롤러 테스트의 이해 완료
    // 현재 400에러가 발생하는데, 기대 조건에 200 OK 를 주고있음
    // 에러 발생과 동일하게 변경
    @Test
    void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
        // given
        // 테스트 시나리오 준비 : todoId 임의로 준비
        long todoId = 1L;

        // when
        // InvalidRequestException 예외 던짐 -> 실제 400 에러 발생
        when(todoService.getTodo(todoId))
                .thenThrow(new InvalidRequestException("Todo not found"));

        // then
        // 문제) 기대 조건을 OK(200) 으로 주었으므로 테스트가 정상작동하지 않음 (기대 조건 != 실제 발생 에러)
        // 해결) 실제 발생하는 에러와 동일하게 기대 조건은 BAD_REQUEST(400)로 변경해줌
        // /todos/{todoId} 경로로 GET 요청 보냄 (시뮬레이션)  -> 기대 조건!
        mockMvc.perform(get("/todos/{todoId}", todoId))
                // 응답 HTTP 상태코드가 400 Bad Request 이어야 함 (검증)
                .andExpect(status().isBadRequest())
                // "status" : Bad Request (검증)
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                // "code" : 400 (검증)
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                // "message" : "Todo not found" (검증)
                .andExpect(jsonPath("$.message").value("Todo not found"));
    }
}
