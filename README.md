# AI Chatbot API

Kotlin + Spring Boot 기반의 AI 챗봇 REST API 서비스입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Kotlin 1.9, Java 21 |
| Framework | Spring Boot 3.5 |
| Database | H2 (In-Memory) |
| Auth | JWT (jjwt 0.12) + BCrypt |
| AI | OpenAI API (OkHttp) |
| Streaming | SSE (Server-Sent Events) |

---

## 실행 방법

### 사전 조건

- Java 21 이상
- OpenAI API Key

### 환경변수

| 변수 | 필수 여부 | 기본값 |
|------|-----------|--------|
| `OPENAI_API_KEY` | **필수** | — |
| `JWT_SECRET` | 선택 | 내장 기본값 (개발용) |
| `ADMIN_EMAIL` | 선택 | `admin@example.com` |
| `ADMIN_PASSWORD` | 선택 | `admin1234` |
| `ADMIN_NAME` | 선택 | `admin` |

### 실행

```bash
OPENAI_API_KEY=sk-... ./gradlew bootRun
```

서버 기동 시 관리자 계정이 자동 생성됩니다.

| 항목 | 기본값 |
|------|--------|
| Email | `admin@example.com` |
| Password | `admin1234` |

환경변수(`ADMIN_EMAIL`, `ADMIN_PASSWORD`)로 변경 가능합니다.

- API Base URL: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa` / Password: (비어있음)

---

## API 명세

모든 요청/응답은 JSON이며, 인증이 필요한 엔드포인트는 `Authorization: Bearer <token>` 헤더가 필요합니다.

### 인증

| Method | Path | 인증 | 설명 |
|--------|------|------|------|
| POST | `/api/auth/register` | 불필요 | 회원가입 |
| POST | `/api/auth/login` | 불필요 | 로그인 (JWT 발급) |

**회원가입 요청**
```json
POST /api/auth/register
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

**로그인 요청**
```json
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}
```

---

### 대화 (Chat)

| Method | Path | 인증 | 권한 | 설명 |
|--------|------|------|------|------|
| POST | `/api/chats` | 필요 | member / admin | 대화 생성 |
| GET | `/api/chats` | 필요 | member / admin | 대화 목록 조회 |
| DELETE | `/api/threads/{threadId}` | 필요 | member / admin | 스레드 삭제 |

**대화 생성 요청**
```json
POST /api/chats
{
  "question": "안녕하세요",
  "isStreaming": false,
  "model": "gpt-5.4-mini"
}
```

- `isStreaming: true` 설정 시 `text/event-stream` 형식으로 응답합니다.
- `model` 기본값은 `gpt-5.4-mini`입니다.

**대화 목록 조회**
```
GET /api/chats?page=0&size=10&sort=desc
```

- 스레드 단위로 그룹화된 대화 목록을 응답합니다.
- 일반 사용자는 본인의 대화만, 관리자는 전체 대화를 조회합니다.

**스레드 생성 기준**

마지막 대화로부터 30분 이내에 질문하면 기존 스레드를 유지하고, 30분을 초과하거나 최초 질문이면 새 스레드를 생성합니다.

---

### 피드백 (Feedback)

| Method | Path | 인증 | 권한 | 설명 |
|--------|------|------|------|------|
| POST | `/api/feedbacks` | 필요 | member / admin | 피드백 생성 |
| GET | `/api/feedbacks` | 필요 | member / admin | 피드백 목록 조회 |
| PATCH | `/api/feedbacks/{feedbackId}/status` | 필요 | **admin only** | 피드백 상태 변경 |

**피드백 생성 요청**
```json
POST /api/feedbacks
{
  "chatId": 1,
  "positive": true
}
```

- 각 사용자는 하나의 대화에 하나의 피드백만 생성할 수 있습니다.
- 일반 사용자는 본인이 생성한 대화에만 피드백을 남길 수 있습니다.

**피드백 목록 조회**
```
GET /api/feedbacks?positive=true&page=0&size=10&sort=desc
```

- `positive` 파라미터로 긍정/부정 필터링이 가능합니다.
- 일반 사용자는 본인의 피드백만, 관리자는 전체 피드백을 조회합니다.

**피드백 상태 변경 (관리자 전용)**
```json
PATCH /api/feedbacks/1/status
{
  "status": "resolved"
}
```

- 상태값: `pending`, `resolved`

---

## 과제 분석 및 설계 결정

### 과제를 어떻게 분석하셨나요?

