# spring-cgv-23rd
## CEOS 23기 백엔드 스터디 - CGV 클론 코딩 프로젝트

<details>
<summary><strong>ERD</strong></summary>

<br />

과제 요구사항은 다음과 같습니다.

**구현 기능**
1. 영화관 조회
2. 영화관 찜
3. 영화 조회
4. 영화 예매, 취소
5. 영화 찜
6. 매점 구매 (환불X)
- 기타 기능 설명
    - 모든 영화관에 특별관과 일반관이 존재해요
    - 특별관, 일반관 종류가 같다면 좌석은 동일해요
    - 좌석은 직사각형 형태로 존재해요 (중간에 비어있는 곳 없음, 통로 고려X)
    - 영화관마다 매점이 있으며 재고를 따로 관리해요 (재고는 항상 1 이상이에요)
    - 모든 영화관의 매점 메뉴는 같아요

---

<img width="1635" height="834" alt="Image" src="https://github.com/user-attachments/assets/9cb8d25f-9a83-4f26-9eea-671dcee1282c" />

### 🎞️ MOVIE (영화)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| movie_id | PK | 영화 고유 ID |
| title | varchar | 영화 제목 |
| running_time | int | 러닝타임 (분) |
| rating | varchar | 관람 등급 |
| release_date | date | 개봉일 |
| genre | varchar | 장르 |
| prologue | text | 줄거리 |
| poster_url | varchar | 포스터 이미지 |

**관계**
- 1 : N → SCHEDULE
- 1 : N → MOVIE_WISH

---

### 🗓️ SCHEDULE (상영 일정)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| schedule_id | PK | 상영 일정 ID |
| start_time | datetime | 상영 시작 시간 |
| hall_id | FK | 상영관 ID |
| movie_id | FK | 영화 ID |

**관계**
- N : 1 → MOVIE
- N : 1 → HALL
- 1 : N → RESERVATION

---

### 🏢 THEATER (영화관)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| theater_id | PK | 영화관 ID |
| name | varchar | 지점명 |
| location | varchar | 지역 |
| address | varchar | 상세 주소 |

**관계**
- 1 : N → HALL
- 1 : N → STORE_ORDER
- 1 : N → THEATER_WISH

---

### 🎦 HALL (상영관)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| hall_id | PK | 상영관 ID |
| name | varchar | 상영관 이름 |
| theater_id | FK | 영화관 ID |
| type_id | FK | 상영관 종류 ID |

**관계**
- N : 1 → THEATER
- N : 1 → HALL_TYPE
- 1 : N → SCHEDULE

---

### 🧩 HALL_TYPE (상영관 종류)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| type_id | PK | 타입 ID |
| type_name | varchar | IMAX, 4DX, 일반관 |
| row_count | int | 좌석 행 수 |
| col_count | int | 좌석 열 수 |

---

## 🎟️ 2. Booking Domain (예매 시스템)

### 🧾 RESERVATION (예매)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| res_id | PK | 예매 ID |
| status | varchar | BOOKED / CANCELED |
| user_id | FK | 사용자 ID |
| schedule_id | FK | 상영 일정 ID |

**관계**
- N : 1 → USER
- N : 1 → SCHEDULE
- 1 : N → RESERVED_SEAT

---

### 💺 RESERVED_SEAT (예매 좌석)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| seat_row | int | 좌석 행 |
| seat_col | int | 좌석 열 |
| res_id | FK | 예매 ID |

**특이사항**
- (seat_row, seat_col, schedule_id) → 좌석 중복 방지
- 복합키 또는 Unique 제약 사용

---

## 👤 3. User & Convenience Domain

### 👤 USER (사용자)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| user_id | PK | 사용자 ID |
| login_id | varchar | 로그인 ID (Unique) |
| password | varchar | 비밀번호 |
| name | varchar | 사용자 이름 |

**관계**
- 1 : N → RESERVATION
- 1 : N → MOVIE_WISH
- 1 : N → THEATER_WISH
- 1 : N → STORE_ORDER

---

### 🎬 MOVIE_WISH (영화 찜)

| 컬럼명 | 타입 |
|--------|------|
| user_id | FK |
| movie_id | FK |

→ 복합 PK (user_id, movie_id)

---

### 🏢 THEATER_WISH (영화관 찜)

| 컬럼명 | 타입 |
|--------|------|
| user_id | FK |
| theater_id | FK |

→ 복합 PK (user_id, theater_id)

---

## 🍿 4. Store Domain (매점)

### 🛒 STORE_ORDER (매점 주문)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| order_id | PK | 주문 ID |
| total_price | int | 총 금액 |
| order_date | datetime | 주문 시간 |
| user_id | FK | 사용자 ID |
| theater_id | FK | 영화관 ID |

**관계**
- N : 1 → USER
- N : 1 → THEATER

---

### 🥤 ITEM (상품)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| item_id | PK | 상품 ID |
| name | varchar | 상품명 |
| price | int | 가격 |

---

### 📦 STORE_INVENTORY (재고)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| theater_id | FK | 영화관 ID |
| item_id | FK | 상품 ID |
| stock | int | 재고 수량 |

→ 복합 PK (theater_id, item_id)

---

## 🔗 전체 관계 요약

- MOVIE → SCHEDULE → RESERVATION → RESERVED_SEAT
- THEATER → HALL → SCHEDULE
- USER → RESERVATION
- USER → MOVIE_WISH / THEATER_WISH
- THEATER → STORE_ORDER
- STORE_ORDER → ITEM (via order items 확장 가능)
- ITEM → STORE_INVENTORY (영화관별 재고 관리)

---

## ⚠️ 설계 핵심 포인트

- 좌석은 **행/열 기반으로 관리 (직사각형 구조)**
- 좌석 중복 방지는 **(schedule_id + row + col) unique**
- 매점 메뉴는 공통, 재고는 영화관별 관리
- 예매 취소는 상태값으로 관리 (삭제 X)
- 찜 기능은 **N:M 관계 → 교차 테이블로 분리**

</details>

---
<details>
<summary><strong>3주차 미션 관련 내용 정리</strong></summary>

<br />

### 1. Spring Security의 역할
- 애플리케이션의 **인증(Authentication)** 과 **인가(Authorization)** 를 담당한다.
- 이번 과제에서는 세션 방식 대신 **JWT 기반의 stateless 인증** 구조를 사용했다.
- `SecurityConfig`에서 어떤 요청을 허용할지, 어떤 요청에 인증이 필요한지, 어떤 요청이 관리자 권한이 필요한지를 한 곳에서 관리했다.

### 2. JwtTokenProvider의 역할
- JWT를 **생성하고, 파싱하고, 검증하는 역할**을 담당한다.
- 로그인에 성공하면 `userId`와 `loginId`를 담은 access token을 발급한다.
- 이후 요청에서는 토큰에서 사용자 정보를 다시 꺼내 인증에 활용한다.
- 즉, 토큰과 관련된 로직을 서비스나 필터에 흩어놓지 않고 **전담 클래스 하나로 분리**했다는 점이 중요했다.

