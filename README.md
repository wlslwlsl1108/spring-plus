# SPRING PLUS 개인 과제


---

## 목차
- [개요](#개요)
- [과제 목표](#과제-목표)
- [기술 스택](#기술-스택)
- [필수 과제](#필수-과제)
- [API 예시](#api-예시)
- [학습 키워드](#학습-키워드)
- [참고](#참고)

---

## 개요
- 실무에서 발생할 수 있는 문제(JPA 성능 저하, 보안, 테스트 신뢰성 등)를 직접 해결하여, 성능을 최적화하는 방법을 배워볼 수 있는 과제입니다.

---

## 과제 목표
- **JPA 심화**: 연관관계 매핑, JPQL, QueryDSL, Cascade, N+1 문제 해결 등 실무에서 자주 발생하는 문제를 다룸  
- **테스트 코드**: Spring Security 환경에서도 안정적으로 동작하는 테스트 코드 작성, `MockMvc`, `@MockBean` 활용  
- **성능 최적화**: 동적 쿼리(QueryDSL), N+1 문제 해결, 리팩토링을 통한 성능 개선  
- **실무 역량 강화**: 레거시 코드 개선, 보안 적용, 협업/리뷰 기반의 개발 습관 학습  

---

## 기술 스택
- **Backend**: Spring Boot 3.3.3, Java 17, JPA/Hibernate, QueryDSL, Validation
- **Auth**: Spring Security, JWT (Bearer Token), BCrypt
- **DB**: MySQL, H2 (Test)
- **Build**: Gradle 8.13 (`build.gradle`), `application.yml`
- **Docs**: Markdown (README)
- **Test**: JUnit5, Spring Boot Test, Spring Security Test

---

## 필수 과제

### Level 1
1. **코드 개선 퀴즈 – @Transactional**
   - 읽기 전용 트랜잭션 문제 해결 (`Connection is read-only` 오류 수정)

2. **코드 추가 퀴즈 – JWT**
   - `User` 엔티티에 `nickname` 컬럼 추가  
   - JWT에서 `nickname` 추출 가능하도록 구현

3. **코드 개선 퀴즈 – JPA**
   - `weather` 조건 검색 추가 (옵션 조건)
   - 수정일 기준 기간 검색 기능 추가 (옵션 조건)
   - JPQL 사용 (필요 시, 서비스 단에서 `if`문 사용 가능)

4. **테스트 코드 퀴즈 – 컨트롤러 테스트**
   - `todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다()` 테스트 수정 및 통과

5. **코드 개선 퀴즈 – AOP**
   - `AdminAccessLoggingAspect` 수정  
   - `changeUserRole()` 실행 전 AOP 동작하도록 개선

---

### Level 2
6. **JPA Cascade**
   - 할 일 생성 시, 작성자가 자동으로 담당자로 등록되도록 Cascade 활용

7. **N+1 문제 해결**
   - `CommentController.getComments()` API 호출 시 발생하는 N+1 문제 해결

8. **QueryDSL**
   - `findByIdWithUser` 메소드를 JPQL → QueryDSL로 전환  
   - N+1 문제 발생하지 않도록 주의

9. **Spring Security**
   - 기존 `Filter` & `Argument Resolver` 기반 인증 → **Spring Security + JWT 기반 인증**으로 변경  
   - 접근 권한 및 유저 권한 로직 유지

---

## API 예시
```http
### 회원가입
POST http://localhost:8080/auth/signup
Content-Type: application/json

{
  "email": "Test1@test.com",
  "password": "1234",
  "userRole": "ROLE_USER",
  "nickname": "첫번째"
}


### 로그인 ( + 토큰 생성 )
POST http://localhost:8080/auth/signin
Content-Type: application/json

{
  "email": "Test1@test.com",
  "password": "1234"
}


### todo 생성
POST http://localhost:8080/todos
Content-Type: application/json
Authorization: Bearer {{bearerToken}}

{
  "title": "테스트4",
  "contents": "Spring"
}


### todo 목록 전체 조회
GET http://localhost:8080/todos
Authorization: Bearer {{bearerToken}}


### [1-3] todo 목록 조회 (날씨)
GET http://localhost:8080/todos?weather=
Authorization: Bearer {{bearerToken}}


### [1-3] todo 목록 조회 (기간_수정일 기준)
GET http://localhost:8080/todos?startDate=2025-09-01T00:00:00&endDate=2025-09-23T23:59:59
Authorization: Bearer {{bearerToken}}


### 특정 todo 조회
GET http://localhost:8080/todos/2
Authorization: Bearer {{bearerToken}}


### 댓글 생성
POST http://localhost:8080/todos/1/comments
Content-Type: application/json
Authorization: Bearer {{bearerToken}}

{
  "contents": "시큐리티 테스트2"
}


### 댓글 조회
GET http://localhost:8080/todos/1/comments
Authorization: Bearer {{bearerToken}}


### 특정 사용자(유저) 조회
GET http://localhost:8080/users/{userId}
Authorization: Bearer {{bearerToken}}


### 비밀번호 변경
PUT http://localhost:8080/users
Content-Type: application/json
Authorization: Bearer {{bearerToken}}

{
  "oldPassword": "1234",
  "newPassword": "AAA"
}


### 역할(role) 변경
PATCH http://localhost:8080/admin/users/4
Content-Type: application/json
Authorization: Bearer {{bearerToken}}

{
  "role": "ROLE_USER"
}


### 특정 todo에 매니저 지정
POST http://localhost:8080/todos/{todoId}/managers
Content-Type: application/json
Authorization: Bearer {{bearerToken}}

{
  "managerUserId": 1
}


### 특정 todo 매니저 조회
GET http://localhost:8080/todos/{todoId}/managers
Authorization: Bearer {{bearerToken}}


### 특정 todo 매니저 삭제
DELETE http://localhost:8080/todos/{todoId}/managers/{managerId}
Authorization: Bearer {{bearerToken}}
```
---

## 학습 키워드
- JPA 심화 (JPQL, QueryDSL, Cascade, N+1 문제)  
- 테스트 코드 (Spring Security Test, MockMvc, 단위/통합 테스트)  
- 성능 최적화 (쿼리 최적화, 리팩토링)  
- 보안 (Spring Security, JWT)  

---

## 참고
- 과제에 대한 개념 및 변경 사항은 아래 블로그 참고해주시면 됩니다.
- [Level 1.1](첨부 예정)
- [Level 1.2](첨부 예정)
- [Level 1.3](첨부 예정)
- [Level 1.4](첨부 예정)
- [Level 1.5](첨부 예정)
- [Level 2.6](첨부 예정)
- [Level 2.7](첨부 예정)
- [Level 2.8](첨부 예정)
- [Level 2.9](첨부 예정)
