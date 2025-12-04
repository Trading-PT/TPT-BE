# Spring Security Dual SecurityFilterChain 세션 격리 문제 해결

> **Version**: 1.0.0
> **Last Updated**: 2025-12-03
> **Author**: TPT-API 개발팀

---

## 기술 키워드 (Technical Keywords)

| 카테고리 | 키워드 |
|---------|--------|
| **문제 유형** | `Security`, `Session Management`, `Authorization`, `Configuration` |
| **기술 스택** | `Spring Boot 3.5`, `Spring Security 6`, `Redis Session`, `OAuth2` |
| **해결 기법** | `Root Cause Analysis`, `Architecture Review`, `API Design` |
| **설계 패턴** | `Dual Authentication`, `Filter Chain Pattern`, `Role-Based Access Control` |
| **핵심 개념** | `SecurityFilterChain`, `SecurityContext`, `Session Isolation`, `Request Matcher` |

---

> **작성일**: 2025년 12월
> **프로젝트**: TPT-API (Trading Platform API)
> **도메인**: Spring Security / 인증 및 인가 / 세션 관리
> **심각도**: High

## 목차

1. [문제 발견 배경](#1-문제-발견-배경)
2. [문제 분석](#2-문제-분석)
3. [영향도 분석](#3-영향도-분석)
4. [원인 분석](#4-원인-분석)
5. [해결 방안 탐색](#5-해결-방안-탐색)
6. [최종 해결책](#6-최종-해결책)
7. [성과 및 개선 효과](#7-성과-및-개선-효과)

---

## 1. 문제 발견 배경

### 발견 경위
- **언제**: 트레이너/어드민 로그인 후 피드백 상세 조회 기능 테스트 중
- **어떻게**: 트레이너가 Admin API로 로그인 후, User API 엔드포인트 호출 시 권한 오류 발생
- **증상**:
  - 로컬 환경: `SpelEvaluationException` (Principal 객체가 null)
  - 서버 환경: `ACCESS_DENIED` 응답 (403 Forbidden)

### 환경 정보
- **시스템**: 개발(local) 및 스테이징(server) 환경
- **기술 스택**: Spring Boot 3.5.5, Spring Security 6.x, Redis Session
- **트래픽**: 단일 사용자 테스트 환경

---

## 2. 문제 분석

### 재현 시나리오
```
1. 트레이너가 /api/v1/admin/login 으로 로그인 (Admin SecurityFilterChain 처리)
2. 세션 생성 및 인증 정보 저장
3. 트레이너가 /api/v1/feedback-requests/67 호출 (User SecurityFilterChain으로 라우팅)
4. User SecurityFilterChain에서 해당 세션의 인증 정보를 인식하지 못함
5. 익명 사용자로 처리되어 SpelEvaluationException 또는 ACCESS_DENIED 발생
```

### 에러 로그/증상

**로컬 환경 (SpelEvaluationException)**:
```
org.springframework.expression.spel.SpelEvaluationException:
EL1007E: Property or field 'id' cannot be found on null

at @AuthenticationPrincipal(expression = "id") Long currentUserId
```

**서버 환경 (ACCESS_DENIED)**:
```json
{
  "timestamp": "2025-12-03T10:30:00",
  "code": "AUTH4003",
  "message": "접근 권한이 없습니다.",
  "result": null
}
```

### 문제가 있는 아키텍처

```java
// SecurityConfig.java - 두 개의 분리된 SecurityFilterChain

@Bean
@Order(0)
public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, ...) {
    // Admin 전용 요청 매처: /api/v1/admin/** 경로만 처리
    var adminMatcher = new RegexRequestMatcher("^/api/v1/admin(?:/.*)?$", null);

    http.securityMatcher(adminMatcher)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.NEVER))
        // ... 설정
    return http.build();
}

@Bean
@Order(1)
public SecurityFilterChain userSecurityFilterChain(HttpSecurity http, ...) {
    // User 전용 요청 매처: /api/v1/admin/** 제외한 모든 경로 처리
    var userApiMatcher = new OrRequestMatcher(
        new RegexRequestMatcher("^/api/(?!v1/admin(?:/|$)).*$", null),
        new AntPathRequestMatcher("/oauth2/**"),
        new AntPathRequestMatcher("/login/oauth2/**")
    );

    http.securityMatcher(userApiMatcher)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        // ... 설정
    return http.build();
}
```

**문제 흐름도**:
```
[트레이너 로그인]
    |
    v
POST /api/v1/admin/login
    |
    v
adminSecurityFilterChain 처리
    |
    v
세션 생성 (Admin Context에서)
    |
    v
[피드백 상세 조회 시도]
    |
    v
GET /api/v1/feedback-requests/67
    |
    v
userSecurityFilterChain으로 라우팅 (경로가 admin이 아니므로)
    |
    v
User Chain에서 세션 인식 실패 -> 익명 사용자 처리
    |
    v
SpelEvaluationException 또는 ACCESS_DENIED
```

---

## 3. 영향도 분석

### 비즈니스 영향
- **사용자 영향**: 트레이너/어드민이 User API 엔드포인트에 접근 불가
- **기능 영향**: 피드백 상세 조회, 사용자 정보 조회 등 혼합 API 사용 불가
- **데이터 영향**: 없음 (읽기 전용 조회 실패)

### 기술적 영향
- **성능 저하**: 없음
- **리소스 소비**: 불필요한 에러 로깅 발생
- **확장성 문제**: 향후 Admin/User 간 API 공유 시 지속적 문제 발생

### 심각도 평가
| 항목 | 평가 | 근거 |
|------|------|------|
| **비즈니스 영향** | High | 트레이너 업무 수행에 직접적 지장 |
| **발생 빈도** | 항상 | Admin 로그인 후 User API 호출 시 100% 재현 |
| **복구 난이도** | 보통 | 아키텍처 변경 또는 프론트엔드 수정 필요 |

---

## 4. 원인 분석

### Root Cause (근본 원인)
- **직접적 원인**: 두 SecurityFilterChain이 각각 독립적인 요청 매처를 사용하여 세션 컨텍스트를 공유하지 않음
- **근본 원인**: Dual Authentication 아키텍처 설계 시 크로스 체인 접근 시나리오를 고려하지 않음

### 5 Whys 분석

1. **Why 1**: 왜 트레이너가 피드백 상세 조회 시 권한 오류가 발생하는가?
   - **Answer**: `/api/v1/feedback-requests/{id}` 경로가 userSecurityFilterChain으로 라우팅되기 때문

2. **Why 2**: 왜 userSecurityFilterChain에서 트레이너 세션을 인식하지 못하는가?
   - **Answer**: 트레이너가 adminSecurityFilterChain을 통해 로그인했기 때문

3. **Why 3**: 왜 두 Filter Chain이 세션을 공유하지 않는가?
   - **Answer**: 각 Chain이 독립적인 `securityMatcher`와 세션 정책을 가지고 있기 때문

4. **Why 4**: 왜 독립적인 설정을 사용하는가?
   - **Answer**: User와 Admin 간 권한 에스컬레이션 방지를 위한 의도적 분리 설계

5. **Why 5**: 왜 크로스 체인 접근 시나리오를 고려하지 않았는가?
   - **Answer**: 초기 설계 시 Admin/User API가 완전히 분리될 것으로 가정했으나, 실제 업무 흐름에서 혼합 접근 필요성이 발생

---

## 5. 해결 방안 탐색

### 검토한 해결책들

| 방안 | 설명 | 장점 | 단점 | 복잡도 | 선택 |
|------|------|------|------|--------|------|
| **방안 1: SecurityContextRepository 공유** | 공통 SecurityContextRepository Bean 생성 후 양쪽 Chain에 주입 | 기존 구조 유지<br>코드 변경 최소화 | 근본 해결 안됨 (테스트 결과 실패)<br>Chain별 세션 정책 충돌 | ***** | X |
| **방안 2: 프론트엔드 수정 (단기)** | Admin 로그인 시 Admin API 엔드포인트만 호출하도록 변경 | 백엔드 변경 없음<br>즉시 적용 가능<br>명확한 API 분리 | 프론트엔드 수정 필요<br>중복 API 유지 필요 | ** | 단기 |
| **방안 3: 통합 SecurityFilterChain** | 단일 Filter Chain으로 통합하고 URL 패턴별 권한 설정 | 세션 공유 문제 해결<br>구조 단순화 | 대규모 리팩토링<br>기존 Dual Auth 장점 상실<br>테스트 범위 확대 | ***** | 장기 |
| **방안 4: API Gateway 도입** | 인증을 중앙에서 처리 후 하위 서비스로 전달 | 완전한 분리<br>확장성 우수 | 인프라 비용<br>복잡도 증가<br>개발 기간 | ***** | X |

### 최종 선택 근거
**선택한 방안**: 방안 2 (단기) + 방안 3 (장기 검토)

**이유**:
1. **즉시 해결 필요**: 트레이너 업무에 지장을 주고 있어 빠른 해결 필요
2. **기존 Admin API 활용**: `AdminFeedbackRequestV1Controller`에 이미 동일 기능의 API 존재
3. **아키텍처 무결성 유지**: Dual Authentication의 보안 이점 유지
4. **점진적 개선**: 장기적으로 통합 방안 검토 가능

---

## 6. 최종 해결책

### 구현 개요
기존 Admin API 엔드포인트를 활용하고, User API에서 Admin 권한을 체크하던 데드 코드를 제거하여 명확한 API 분리 원칙을 적용했습니다.

### 변경 사항

#### Before (문제 코드)

```java
// FeedbackRequestQueryServiceImpl.java - 도달 불가능한 코드
private void validateFeedbackAccess(FeedbackRequest feedbackRequest, Long currentUserId) {
    // ... 베스트 피드백 체크 ...

    // DEAD CODE: User SecurityFilterChain에서는 절대 실행되지 않음
    // Admin/Trainer는 Admin Chain으로만 인증되므로 이 코드에 도달할 수 없음
    if (isTrainerOrAdmin(currentUserId)) {
        return; // 모든 피드백 접근 허용
    }

    // ... 나머지 검증 로직 ...
}

private boolean isTrainerOrAdmin(Long userId) {
    // 이 메서드는 User API 컨텍스트에서 호출되므로
    // Admin/Trainer 세션은 인식되지 않아 항상 false 반환
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) return false;
    return user.getRole() == Role.ADMIN || user.getRole() == Role.TRAINER;
}
```

**문제점**:
- `isTrainerOrAdmin()` 메서드는 User SecurityFilterChain 컨텍스트에서 실행
- Admin/Trainer는 이 Chain을 통해 인증되지 않으므로 `currentUserId`가 null이거나 인식 불가
- 결과적으로 이 코드 경로는 절대 실행되지 않는 데드 코드

#### After (개선 코드)

**1. 데드 코드 제거**
```java
// FeedbackRequestQueryServiceImpl.java - 정리된 코드
private void validateFeedbackAccess(FeedbackRequest feedbackRequest, Long currentUserId) {
    // 1. 베스트 피드백은 누구나 접근 가능
    if (Boolean.TRUE.equals(feedbackRequest.getIsBestFeedback())) {
        return;
    }

    // 2. 트레이너가 작성한 피드백도 누구나 접근 가능
    if (Boolean.TRUE.equals(feedbackRequest.getIsTrainerWritten())) {
        return;
    }

    // 3. 비로그인 사용자는 일반 피드백 접근 불가
    if (currentUserId == null) {
        throw new FeedbackRequestException(FeedbackRequestErrorStatus.ACCESS_DENIED);
    }

    // 4. 자신의 피드백인 경우 접근 허용
    if (feedbackRequest.getCustomer().getId().equals(currentUserId)) {
        return;
    }

    // 5. 구독자(PREMIUM)인 경우 모든 피드백 접근 가능
    Customer customer = (Customer) userRepository.findById(currentUserId)
        .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

    if (customer.getMembershipLevel() == MembershipLevel.PREMIUM) {
        return;
    }

    // 6. 그 외의 경우 접근 거부
    throw new FeedbackRequestException(FeedbackRequestErrorStatus.ACCESS_DENIED);
}

// isTrainerOrAdmin() 메서드 삭제 - 데드 코드 제거
```

**2. Admin API 활용 (이미 존재)**
```java
// AdminFeedbackRequestV1Controller.java - 기존 Admin API 활용
@RestController
@RequestMapping("/api/v1/admin/feedback-requests")
public class AdminFeedbackRequestV1Controller {

    @Operation(summary = "피드백 요청 상세 조회 (어드민)")
    @GetMapping("/{feedbackRequestId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
    public BaseResponse<FeedbackRequestDetailResponseDTO> getFeedbackRequestDetail(
        @PathVariable Long feedbackRequestId
    ) {
        return BaseResponse.onSuccess(
            feedbackRequestQueryService.getAdminFeedbackDetail(feedbackRequestId)
        );
    }
}
```

### 주요 설계 결정

**결정 1**: User API에서 Admin 권한 체크 코드 제거
- **선택**: `isTrainerOrAdmin()` 메서드 및 관련 로직 삭제
- **이유**: User SecurityFilterChain에서는 절대 실행되지 않는 데드 코드
- **트레이드오프**: 코드 명확성 향상, 혼란 방지

**결정 2**: Admin/User API 명확한 분리 원칙 적용
- **선택**: 역할에 따라 적절한 API 엔드포인트 호출 필수
- **이유**: Dual SecurityFilterChain 아키텍처의 설계 의도 준수
- **트레이드오프**: 프론트엔드에서 역할 기반 API 호출 로직 필요

**결정 3**: 장기적 통합 방안 검토 예정
- **선택**: 현재 아키텍처 유지, 추후 통합 SecurityFilterChain 검토
- **이유**: 안정성 우선, 점진적 개선 전략
- **트레이드오프**: 일부 API 중복 유지 필요

---

## 7. 성과 및 개선 효과

### 정량적 성과

| 지표 | Before | After | 개선율 |
|------|--------|-------|--------|
| **에러 발생률** | 100% (Admin의 User API 호출 시) | 0% | **-100%** |
| **제거된 데드 코드** | 15줄 | 0줄 | **-15줄** |
| **API 명확성** | 혼란 (역할 무관 호출) | 명확 (역할별 API 분리) | **개선** |

### 정성적 성과
- **명확한 API 설계**: Admin/User API 역할 분리 원칙 확립
- **코드 품질 향상**: 도달 불가능한 데드 코드 제거
- **유지보수성 개선**: 각 SecurityFilterChain의 책임 명확화
- **문서화**: 아키텍처 결정 사항 문서화로 팀 이해도 향상

### 비즈니스 임팩트
- **사용자 경험**: 트레이너가 정상적으로 피드백 조회 가능
- **운영 비용**: 불필요한 에러 로그 감소로 모니터링 효율 향상
- **기술 부채**: 데드 코드 제거로 코드베이스 정리

---

## 8. 테스트 검증 결과 (Test Verification)

### 8.1 수정 전 상태 (Before)
```
[문제 재현 시나리오]
1. POST /api/v1/admin/login (트레이너 계정으로 로그인)
2. GET /api/v1/feedback-requests/67 (User API 호출)
3. 예상 결과: 권한 오류 발생

[실제 결과]
- 로컬: SpelEvaluationException - Property 'id' cannot be found on null
- 서버: HTTP 403 ACCESS_DENIED
```

### 8.2 수정 후 상태 (After)
```
[동일 시나리오 테스트 - Admin API 사용]
1. POST /api/v1/admin/login (트레이너 계정으로 로그인)
2. GET /api/v1/admin/feedback-requests/67 (Admin API 호출)
3. 예상 결과: 정상 조회

[실제 결과]
- HTTP 200 OK
- 피드백 상세 정보 정상 반환
```

### 8.3 테스트 커버리지
| 테스트 유형 | 테스트 케이스 | 결과 | 비고 |
|------------|--------------|------|------|
| 단위 테스트 | Admin API 피드백 상세 조회 | Pass | 트레이너 권한 검증 |
| 통합 테스트 | Admin 로그인 -> Admin API 호출 | Pass | E2E 흐름 검증 |
| 회귀 테스트 | User API 기존 기능 | Pass | Customer 조회 정상 |
| 엣지 케이스 | 비로그인 사용자 베스트 피드백 조회 | Pass | 공개 접근 허용 |

### 8.4 검증 체크리스트
- [x] 문제 상황 재현 후 수정 코드로 해결 확인
- [x] 관련 기능 회귀 테스트 통과
- [x] Admin API 정상 동작 확인
- [x] User API 기존 기능 정상 동작 확인
- [x] 코드 리뷰 완료

---

## 9. 면접 Q&A (Interview Questions)

### Q1. 이 문제를 어떻게 발견하고 분석했나요?
**A**: 트레이너 권한으로 피드백 상세 조회 기능 테스트 중 권한 오류를 발견했습니다. 로컬에서는 SpelEvaluationException, 서버에서는 ACCESS_DENIED가 발생했는데, 에러 메시지와 스택 트레이스를 분석하여 `@AuthenticationPrincipal`에서 principal 객체가 null임을 확인했습니다. Spring Security 디버그 로그를 활성화하여 요청이 어떤 SecurityFilterChain을 통과하는지 추적했고, Admin 로그인 세션이 User FilterChain에서 인식되지 않는 것을 확인했습니다.

**포인트**:
- 로그 분석을 통한 문제 인지
- 디버그 모드를 활용한 Filter Chain 추적
- 요청 흐름 시각화로 근본 원인 파악

---

### Q2. 여러 해결 방안 중 최종 방안을 선택한 이유는?
**A**: 4가지 방안을 검토했습니다. SecurityContextRepository 공유는 테스트 결과 Chain별 세션 정책 충돌로 실패했고, 통합 SecurityFilterChain은 대규모 리팩토링이 필요하며 기존 Dual Auth의 보안 이점을 잃을 수 있었습니다. API Gateway 도입은 인프라 비용과 복잡도가 높았습니다. 최종적으로 이미 존재하는 Admin API를 활용하는 방안을 선택했는데, 즉시 적용 가능하고 아키텍처 무결성을 유지하면서 문제를 해결할 수 있었기 때문입니다.

**포인트**:
- 각 대안의 장단점, 복잡도 비교
- 실제 테스트를 통한 방안 검증
- 비용 대비 효과 분석

---

### Q3. 이 문제의 기술적 근본 원인은 무엇인가요?
**A**: Spring Security의 Dual SecurityFilterChain 아키텍처에서 각 Chain이 독립적인 `securityMatcher`를 사용하기 때문입니다. Admin Chain은 `/api/v1/admin/**` 패턴만, User Chain은 그 외 경로를 처리합니다. Admin으로 로그인하면 Admin Chain에서 세션이 생성되지만, User API 엔드포인트를 호출하면 User Chain으로 라우팅되어 세션 컨텍스트가 공유되지 않습니다. 이는 의도적인 격리 설계이지만, 크로스 체인 접근 시나리오를 고려하지 않은 것이 문제였습니다.

**포인트**:
- SecurityFilterChain의 독립적 동작 원리
- securityMatcher에 의한 요청 라우팅 메커니즘
- 의도적 설계와 예상치 못한 사용 시나리오의 충돌

---

### Q4. 해결 과정에서 어떤 어려움이 있었고, 어떻게 극복했나요?
**A**: 처음에는 SecurityContextRepository를 공유하면 해결될 것으로 예상했지만, 각 Chain의 세션 정책(`NEVER` vs `IF_REQUIRED`)이 충돌하여 실패했습니다. 이후 Spring Security 소스 코드를 분석하여 각 Filter Chain이 독립적인 `SecurityContextHolderFilter`를 통해 컨텍스트를 로드한다는 것을 이해했습니다. 최종적으로 기술적 해결보다 아키텍처적 접근(역할별 API 분리)이 더 적합하다고 판단했습니다.

**포인트**:
- 초기 가설이 실패한 경험과 원인 분석
- 프레임워크 소스 코드 분석을 통한 깊은 이해
- 기술적 해결과 아키텍처적 해결 간의 트레이드오프 판단

---

### Q5. 이 경험에서 배운 점과 재발 방지 대책은?
**A**: 보안 아키텍처 설계 시 모든 사용 시나리오를 사전에 정의해야 한다는 것을 배웠습니다. 재발 방지를 위해 API 설계 문서에 역할별 접근 가능 엔드포인트를 명시하고, 코드 리뷰 시 SecurityFilterChain 경계를 넘는 접근이 없는지 확인하는 체크리스트를 추가했습니다. 또한 도달 불가능한 코드를 사전에 식별하기 위해 정적 분석 도구 도입을 검토 중입니다.

**포인트**:
- 사전 시나리오 정의의 중요성
- 코드 리뷰 체크리스트 강화
- 정적 분석을 통한 데드 코드 탐지

---

### Q6. 유사한 문제를 예방하기 위한 설계 원칙은?
**A**: 첫째, **Single Responsibility Principle**을 API 레벨에 적용하여 Admin API와 User API의 책임을 명확히 분리합니다. 둘째, **Fail-fast 원칙**을 적용하여 잘못된 API 호출 시 명확한 에러 메시지를 반환합니다. 셋째, **Documentation-driven Development**로 API 설계 시 역할별 접근 권한을 먼저 문서화합니다. 마지막으로, 보안 관련 코드는 반드시 **통합 테스트**로 실제 인증 흐름을 검증합니다.

**포인트**:
- API 레벨의 책임 분리 원칙
- 명확한 에러 메시지를 통한 빠른 실패
- 문서 우선 개발 및 통합 테스트 강조

---

## 핵심 교훈 (Key Takeaways)

### 1. SecurityFilterChain 분리 시 크로스 체인 시나리오 고려
- **문제**: Dual SecurityFilterChain 설계 시 크로스 체인 접근을 고려하지 않음
- **교훈**: 보안 아키텍처 설계 시 모든 역할의 모든 API 접근 시나리오를 사전 정의
- **적용**: API 설계 문서에 역할별 접근 가능 엔드포인트 매트릭스 작성

### 2. 데드 코드는 혼란과 버그의 원인
- **문제**: 도달 불가능한 `isTrainerOrAdmin()` 코드가 마치 동작할 것처럼 존재
- **교훈**: 아키텍처를 이해하지 못하면 무의미한 코드를 작성하게 됨
- **적용**: 코드 리뷰 시 실제 실행 가능 여부 검증, 정적 분석 도구 활용

### 3. 기술적 해결보다 아키텍처적 접근이 나을 때가 있다
- **문제**: SecurityContextRepository 공유 등 기술적 해결 시도 실패
- **교훈**: 때로는 아키텍처 설계 의도를 존중하고 그에 맞게 사용하는 것이 최선
- **적용**: 프레임워크/라이브러리의 설계 철학을 이해하고 그에 맞게 활용

---

## 관련 문서

- [SecurityConfig.java](../../src/main/java/com/tradingpt/tpt_api/global/config/SecurityConfig.java)
- [AdminFeedbackRequestV1Controller.java](../../src/main/java/com/tradingpt/tpt_api/domain/feedbackrequest/controller/AdminFeedbackRequestV1Controller.java)
- [FeedbackRequestQueryServiceImpl.java](../../src/main/java/com/tradingpt/tpt_api/domain/feedbackrequest/service/query/FeedbackRequestQueryServiceImpl.java)
- [CLAUDE.md - Security & Sessions](../../CLAUDE.md#security--sessions)

---

## 참고 자료

### Dual SecurityFilterChain 아키텍처 다이어그램
```
                    ┌─────────────────────────────────────────────────────────┐
                    │                    HTTP Request                          │
                    └─────────────────────────────────────────────────────────┘
                                              │
                                              v
                    ┌─────────────────────────────────────────────────────────┐
                    │              FilterChainProxy (Spring Security)          │
                    │                                                          │
                    │   ┌─────────────────────────────────────────────────┐   │
                    │   │            Request Matcher 평가                   │   │
                    │   │   - /api/v1/admin/** -> adminSecurityFilterChain │   │
                    │   │   - 그 외 -> userSecurityFilterChain             │   │
                    │   └─────────────────────────────────────────────────┘   │
                    └─────────────────────────────────────────────────────────┘
                                              │
                    ┌─────────────────┬───────┴───────┬─────────────────┐
                    │                 │               │                 │
                    v                 v               v                 v
    ┌───────────────────────────┐         ┌───────────────────────────┐
    │  adminSecurityFilterChain │         │  userSecurityFilterChain  │
    │       (Order = 0)         │         │       (Order = 1)         │
    ├───────────────────────────┤         ├───────────────────────────┤
    │ SecurityMatcher:          │         │ SecurityMatcher:          │
    │   /api/v1/admin/**        │         │   /api/* (admin 제외)     │
    │                           │         │   /oauth2/**              │
    │ Session Policy: NEVER     │         │ Session Policy: IF_REQUIRED│
    │                           │         │                           │
    │ Auth: ADMIN, TRAINER only │         │ Auth: CUSTOMER only       │
    └───────────────────────────┘         └───────────────────────────┘
              │                                       │
              │                                       │
              v                                       v
    ┌───────────────────────────┐         ┌───────────────────────────┐
    │   Admin 세션 컨텍스트      │         │   User 세션 컨텍스트       │
    │   (독립적으로 관리)        │  ≠≠≠≠≠  │   (독립적으로 관리)        │
    └───────────────────────────┘         └───────────────────────────┘

                    ⚠️ 문제: Admin으로 로그인 후 User API 호출 시
                       User Chain에서 Admin 세션을 인식하지 못함
```

### 해결 후 권장 API 호출 패턴
```
┌─────────────────────────────────────────────────────────────┐
│                    역할별 API 호출 가이드                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  [CUSTOMER 역할]                                             │
│  ├── 로그인: POST /api/v1/auth/login                        │
│  ├── 피드백 목록: GET /api/v1/feedback-requests             │
│  └── 피드백 상세: GET /api/v1/feedback-requests/{id}        │
│                                                              │
│  [TRAINER / ADMIN 역할]                                      │
│  ├── 로그인: POST /api/v1/admin/login                       │
│  ├── 피드백 목록: GET /api/v1/admin/feedback-requests       │
│  └── 피드백 상세: GET /api/v1/admin/feedback-requests/{id}  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

**작성자**: TPT-API 개발팀
**최종 수정일**: 2025년 12월
**버전**: 1.0.0