### 3. JwtAuthenticationFilter의 역할
- 클라이언트 요청이 들어올 때마다 실행되며, `Authorization` 헤더에서 Bearer 토큰을 확인한다.
- 토큰이 유효하면 `loginId`를 꺼내 사용자 정보를 조회하고, Spring Security가 이해할 수 있는 `Authentication` 객체를 만든다.
- 그 인증 객체를 `SecurityContext`에 저장해서, 이후 컨트롤러나 인가 로직이 "현재 로그인한 사용자"를 사용할 수 있게 한다.
- 즉, **JWT 문자열을 실제 로그인 상태로 바꿔주는 연결 지점** 역할을 한다.

### 4. AuthenticatedUser의 역할
- `UserDetails`를 구현한 커스텀 인증 객체다.
- `userId`, `loginId`, `password`, `authorities`를 보관해서 Spring Security 내부 인증 객체로 사용된다.
- 컨트롤러에서 `@AuthenticationPrincipal AuthenticatedUser`로 바로 받아서 현재 로그인한 사용자의 `userId`를 꺼낼 수 있었다.
- 덕분에 매번 토큰을 직접 파싱하지 않고도 비즈니스 로직에서 사용자 식별이 쉬워졌다.

### 5. 인증 흐름 정리
1. 사용자가 로그인한다.
2. `AuthService`가 아이디/비밀번호를 검증한 뒤 JWT를 발급한다.
3. 이후 요청마다 클라이언트가 `Authorization: Bearer {token}` 형식으로 토큰을 보낸다.
4. `JwtAuthenticationFilter`가 토큰을 검증하고 `SecurityContext`에 인증 정보를 저장한다.
5. Spring Security가 인증/권한을 확인한 뒤 컨트롤러까지 요청을 전달한다.

### 6. Spring Security 예외 처리는 어떻게 했는가
- 일반 예외는 `GlobalExceptionHandler`에서 처리하지만, **Security 필터 단계에서 발생하는 인증/인가 예외는 별도로 처리**해야 했다.
- 인증이 안 된 사용자가 보호된 API에 접근하면 `CustomAuthenticationEntryPoint`가 동작해서 `401 Unauthorized` JSON 응답을 내려주도록 했다.
- 로그인은 했지만 권한이 없는 사용자가 접근하면 `CustomAccessDeniedHandler`가 동작해서 `403 Forbidden` JSON 응답을 내려주도록 했다.
- 그리고 이 두 클래스를 `SecurityConfig`의 `exceptionHandling()`에 등록해서, Spring Security 기본 HTML 에러 페이지 대신 **프로젝트 공통 형식의 JSON 에러 응답**을 주도록 맞췄다.

### 7. 이번 과제에서 중요하게 느낀 포인트
- JWT 인증은 "로그인 시 토큰 발급"보다도, **매 요청마다 토큰을 해석해서 SecurityContext에 넣는 과정**이 핵심이다.
- Spring Security를 쓰면 인증 정보를 컨트롤러까지 자연스럽게 전달할 수 있어서, 비즈니스 로직이 더 깔끔해진다.
- 예외 처리도 MVC 예외 처리와 Security 예외 처리가 나뉘기 때문에, **어디서 발생한 예외인지에 따라 처리 지점이 다르다**는 점을 배웠다.

</details>

---

<details>
<summary><strong>4주차 미션 관련 내용 정리</strong></summary>

<br />

## 1. 동시성 해결 방법 조사 및 적용

영화 예매 서비스에서 가장 중요한 동시성 문제는 **같은 상영 회차의 동일 좌석에 대해 여러 사용자가 동시에 예약을 시도하는 상황**이다.
이번 과제에서는 이 문제를 해결하기 위해 여러 동시성 제어 방식을 비교했다.

### 1. 비관적 락 (Pessimistic Lock)

비관적 락은 충돌이 자주 발생할 것이라고 가정하고, 데이터를 조회하는 시점부터 락을 걸어 다른 트랜잭션의 접근을 제한하는 방식이다.
정합성을 강하게 보장할 수 있다는 장점이 있지만, 락 범위가 넓어질수록 대기 시간이 길어지고 처리량이 줄어들 수 있다.

이 방식은 **이미 DB에 존재하는 row**를 기준으로 적용할 수 있다.
따라서 경쟁이 치열하고, 충돌 가능성이 높으며, 데이터 정합성이 매우 중요한 경우에 적합하다.

이번 과제에서 고민한 방식은 `reserve()` 메서드 내에서 `schedule` 조회 시점에 비관적 락을 거는 방법이었다.
하지만 이 경우 동일한 `schedule`에 대한 예약 요청 전체가 직렬화된다. 즉, 같은 상영 회차 안에서 서로 다른 좌석을 예약하는 요청도 함께 대기하게 된다.
정합성은 확보할 수 있지만, **좌석 단위가 아니라 스케줄 단위로 경쟁을 묶어버린다는 점에서 락 범위가 너무 넓다**고 판단했다.

| 항목 | 내용 |
|------|------|
| 장점 | 정합성을 강하게 보장할 수 있음 |
| 단점 | 락 범위가 넓어지면 성능 저하와 대기 시간이 커짐 |
| 적합한 경우 | 충돌이 잦고, 동일 자원에 대한 동시 수정이 자주 발생하는 경우 |
| 과제에서의 판단 | `schedule` 단위로 직렬화되어 좌석 예매에는 락 범위가 과하다고 판단 |

### 2. 낙관적 락 (Optimistic Lock)

낙관적 락은 충돌이 자주 발생하지 않는다고 가정하고, 실제 저장 시점에 버전 정보를 비교하여 충돌 여부를 판단하는 방식이다.
평상시에는 성능상 이점이 있지만, 충돌이 발생했을 때 재시도 로직이 필요하다.

즉, **읽기 비중이 높고 충돌 빈도가 상대적으로 낮은 환경**에서는 적합하지만, 좌석 예매처럼 같은 자원에 대한 동시 요청이 자주 몰릴 수 있는 상황에서는 재시도 비용이 커질 수 있다.
특히 영화 예매는 인기 상영 시간대나 인기 좌석으로 요청이 집중될 수 있어, 현재 문제를 해결하는 핵심 방식으로는 적합도가 낮다고 판단했다.

| 항목 | 내용 |
|------|------|
| 장점 | 평상시 성능이 좋고 락 대기가 없음 |
| 단점 | 충돌 시 재시도 로직이 필요하고 실패 비용이 커질 수 있음 |
| 적합한 경우 | 읽기가 많고 충돌 빈도가 낮은 경우 |
| 과제에서의 판단 | 좌석 예매는 충돌 가능성이 높아 주된 해결책으로는 부적합 |

### 3. DB 유니크 제약조건 (Unique Constraint)

DB 차원에서 중복 데이터를 허용하지 않도록 제약조건을 설정하는 방식이다.
이번 과제에서는 `(schedule_id, seat_row, seat_col)` 조합에 유니크 제약조건을 두어, 동일한 상영 회차의 동일 좌석이 중복 저장되지 않도록 했다.

이 방식은 구현이 비교적 단순하고, 애플리케이션 로직과 무관하게 DB가 최종적으로 데이터 무결성을 보장한다는 장점이 있다.
다만 애플리케이션 레벨에서 경쟁을 미리 제어하는 것이 아니라, **실제로 insert를 시도한 뒤에야 예외가 발생한다**는 한계가 있다.
즉, 이미 커넥션과 쿼리 비용이 발생한 이후라는 점에서 사전 제어 방식보다는 비효율적일 수 있다.