이 과제의 핵심 목표는 *"API를 통해 AI를 활용할 수 있다"* 는 것을 시연하는 데 있다고 판단했습니다.

기능 단위로 의존성을 분석해 **인증 → 채팅 → 피드백** 순으로 구현 순서를 결정했습니다. 채팅 기능은 인증에 의존하고, 피드백 기능은 채팅에 의존하는 구조이기 때문에 하위 의존성이 먼저 완성되어야 상위 기능을 안정적으로 구현할 수 있기 때문입니다.

분석 및 보고 기능(활동 기록, CSV 보고서)은 제한된 3시간 내에서 상대적으로 우선순위를 낮게 설정했습니다. 시연의 목표 달성에 직접적으로 기여하는 핵심 기능들을 먼저 완성하는 것이 적절하다고 판단했습니다.

---

### 과제 진행에 AI를 어떻게 활용하셨나요? 어떤 어려움이 있었나요?

Claude Code를 **페어 프로그래밍 방식**으로 활용했습니다.

단순히 코드를 생성시키는 것이 아니라, AI가 모호한 요구사항을 추측으로 처리하지 않도록 명시적으로 통제하는 것이 핵심이라고 판단했습니다. 구체적으로는 다음 두 가지 원칙을 적용했습니다.

1. **모호성 통제**: 요구사항을 제시할 때 불명확한 부분이 있으면 추측 없이 반드시 질문을 통해 명확화한 뒤 설계를 진행하도록 지시했습니다.

2. **근거 기반 코드 리뷰**: 설계 플랜과 구현 결과를 직접 검토하며, 판단 근거가 불분명한 결정에 대해서는 추가 질문을 제시해 의도를 명확하게 만들어갔습니다. (하단 부록 참고)

어려움이 있었던 부분은 AI가 기술적 선택의 근거를 충분히 제시하지 않은 채 구현을 진행하는 경우였습니다. 이를 계속 되물어 수정하는 과정이 필요했습니다.

---

### 구현하기 가장 어려웠던 기능

**SSE(Server-Sent Events) 스트리밍 응답**

`isStreaming: true` 요청 시 OpenAI의 스트리밍 응답을 SSE로 클라이언트에 실시간 전달하는 기능입니다.

WebSocket과 SSE 중 SSE를 선택한 이유는, OpenAI API 자체가 SSE 방식으로 스트리밍 응답을 제공하고 있어 클라이언트-서버-OpenAI 모두 단방향 스트림으로 일관되게 처리할 수 있기 때문입니다. 채팅의 경우 서버→클라이언트 방향의 단방향 스트리밍으로 충분해 WebSocket의 양방향 연결이 오버스펙이라고 판단했습니다.

직접 SSE를 구현해본 경험이 없었기 때문에 `SseEmitter` 생명주기 관리와 스트리밍 완료 시점에 DB 저장을 처리하는 부분에서 트러블슈팅에 어려움이 있었습니다.

---

## 부록 - AI 협업 시 제시한 질문 목록

설계 및 구현 과정에서 AI의 판단 근거를 명확히 하기 위해 직접 제시한 질문들입니다.

- `JwtAuthenticationFilter`에서 response 401 응답 body를 문자열로만 적용할 수밖에 없어서 사용한 것인지?
- `spring-security-crypto` 의존성은 왜 필요한지? Spring Security 전체 의존성은 사용하고 싶지 않은 상황에서의 근거 설명 요청
- 테스트 메서드에서 백틱 기반 표현(`fun \`test case\`()`)이 Kotlin 표준인지? vs `@DisplayName`에 한국어 설명을 작성하는 방식 중 어느 것을 선택해야 하는지
- DTO 패키지 구조를 `request` / `response`로 분리하도록 요청
- `isStreaming=true` 기능에서 WebSocket과 SSE 중 SSE를 선택한 근거 설명 요청
- OkHttp를 사용한 이유? Spring 기본 기능(`RestTemplate`, `WebClient`)으로 대체할 수 없는 근거 설명 요청
- `CreateChatRequest`에서 model 값을 하드코딩한 근거 설명 요청
- `ThreadRepository`에서 비관적 락을 사용한 근거 설명 요청
- SSE 관련 로직을 Service에서 직접 처리하지 않고 별도 패키지(`sse`)로 분리하는 방향 제안 → **채택**
- `ChatController`의 인증/인가 공통 로직을 커스텀 어노테이션(`@Auth`, `@Admin`) + AOP로 추출하는 방향 제안 → **`@Auth`, `@Admin`으로 채택**