그럼에도 불구하고 이번 과제 범위에서는 구현 난이도와 실용성을 모두 고려했을 때 가장 현실적인 방식이라고 판단했다.
현재 좌석 선점 로직은 이 유니크 제약조건을 기반으로 중복 예약을 방지하고, 충돌 시 `DataIntegrityViolationException`을 비즈니스 예외로 변환해 처리하고 있다.

| 항목 | 내용 |
|------|------|
| 장점 | 구현이 단순하고 DB가 최종 무결성을 강하게 보장함 |
| 단점 | insert 이후에야 충돌을 감지하므로 비용이 더 큼 |
| 적합한 경우 | 중복 생성 방지가 핵심이고, 단일 DB 기반으로 빠르게 안정성을 확보해야 하는 경우 |
| 과제에서의 판단 | **현재 적용한 방식**으로, 좌석 중복 예약 방지를 위한 가장 실용적인 선택 |

### 4. Redis 분산 락

Redis를 이용하면 DB row가 없어도 좌석별 key를 기준으로 락을 걸 수 있다.
예를 들어 `schedule:{scheduleId}:seat:{seatRow}:{seatCol}` 같은 key를 사용하면, 좌석 단위로 세밀하게 락을 제어할 수 있다.

이 방식의 가장 큰 장점은 **아직 DB에 존재하지 않는 예약 대상에도 락을 걸 수 있다**는 점이다.
즉, 현재처럼 `reserved_seat`가 예약 시점에 새로 생성되는 구조에서도 좌석 단위 경쟁을 자연스럽게 제어할 수 있다.
또한 `schedule` 전체가 아니라 좌석 단위로 락을 걸 수 있어, 같은 상영 회차 내에서도 서로 다른 좌석 예약 요청은 병렬로 처리할 수 있다.

다만 Redis 도입, 락 해제 처리, TTL 설정, 장애 상황 대응 등 운영 복잡도가 커진다.
이번 과제에서는 학습 및 구현 범위를 고려해 도입을 보류했지만, **장기적으로 실제 서비스 수준으로 확장한다면 가장 먼저 고려할 방식**이라고 판단했다.

| 항목 | 내용 |
|------|------|
| 장점 | DB row 없이도 좌석 단위로 세밀한 락 제어 가능 |
| 단점 | Redis 도입과 락 관리 로직 등 운영 복잡도가 증가 |
| 적합한 경우 | 다중 인스턴스 환경이거나, 좌석 단위의 정교한 경쟁 제어가 필요한 경우 |
| 과제에서의 판단 | 현재는 보류했지만, 추후 도입을 고려 중인 방식 |

### 현재 적용한 방식과 판단

이번 과제에서는 **DB 유니크 제약조건을 활용해 동일 상영 회차의 동일 좌석 중복 예약을 방지**하고 있다.
이 방식은 애플리케이션 단계에서 미리 경쟁을 제어하지는 못하지만, 최소한의 구현으로도 데이터 무결성을 보장할 수 있다는 장점이 있다.

정리하면 현재 판단은 다음과 같다.

- 현재 적용: DB 유니크 제약조건 기반 좌석 중복 예약 방지
- 검토했지만 적용하지 않음: `schedule` 단위 비관적 락
- 추후 도입 고려: Redis 기반 좌석 단위 분산 락

---

## 2. Feign Client / Http Client 장단점 조사

외부 결제 서버(PortOne)와 통신하기 위해 사용할 수 있는 HTTP 클라이언트 방식도 함께 정리했다.
Spring 환경에서 많이 사용하는 방식은 `Feign Client`, `RestClient`, `WebClient`, 그리고 보다 저수준의 `HttpClient` 계열로 나눌 수 있다.
각 방식의 특징과 장단점은 다음과 같다.

### 1. Feign Client

Feign Client는 선언형 HTTP 클라이언트로, 인터페이스에 메서드와 어노테이션을 정의하면 구현체를 자동으로 생성해 주는 방식이다.
코드가 간결하고, 외부 API 명세를 인터페이스 형태로 분리할 수 있어 가독성이 좋다.

반면 세부 요청/응답 제어가 필요할 때는 설정이 많아질 수 있고, 실제 요청이 추상화되어 보여 디버깅이 다소 불편할 수 있다.

| 항목 | 내용 |
|------|------|
| 장점 | 선언형이라 코드가 간결하고 인터페이스 기반으로 관리하기 쉬움 |
| 단점 | 세부 제어와 디버깅이 상대적으로 불편할 수 있음 |
| 적합한 경우 | 외부 API가 많고, 명세가 비교적 안정적이며, 클라이언트 코드를 일관되게 관리하고 싶은 경우 |

### 2. RestClient

`RestClient`는 Spring 6부터 제공되는 동기식 HTTP 클라이언트로, 기존 `RestTemplate`보다 현대적인 API를 제공한다.
요청 URL, 헤더, 바디, 응답 파싱 과정을 코드에서 명시적으로 확인할 수 있어 흐름을 이해하기 쉽다.

요청과 응답 흐름이 코드에 직접 드러나기 때문에, 예외 처리나 응답 파싱을 세밀하게 다루기 좋다.

| 항목 | 내용 |
|------|------|
| 장점 | 요청/응답 흐름이 명확하고, Spring 환경에서 사용하기 편함 |
| 단점 | 선언형 방식에 비해 코드가 다소 길어질 수 있음 |
| 적합한 경우 | 외부 API 호출 흐름을 직접 제어하고, 예외 처리나 응답 파싱을 세밀하게 다뤄야 하는 경우 |

### 3. WebClient

`WebClient`는 비동기/논블로킹 기반의 HTTP 클라이언트다.
대량의 외부 요청을 효율적으로 처리하거나, 반응형 프로그래밍이 필요한 환경에서 강점을 가진다.

반면 동기식 MVC 구조에서는 코드 복잡도가 증가할 수 있고, 프로젝트 전반이 반응형 구조가 아닐 경우 장점을 충분히 활용하기 어렵다.

| 항목 | 내용 |
|------|------|
| 장점 | 비동기/논블로킹 처리에 강하고 확장성이 좋음 |
| 단점 | 동기식 MVC 구조에서는 오히려 코드 복잡도가 증가할 수 있음 |
| 적합한 경우 | 대량 외부 호출, 반응형 프로그래밍, 논블로킹 처리가 중요한 경우 |

### 4. HttpClient (저수준 클라이언트)

Java 기본 `HttpClient`나 Apache HttpClient 같은 저수준 HTTP 클라이언트는 요청과 응답을 가장 세밀하게 제어할 수 있다.
반면 Spring 애플리케이션에서 사용하는 경우, 예외 처리, 직렬화/역직렬화, 공통 설정 등을 직접 더 많이 관리해야 한다.

| 항목 | 내용 |
|------|------|
| 장점 | 세부 제어가 가장 자유롭고 라이브러리 의존이 적을 수 있음 |
| 단점 | 직접 관리해야 할 코드와 설정이 많아짐 |
| 적합한 경우 | 매우 세밀한 제어가 필요하거나, 프레임워크 의존을 최소화해야 하는 경우 |

---

## 3. API 테스트 결과

### 1. 좌석 선점 성공

- `POST /api/reservations`
- 동일 상영 회차에서 선택한 좌석이 정상적으로 선점되는 것을 확인했다.

<img width="886" height="141" alt="Image" src="https://github.com/user-attachments/assets/e40bbcab-609e-4ee3-a87e-3358e5946113" />

### 2. 결제 성공

- `POST /api/reservations/{id}/pay`
- 예매 결제가 정상적으로 완료되고, 결제 상태가 `PAID`로 반영되는 것을 확인했다.

<img width="891" height="310" alt="Image" src="https://github.com/user-attachments/assets/2eb6a589-9c83-4541-bddf-d3cb872a8872" />

### 3. 취소 성공

- `PATCH /api/reservations/{id}/cancel`
- 예약 취소와 함께 결제 취소가 정상적으로 수행되는 것을 확인했다.

<img width="884" height="180" alt="Image" src="https://github.com/user-attachments/assets/5b057b40-ab92-4200-87bb-f5cac289fa9e" />

</details>

---

<details>
<summary><strong>5주차 미션 관련 내용 정리</strong></summary>

<br />

## 1. 배포 모습

### 1. 수동 배포
<img width="1016" height="30" alt="Image" src="https://github.com/user-attachments/assets/d70f88de-ae4d-47a1-8570-f1d371d2247f" />

### 2. CI/CD
<img width="1127" height="460" alt="Image" src="https://github.com/user-attachments/assets/dc5526ed-c7b9-4912-b612-c111209042d8" />

### 3. CI/CD 이후 회원가입 API 테스트
<img width="855" height="576" alt="Image" src="https://github.com/user-attachments/assets/ba8c3c77-aa1b-43b6-af93-a80593420d0a" />

---

## 2. 배포 관련 내용 정리

### 1. 배포 과정 요약

- CI/CD: GitHub Actions를 활용해 빌드부터 Docker Hub 이미지 푸시, EC2 배포까지 자동화했습니다.
- 컨테이너: Docker를 사용해 환경 의존성 문제를 줄였고, 외부 `80` 포트와 내부 `8080` 포트를 연결해 배포했습니다.
- 인프라: AWS EC2에는 애플리케이션을, AWS RDS에는 데이터베이스를 두는 구조로 분리해 연동했습니다.

### 2. 문제 해결

- 파일 경로 문제: Dockerfile 작성 과정에서 빌드 결과물인 `jar` 파일의 실제 위치와 `COPY` 대상 경로가 달라 에러가 발생했고, 빌드 산출물 경로를 다시 확인해 수정했습니다.
- 라이브러리 버전 호환성: Hibernate 7 환경에서 더 이상 권장되지 않는 `MySQL8Dialect` 설정으로 인해 구동 에러가 발생했고, 해당 설정을 제거해 해결했습니다.
- 네트워크 보안 설정: EC2에서 RDS로 연결할 때 `Connection Timeout`이 발생했고, AWS 보안 그룹에서 `3306` 포트를 허용하도록 수정해 해결했습니다.

### 3. 배운 점

- 서버가 정상적으로 실행되지 않을 때 `docker logs`를 확인하면서 `Caused by` 구문을 따라가면 실제 원인을 더 정확하게 파악할 수 있다는 점을 배웠습니다.
- 로컬 환경과 배포 환경은 설정값이 조금만 달라도 다른 문제가 발생할 수 있어서, 환경별 설정 분리의 필요성을 체감했습니다.
- GitHub Actions 파이프라인이 성공했다고 해서 배포가 끝난 것이 아니라, 실제로 API를 호출해 정상 응답까지 확인해야 배포가 완료된다고 볼 수 있다는 점을 배웠습니다.

</details>

---

<details>
<summary><strong>6주차 미션 관련 내용 정리</strong></summary>

<br />

## 0. 서비스 아키텍처

<img width="597" height="579" alt="Image" src="https://github.com/user-attachments/assets/67587a15-6cfa-42b5-b5ee-4ec54aaa4bea" />

---

## 1. 부하테스트 개요

k6를 사용해 두 가지 부하테스트를 진행했습니다.

1. 결제 API 부하테스트
2. 좌석 예약 동시성 제어 테스트

테스트 대상 서버는 EC2에 배포된 Spring Boot 서버이며, DB는 AWS RDS MySQL을 사용했습니다.

---

## 2. 결제 API 부하테스트

### 1. 테스트 목적

사용자가 좌석을 예약한 뒤 결제까지 진행하는 흐름에서 서버와 DB가 안정적으로 동작하는지 확인했습니다.

테스트 흐름은 다음과 같습니다.

> 로그인 → 좌석 예약 → 결제 요청 → 결제 상태 확인

### 2. 테스트 결과

| 지표 | 결과 |
| --- | ---: |
| 총 iteration 수 | 207 |
| 예약 시도 수 | 207 |
| 결제 성공 수 | 190 |
| 결제 실패 수 | 17 |
| 결제 성공률 | 91.78% |
| HTTP 실패율 | 4.09% |
| 평균 응답 시간 | 54.88ms |
| p95 응답 시간 | 110.11ms |
| 최대 응답 시간 | 1.59s |

<img width="1496" height="765" alt="Image" src="https://github.com/user-attachments/assets/a83a5f66-6d81-4d75-b576-120ea07a9b8a" />

<img width="1451" height="781" alt="Image" src="https://github.com/user-attachments/assets/3cb314e8-5603-45a5-8e03-d239a396fcdd" />

### 3. 분석

결제 API 부하테스트 결과, 평균 응답 시간은 `54.88ms`, p95 응답 시간은 `110.11ms`로 측정되었습니다. 기준으로 설정한 `p95 1500ms`보다 훨씬 낮기 때문에, 현재 `small` 부하 수준에서는 EC2 서버나 RDS가 응답 지연의 병목이라고 보기는 어렵다고 판단했습니다.

예약 요청은 정상적으로 처리되었지만, 결제 요청 `207건` 중 `17건`이 실패했습니다. 결제 성공률은 `91.78%`로 측정되었고, 일부 결제 요청에서 `500` 에러가 발생했습니다.

따라서 현재 결과에서 주요 의심 지점은 EC2나 DB 자체 성능보다는 결제 처리 구간입니다. 특히 결제 로직에서 외부 결제 API를 호출하고 있으므로, 외부 API 연동 과정이나 결제 트랜잭션 처리 구조에서 일부 실패가 발생했을 가능성이 있다고 보았습니다.

---

## 3. 좌석 예약 동시성 제어 테스트

### 1. 테스트 목적

여러 사용자가 동시에 같은 상영일정의 같은 좌석을 예약하려고 할 때, 중복 예약이 발생하지 않는지 확인했습니다.

테스트에서는 모든 요청이 아래 좌석으로 집중되도록 설정했습니다.

| 항목 | 값 |
| --- | --- |
| schedule_id | 1 |
| seat_row | A |
| seat_col | 1 |

### 2. 테스트 결과

| 지표 | 결과 |
| --- | ---: |
| 총 iteration 수 | 219 |
| 예약 시도 수 | 219 |
| 예약 성공 수 | 1 |
| 좌석 충돌 수 | 218 |
| 평균 응답 시간 | 28.94ms |
| p95 응답 시간 | 28.83ms |
| HTTP 실패율 | 98.64% |

<img width="1497" height="754" alt="Image" src="https://github.com/user-attachments/assets/3f5fbe1b-7934-4641-9e8f-fde7c60b0606" />

<img width="1496" height="797" alt="Image" src="https://github.com/user-attachments/assets/5fe7c49b-af53-4ee3-9062-c309ababeb29" />

### 3. 분석

동시성 테스트에서는 동일 좌석에 `219회`의 예약 요청을 집중시켰습니다. 그 결과 최초 `1건`만 예약에 성공했고, 나머지 `218건`은 이미 예약된 좌석으로 처리되었습니다.

이 테스트에서 HTTP 실패율이 `98.64%`로 높게 나타났지만, 이는 서버 장애로 해석하면 안 된다고 판단했습니다. k6는 `400` 계열의 비즈니스 예외 응답도 실패로 집계하기 때문입니다. 이번 테스트에서는 동일 좌석에 요청을 집중시키는 것이 목적이었으므로, 대부분의 요청이 `"이미 예약된 좌석"`으로 거절되는 것이 정상적인 결과였습니다.

동시성 제어 성공 여부는 HTTP 실패율이 아니라 DB에 중복 좌석 예약 데이터가 생성되었는지로 판단했습니다. 테스트 후 DB를 확인한 결과, 동일한 `schedule_id`, `seat_row`, `seat_col` 조합의 중복 데이터는 생성되지 않았습니다.

따라서 좌석 예약 동시성 제어는 정상적으로 동작한다고 판단했습니다.

---

## 4. 병목 지점 진단

### 1. EC2

결제 API 부하테스트에서 p95 응답 시간은 `110.11ms`, 동시성 테스트에서 p95 응답 시간은 `28.83ms`로 측정되었습니다. 두 테스트 모두 응답 시간이 안정적으로 유지되었기 때문에, 현재 `small` 부하 수준에서는 EC2가 병목이라고 보기 어렵다고 판단했습니다.

### 2. RDS

예약 생성과 좌석 선점 데이터 저장이 정상적으로 처리되었고, 동시성 테스트에서도 중복 예약 데이터가 생성되지 않았습니다. 따라서 현재 테스트 규모에서는 RDS 역시 명확한 병목으로 보이지 않았습니다.

### 3. 결제 처리 구간

결제 API 부하테스트에서 일부 결제 요청이 실패했습니다. 응답 시간이 느려진 것이 아니라 결제 요청 자체가 `500` 에러로 실패했기 때문에, 병목 지점은 EC2/RDS의 처리 성능보다는 외부 결제 API 연동 또는 결제 처리 로직에 있을 가능성이 높다고 보았습니다.

</details>


---

<details>
<summary><strong>7주차 미션 관련 내용 정리</strong></summary>

<br />

## 1. Redis 캐싱 적용

### 1. 적용 내용

영화/극장 조회 API는 반복 호출될 가능성이 높고, 데이터 변경 빈도는 상대적으로 낮다고 판단했습니다.
특히 전체 목록 조회와 상세 조회는 같은 요청이 여러 번 들어올 수 있기 때문에, 매번 DB를 조회하는 대신 Redis 캐시를 두는 것이 더 효율적이라고 보았습니다.

이번 프로젝트에서는 다음 조회 기능에 캐싱을 적용했습니다.

- 영화 전체 조회
- 영화 상세 조회
- 극장 전체 조회
- 극장 상세 조회

```java
@Cacheable(cacheNames = "movies", key = "'all'")
public List<MovieResponse> findAllMovies() {
    return movieRepository.findAll().stream()
            .map(MovieResponse::from)
            .toList();
}

@Cacheable(cacheNames = "movie", key = "#id")
public MovieResponse findMovieById(Long id) {
    Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MovieErrorCode.MOVIE_NOT_FOUND));
    return MovieResponse.from(movie);
}
```

---

### 2. 캐싱 전략

캐싱 전략은 `Look-aside(Cache-aside)` 방식을 기준으로 적용했습니다.

이 방식은 애플리케이션이 먼저 캐시를 확인하고, 캐시에 값이 없을 때만 DB를 조회한 뒤 결과를 캐시에 저장하는 구조입니다.
이번 프로젝트에서는 Spring Cache의 `@Cacheable`을 사용해, Redis 조회/저장 로직을 직접 작성하지 않고 Spring의 캐시 추상화를 활용했습니다.

영화 상세 조회 기준 흐름은 다음과 같습니다.

1. 클라이언트가 영화 상세 조회 요청
2. Spring Cache가 Redis에서 `movie::{id}` 조회
3. 캐시에 값이 있으면 Redis 데이터 반환
4. 캐시에 값이 없으면 DB 조회
5. 조회 결과를 Redis에 저장
6. 응답 반환

---

### 3. Cache Key 설계

전체 조회와 상세 조회는 성격이 다르기 때문에 cache name을 분리했습니다.

| 대상 | Cache Name | Key | Redis Key 예시 |
|------|------|------|------|
| 영화 전체 조회 | `movies` | `'all'` | `movies::all` |
| 영화 상세 조회 | `movie` | `#id` | `movie::1` |
| 극장 전체 조회 | `theaters` | `'all'` | `theaters::all` |
| 극장 상세 조회 | `theater` | `#id` | `theater::1` |

전체 조회는 별도 조건 없이 목록 전체를 가져오기 때문에 key를 `'all'`로 고정했고, 상세 조회는 리소스 ID별로 결과가 달라지므로 `#id`를 사용했습니다.

---

### 4. TTL 설정

캐시를 너무 오래 유지하면 DB와 Redis의 데이터가 달라질 수 있기 때문에 TTL을 함께 설정했습니다.

```java
Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
        "movies", defaultConfig.entryTtl(Duration.ofMinutes(5)),
        "movie", defaultConfig.entryTtl(Duration.ofMinutes(10)),
        "theaters", defaultConfig.entryTtl(Duration.ofMinutes(5)),
        "theater", defaultConfig.entryTtl(Duration.ofMinutes(10))
);
```

| Cache Name | TTL | 설정 이유 |
|------|------|------|
| `movies` | 5분 | 전체 목록은 추가/삭제의 영향을 받기 때문에 비교적 짧게 설정 |
| `movie` | 10분 | 상세 정보는 목록보다 변경 빈도가 낮다고 판단 |
| `theaters` | 5분 | 극장 목록도 변경 가능성을 고려해 짧게 설정 |
| `theater` | 10분 | 극장 상세 정보는 상대적으로 변경 빈도가 낮다고 판단 |

과제 목적상 TTL을 너무 길게 두기보다, 캐시 동작을 직접 확인하기 쉬운 수준으로 설정했습니다.

---

### 5. 캐시 무효화

조회 결과가 변경될 수 있는 작업에서는 기존 캐시를 제거하도록 구성했습니다.

영화 생성 시에는 전체 목록 캐시를 삭제합니다.

```java
@Transactional
@CacheEvict(cacheNames = "movies", allEntries = true)
public Long saveMovie(MovieCreateRequest request) {
    Movie movie = request.toEntity();
    return movieRepository.save(movie).getId();
}
```

영화 삭제 시에는 전체 목록 캐시와 상세 캐시를 함께 삭제합니다.

```java
@Transactional
@Caching(evict = {
        @CacheEvict(cacheNames = "movies", allEntries = true),
        @CacheEvict(cacheNames = "movie", key = "#id")
})
public void deleteMovieById(Long id) {
    Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MovieErrorCode.MOVIE_NOT_FOUND));
    movieRepository.delete(movie);
}
```

| 작업 | 무효화 대상 |
|------|------|
| 영화 생성 | `movies` |
| 영화 삭제 | `movies`, `movie::{id}` |

극장 도메인은 이번 과제 범위에서 생성/삭제 기능을 구현하지 않았기 때문에, 조회 캐시만 적용했습니다.

---

### 6. 직렬화 설정 관련 트러블슈팅

캐싱 적용 과정에서 `MovieResponse`와 같은 DTO가 Java `record` 타입이라는 점 때문에 Redis 역직렬화 문제가 발생했습니다.

```java
public record MovieResponse(
        Long id,
        String title,
        int runningTime,
        Genre genre,
        LocalDate releaseDate
) { }
```

타입 정보가 충분히 포함되지 않아 캐시 hit 시 Redis 값을 다시 DTO로 복원하는 과정에서 문제가 발생했습니다.
이를 해결하기 위해 `ObjectMapper`에 타입 정보를 포함하도록 설정했고, `JavaTimeModule`도 함께 등록해 `LocalDate`와 같은 시간 타입까지 안정적으로 처리하도록 구성했습니다.

```java
objectMapper.registerModule(new JavaTimeModule());
objectMapper.activateDefaultTyping(
        BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build(),
        ObjectMapper.DefaultTyping.EVERYTHING,
        JsonTypeInfo.As.PROPERTY
);
```

---

### 7. 캐싱 결과 확인

캐싱 적용 여부는 `redis-cli monitor`로 확인했습니다.

```bash
docker exec -it spring-redis redis-cli monitor
```
<img width="737" height="245" alt="Image" src="https://github.com/user-attachments/assets/ac8efff8-9d4b-4fb9-9073-7ff759fde1ba" />

영화 상세 조회 API를 처음 호출했을 때는 `GET` 후 `SET`이 발생했고, 같은 API를 다시 호출했을 때는 `GET`만 발생했습니다.
즉 첫 번째 요청에서는 DB 조회 후 Redis에 캐시가 저장되었고, 두 번째 요청부터는 Redis 캐시를 사용한다는 것을 확인할 수 있었습니다.

---

## 2. 로깅 적용

### 1. 적용 내용

API가 실패했을 때 단순히 에러 응답만 보는 것으로는 원인을 파악하기 어려웠기 때문에, 요청 흐름과 비즈니스 이벤트, 예외를 구분해서 확인할 수 있도록 로깅을 적용했습니다.

다음 항목 위주로 로그를 작성했습니다.

- 주요 서비스 메서드의 비즈니스 로그
- 공통 예외 로그
- 로그인/예매/결제/권한 실패에 대한 감사 로그
- AOP 기반 API 실행 시간 로그

---

### 2. 로그 레벨 전략

| 로그 레벨 | 사용 목적 | 예시 |
|------|------|------|
| `DEBUG` | 개발 중 상세 흐름 확인 | SQL 확인, 캐시 동작 확인 |
| `INFO` | 정상적인 주요 이벤트 기록 | API 실행, 영화 생성, 예매 완료, 결제 성공 |
| `WARN` | 비정상 요청 또는 주의 상황 | 중복 회원가입, 좌석 중복 예매 시도, 권한 실패 |
| `ERROR` | 시스템 장애 또는 예상하지 못한 예외 | 서버 내부 오류, 외부 연동 실패 |

운영 환경에서는 과도한 로그를 줄이기 위해 `DEBUG`와 SQL 로그를 최소화하고, 주요 비즈니스 이벤트 중심으로 `INFO` 이상 로그를 남기도록 구성했습니다.

---

### 3. 개발/운영 환경 로그 설정

개발 환경에서는 애플리케이션 로그를 `DEBUG`로 두고 SQL 로그도 확인할 수 있도록 했습니다.

```yaml
logging:
  level:
    com.cgv.spring_boot: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
```
결과
```
2026-05-16T22:07:54.749+09:00  WARN 60076 --- [nio-8080-exec-1] c.c.s.domain.user.service.AuthService    : signup rejected. loginId=test
2026-05-16T22:07:54.750+09:00  INFO 60076 --- [nio-8080-exec-1] c.c.s.global.logging.ApiLoggingAspect    : api executed. method=POST, uri=/api/users/signup, handler=AuthController.signup(..), durationMs=8
2026-05-16T22:07:54.752+09:00  WARN 60076 --- [nio-8080-exec-1] c.c.s.g.error.GlobalExceptionHandler     : business exception handled. method=POST, uri=/api/users/signup, status=400, message=이미 사용 중인 아이디입니다.
2026-05-16T22:07:54.774+09:00  WARN 60076 --- [nio-8080-exec-1] .m.m.a.ExceptionHandlerExceptionResolver : Resolved [com.cgv.spring_boot.global.error.exception.BusinessException: 이미 사용 중인 아이디입니다.]
```

운영 환경에서는 SQL 로그와 바인딩 로그를 끄고, 애플리케이션 로그는 `INFO` 중심으로 확인하도록 설정했습니다.

```yaml
logging:
  level:
    com.cgv.spring_boot: INFO
    org.hibernate.SQL: OFF
    org.hibernate.orm.jdbc.bind: OFF
```
결과
```
2026-05-16T13:08:53.081Z  WARN 1 --- [nio-8080-exec-8] c.c.s.domain.user.service.AuthService    : signup rejected. loginId=test
2026-05-16T13:08:53.092Z  INFO 1 --- [nio-8080-exec-8] c.c.s.global.logging.ApiLoggingAspect    : api executed. method=POST, uri=/api/users/signup, handler=AuthController.signup(..), durationMs=52
2026-05-16T13:08:53.097Z  WARN 1 --- [nio-8080-exec-8] c.c.s.g.error.GlobalExceptionHandler     : business exception handled. method=POST, uri=/api/users/signup, status=400, message=이미 사용 중인 아이디입니다.
```

---

### 4. AOP 기반 API 실행 시간 로깅

Controller마다 실행 시간 측정 코드를 넣으면 중복이 많아지기 때문에, Spring AOP를 사용해 Controller 계층의 public 메서드 실행 시간을 공통적으로 기록하도록 구성했습니다.

```java
@Slf4j
@Aspect
@Component
public class ApiLoggingAspect {

    @Around("execution(public * com.cgv.spring_boot..controller..*(..))")
    public Object logApiExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getCurrentRequest();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String method = request != null ? request.getMethod() : "N/A";
            String uri = request != null ? request.getRequestURI() : "N/A";

            log.info("api executed. method={}, uri={}, handler={}, durationMs={}",
                    method,
                    uri,
                    joinPoint.getSignature().toShortString(),
                    duration);
        }
    }
}
```

이 로그를 통해 어떤 API가 호출되었는지, 어떤 Controller 메서드가 실행되었는지, 처리 시간이 얼마나 걸렸는지를 공통 형식으로 확인할 수 있었습니다.

---

### 5. 서비스/감사 로그 적용

서비스 계층에는 과제 범위에서 의미가 큰 이벤트 위주로 로그를 남겼습니다.

- 영화 생성/삭제, 영화 조회 실패, 캐시 무효화
- 회원가입, 로그인 성공/실패
- 예매 생성/취소, 좌석 중복 예매 시도
- 결제 요청/성공/실패

특히 로그인 실패, 권한 실패, JWT 인증 실패, 예매/결제 이벤트는 `AUDIT` prefix를 붙여 감사 로그 성격이 드러나도록 정리했습니다.

예를 들면 다음과 같은 형태입니다.

```text
AUDIT login success. userId=1, loginId=test
AUDIT authentication failed. method=GET, uri=/api/movies/1
AUDIT reservation created. userId=1, reservationId=3, scheduleId=1, seatCount=2
AUDIT payment succeeded. reservationId=3, paymentId=pay_123, provider=PORTONE
```

---

### 6. 예외 로그 전략

예외는 `GlobalExceptionHandler`에서 공통 처리하도록 구성했고, 예외 성격에 따라 로그 레벨을 구분했습니다.

- `BusinessException` → `WARN`
- 예상하지 못한 `Exception` → `ERROR`

예를 들어 중복 회원가입, 존재하지 않는 영화 조회, 권한 없는 접근처럼 예상 가능한 비즈니스 예외는 `WARN`으로 처리했고, 시스템 장애나 예상하지 못한 내부 오류는 `ERROR`로 구분했습니다.

---

### 7. 민감정보 로깅 방지

로그에는 문제 추적에 필요한 정보만 남기고, 민감정보는 기록하지 않도록 했습니다.

특히 다음 정보는 로그에 남기지 않도록 주의했습니다.

- 비밀번호
- JWT 토큰 원문
- 결제 관련 민감정보
- 개인정보 전체

실제로 AOP 로그에는 request body나 token 값을 남기지 않고, `method`, `uri`, `handler`, `durationMs` 정도만 남기도록 구성했습니다.

---

</details>

---

<details>
<summary><strong>8주차 미션 관련 내용 정리</strong></summary>

<br />

## 1. 트랜잭션 전파 속성 조사

Spring의 `@Transactional`은 `propagation` 옵션을 통해 현재 트랜잭션이 존재할 때 새 메서드가 어떤 방식으로 트랜잭션에 참여할지 결정할 수 있습니다.

| 전파 속성 | 설명 | 사용 예시 |
| --- | --- | --- |
| `REQUIRED` | 기존 트랜잭션이 있으면 참여하고, 없으면 새로 생성한다. 기본값이다. | 일반적인 서비스 계층의 저장/수정 로직 |
| `REQUIRES_NEW` | 항상 새로운 트랜잭션을 생성한다. 기존 트랜잭션은 잠시 중단된다. | 감사 로그 저장, 실패 이력 저장처럼 본 작업과 독립적으로 커밋해야 하는 로직 |
| `SUPPORTS` | 기존 트랜잭션이 있으면 참여하고, 없으면 트랜잭션 없이 실행한다. | 트랜잭션이 필수는 아닌 조회 로직 |
| `NOT_SUPPORTED` | 트랜잭션 없이 실행한다. 기존 트랜잭션이 있으면 잠시 중단한다. | 외부 API 호출, 긴 네트워크 I/O |
| `MANDATORY` | 반드시 기존 트랜잭션이 있어야 한다. 없으면 예외가 발생한다. | 상위 서비스 트랜잭션 내부에서만 호출되어야 하는 내부 메서드 |
| `NEVER` | 트랜잭션이 있으면 예외가 발생한다. | 트랜잭션 안에서 실행되면 안 되는 작업 |
| `NESTED` | 기존 트랜잭션 내부에 savepoint를 만들고 중첩 트랜잭션처럼 동작한다. | 일부 작업만 롤백하고 전체 트랜잭션은 유지해야 하는 경우 |

이번 개선에서는 외부 결제 API 호출을 DB 트랜잭션 밖으로 분리하기 위해 `NOT_SUPPORTED`를 사용했고, DB 상태 변경 구간은 `TransactionTemplate`으로 필요한 부분만 짧게 트랜잭션 처리했습니다.

---

## 2. CGV 서비스 트랜잭션 분석 및 개선

### 기존 문제

기존 예매 결제 흐름은 `ReservationService.pay()`에서 하나의 `@Transactional` 안에 예약 검증, 결제 생성, 외부 결제 API 호출, 예약 확정이 모두 포함되어 있었습니다.

```java
@Transactional
public PaymentResponse pay(Long userId, Long reservationId) {
    Reservation reservation = getOwnedReservation(userId, reservationId);
    validateReservationPayable(reservation);

    PaymentResponse response = paymentService.payReservation(...);
    reservation.confirm();
    return response;
}
```

예매 취소 흐름도 `ReservationService.cancel()`의 트랜잭션 안에서 외부 환불 API를 호출하고 있었습니다.

```java
@Transactional
public void cancel(Long userId, Long reservationId) {
    Reservation reservation = getOwnedReservation(userId, reservationId);
    reservation.cancel();
    paymentService.cancelReservationPayment(reservation);
    reservedSeatRepository.deleteByReservation(reservation);
}
```

이 구조는 다음 문제가 있습니다.

- 외부 결제 API 응답이 늦어지면 DB 트랜잭션과 커넥션이 오래 점유된다.
- 외부 결제는 성공했지만 DB 커밋이 실패하면 결제 상태와 예약 상태가 불일치할 수 있다.
- 외부 API는 DB 트랜잭션으로 롤백할 수 없기 때문에 트랜잭션 범위에 포함하는 것이 적절하지 않다.

### 개선 방향

결제와 취소 메서드에 `Propagation.NOT_SUPPORTED`를 적용하여 전체 메서드는 트랜잭션 없이 실행되도록 변경했습니다.

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public PaymentResponse pay(Long userId, Long reservationId) {
    PaymentReadyResult payment = transactionTemplate.execute(status -> preparePayment(userId, reservationId));

    try {
        PaymentResponse response = paymentService.requestPayment(payment);
        transactionTemplate.executeWithoutResult(status -> completePayment(userId, payment, response));
        return response;
    } catch (BusinessException e) {
        transactionTemplate.executeWithoutResult(status -> paymentService.markPaymentFailed(payment.paymentPk(), e));
        throw e;
    }
}
```

결제 흐름은 다음처럼 분리했습니다.

1. `preparePayment()`
   짧은 트랜잭션 안에서 예약 검증, 결제 금액 계산, `Payment READY` 저장을 처리한다.
2. `requestPayment()`
   트랜잭션 밖에서 외부 결제 API를 호출한다.
3. `completePayment()`
   짧은 트랜잭션 안에서 `Payment PAID` 처리와 `Reservation RESERVED` 처리를 수행한다.
4. 외부 결제 실패 시
   짧은 트랜잭션 안에서 `Payment FAILED`로 변경한다.

취소 흐름도 같은 방식으로 분리했습니다.

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void cancel(Long userId, Long reservationId) {
    PaymentCancelResult payment = transactionTemplate.execute(status -> prepareCancel(userId, reservationId));

    if (payment != null) {
        paymentService.requestPaymentCancel(payment);
        transactionTemplate.executeWithoutResult(status -> completeCancel(userId, reservationId, payment));
    }
}
```

취소 흐름은 다음처럼 변경했습니다.

1. `prepareCancel()`
   짧은 트랜잭션 안에서 본인 예약과 결제 상태를 확인한다.
2. `requestPaymentCancel()`
   트랜잭션 밖에서 외부 환불 API를 호출한다.
3. `completeCancel()`
   짧은 트랜잭션 안에서 `Payment CANCELLED`, `Reservation CANCELLED`, 예약 좌석 삭제를 처리한다.

이렇게 변경하면서 외부 API 호출로 인해 DB 트랜잭션이 길어지는 문제를 줄이고, DB 상태 변경 구간을 명확하게 나눌 수 있었습니다.

---

## 3. 인덱스 종류 조사

| 인덱스 종류 | 특징 | 예시 |
| --- | --- | --- |
| 단일 컬럼 인덱스 | 하나의 컬럼을 기준으로 검색을 빠르게 한다. | `movie(title)` |
| 복합 인덱스 | 여러 컬럼을 조합해 검색을 빠르게 한다. 컬럼 순서가 중요하다. | `reservation(status, expires_at)` |
| 유니크 인덱스 | 중복을 방지하면서 조회 성능도 높인다. | `payment(payment_id)` |
| 커버링 인덱스 | 쿼리에 필요한 컬럼을 인덱스만으로 모두 처리할 수 있어 테이블 접근을 줄인다. | `reservation(status, expires_at, res_id)` |
| 클러스터드 인덱스 | 실제 데이터가 인덱스 순서에 맞게 저장된다. MySQL InnoDB에서는 PK가 클러스터드 인덱스다. | `PRIMARY KEY` |
| 세컨더리 인덱스 | PK 외에 추가로 생성하는 보조 인덱스다. 세컨더리 인덱스는 PK 값을 함께 들고 있다. | `reserved_seat(res_id)` |
| Full-text 인덱스 | 긴 문자열에서 자연어 검색을 빠르게 하기 위한 인덱스다. | 영화 제목/줄거리 검색 |
| 해시 인덱스 | 동등 비교에 강하지만 범위 검색에는 적합하지 않다. | `key = value` 형태의 조회 |

복합 인덱스는 왼쪽 컬럼부터 순서대로 활용됩니다. 예를 들어 `(schedule_id, seat_row, seat_col)` 인덱스는 `schedule_id` 조건이 있을 때 효율적으로 사용할 수 있지만, `seat_col`만 조건으로 주는 쿼리에는 효과가 제한적입니다.

---

## 4. 성능 최적화

이번 최적화는 현재 서비스에서 실제로 사용 중인 Repository 메서드를 기준으로 최소한의 인덱스를 적용했습니다.

### 4-1. 만료 예약 조회 최적화

예약 만료 스케줄러는 결제 대기 상태이면서 만료 시간이 지난 예약을 조회합니다.

```java
List<Reservation> findAllByStatusAndExpiresAtBefore(
        ReservationStatus status,
        LocalDateTime time
);
```

실행 계획 확인 쿼리는 다음과 같습니다.

```sql
EXPLAIN ANALYZE
SELECT *
FROM reservation
WHERE status = 'PENDING_PAYMENT'
  AND expires_at < NOW();
```

적용한 인덱스는 다음과 같습니다.

```java
@Table(indexes = {
        @Index(name = "idx_reservation_status_expires_at", columnList = "status, expires_at")
})
public class Reservation extends BaseEntity {
}
```

`status`는 동등 조건이고 `expires_at`은 범위 조건이므로 `(status, expires_at)` 순서의 복합 인덱스를 적용했습니다. 이를 통해 전체 예약 테이블을 스캔하지 않고 결제 대기 상태의 만료 예약 범위만 탐색할 수 있습니다.

---

### 4-2. 좌석 중복 조회 최적화

예매 생성 시 같은 상영 일정의 같은 좌석이 이미 선점되었는지 확인합니다.

```java
boolean existsByScheduleIdAndSeatRowAndSeatCol(
        Long scheduleId,
        String seatRow,
        int seatCol
);
```

실행 계획 확인 쿼리는 다음과 같습니다.

```sql
EXPLAIN ANALYZE
SELECT 1
FROM reserved_seat
WHERE schedule_id = 1
  AND seat_row = 'A'
  AND seat_col = 1
LIMIT 1;
```

기존 유니크 제약은 좌석 컬럼이 먼저 오는 순서였습니다.

```java
@UniqueConstraint(
        name = "uk_reserved_seat_per_schedule",
        columnNames = {"seat_row", "seat_col", "schedule_id"}
)
```

하지만 실제 조회 조건은 `schedule_id`를 먼저 기준으로 사용하므로, 다음처럼 인덱스 순서를 변경했습니다.

```java
@UniqueConstraint(
        name = "uk_reserved_seat_per_schedule",
        columnNames = {"schedule_id", "seat_row", "seat_col"}
)
```

이 인덱스는 좌석 중복 방지 역할을 유지하면서, 특정 상영 일정에서 특정 좌석을 찾는 조회에도 더 적합합니다.

---

### 4-3. 예매 좌석 count/delete 최적화

결제 금액 계산 시 예매에 포함된 좌석 수를 조회하고, 예약 취소나 만료 시 해당 예약의 좌석을 삭제합니다.

```java
long countByReservationId(Long reservationId);

void deleteByReservation(Reservation reservation);
```

실행 계획 확인 쿼리는 다음과 같습니다.

```sql
EXPLAIN ANALYZE
SELECT COUNT(*)
FROM reserved_seat
WHERE res_id = 1;
```

```sql
EXPLAIN ANALYZE
DELETE FROM reserved_seat
WHERE res_id = 1;
```

적용한 인덱스는 다음과 같습니다.

```java
@Table(
        indexes = {
                @Index(name = "idx_reserved_seat_reservation_id", columnList = "res_id")
        }
)
public class ReservedSeat extends BaseEntity {
}
```

`reserved_seat`는 좌석 단위로 데이터가 쌓이기 때문에 예약 ID 기준의 count/delete가 반복되면 전체 스캔 비용이 커질 수 있습니다. `res_id` 인덱스를 추가해 특정 예약에 연결된 좌석만 빠르게 찾도록 개선했습니다.

---

### 추가 최적화. 매점 재고 조회 최적화

매점 주문 시 영화관과 상품 기준으로 재고를 조회합니다.

```java
Optional<StoreInventory> findByTheaterIdAndItemId(Long theaterId, Long itemId);
```

실행 계획 확인 쿼리는 다음과 같습니다.

```sql
EXPLAIN ANALYZE
SELECT *
FROM store_inventory
WHERE theater_id = 1
  AND item_id = 1;
```

적용한 인덱스는 다음과 같습니다.

```java
@Table(indexes = {
        @Index(name = "idx_store_inventory_theater_item", columnList = "theater_id, item_id")
})
public class StoreInventory extends BaseEntity {
}
```

영화관별 재고를 상품 단위로 조회하는 패턴이 명확하므로 `(theater_id, item_id)` 복합 인덱스를 적용했습니다.


</details>
