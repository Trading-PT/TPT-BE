# [API 연동] {서비스명} 연동 가이드

> **Version**: 1.0.0
> **Last Updated**: YYYY-MM-DD
> **Author**: {작성자}

---

## 📌 기술 키워드 (Technical Keywords)

| 카테고리 | 키워드 |
|---------|--------|
| **연동 방식** | `REST API`, `Webhook`, `OAuth2`, `API Key`, `HMAC`, `SSL/TLS` |
| **데이터 형식** | `JSON`, `XML`, `Form-Encoded`, `Multipart` |
| **보안** | `HTTPS`, `Signature Verification`, `Token Authentication`, `Rate Limiting` |
| **에러 처리** | `Retry`, `Circuit Breaker`, `Timeout`, `Fallback` |
| **기술 스택** | `Spring Boot`, `Feign Client`, `RestTemplate`, `WebClient` |

---

## 1. 연동 개요 (Integration Overview)

### 1.1 서비스 정보
| 항목 | 내용 |
|------|------|
| **서비스명** | {외부 서비스명} |
| **서비스 유형** | 결제/인증/알림/데이터 등 |
| **연동 목적** | {연동하는 이유와 비즈니스 가치} |
| **API 버전** | v1.0 / v2.0 등 |
| **Base URL (Production)** | `https://api.service.com/v1` |
| **Base URL (Sandbox)** | `https://sandbox-api.service.com/v1` |
| **공식 문서** | [링크](https://docs.service.com) |

### 1.2 연동 범위
- [x] 기능 A: {설명}
- [x] 기능 B: {설명}
- [ ] 기능 C: {미구현 또는 예정}

### 1.3 아키텍처 다이어그램
```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   TPT-API       │────▶│   Gateway/      │────▶│   External      │
│   Application   │◀────│   Adapter       │◀────│   Service       │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                      │                       │
         ▼                      ▼                       ▼
    [내부 로직]           [요청/응답 변환]         [외부 API]
                          [인증 처리]
                          [에러 핸들링]
```

---

## 2. 인증 방식 (Authentication)

### 2.1 인증 유형
- [ ] API Key (Header/Query Parameter)
- [ ] OAuth 2.0 (Client Credentials / Authorization Code)
- [ ] HMAC Signature
- [ ] JWT Token
- [ ] Basic Auth
- [ ] 기타: {설명}

### 2.2 인증 설정
```yaml
# application.yml 설정 예시
external-service:
  api:
    base-url: ${EXTERNAL_API_URL}
    client-id: ${EXTERNAL_CLIENT_ID}
    client-secret: ${EXTERNAL_CLIENT_SECRET}
    timeout:
      connect: 5000
      read: 30000
```

### 2.3 인증 흐름
```
[인증 시퀀스]
1. Client → Server: 인증 정보 전송 (API Key / OAuth Token 요청)
2. Server → External: 인증 검증 또는 토큰 발급 요청
3. External → Server: 인증 결과 / 액세스 토큰 반환
4. Server → Client: 인증 성공 응답
```

### 2.4 시그니처 생성 (해당시)
```java
/**
 * HMAC 시그니처 생성 예시
 */
public String generateSignature(String... params) {
    String data = String.join("", params);
    return DigestUtils.sha256Hex(data);
}
```

---

## 3. API 명세 (API Specification)

### 3.1 API 목록
| API | Method | Endpoint | 용도 |
|-----|--------|----------|------|
| API A | POST | `/api/resource` | 리소스 생성 |
| API B | GET | `/api/resource/{id}` | 리소스 조회 |
| API C | PUT | `/api/resource/{id}` | 리소스 수정 |
| API D | DELETE | `/api/resource/{id}` | 리소스 삭제 |

### 3.2 상세 API 명세

#### 3.2.1 {API 이름}
```http
POST /api/v1/resource HTTP/1.1
Host: api.service.com
Content-Type: application/json
Authorization: Bearer {access_token}

{
    "field1": "value1",
    "field2": "value2"
}
```

**요청 파라미터:**
| 필드 | 타입 | 필수 | 설명 |
|-----|------|-----|------|
| field1 | String | Y | 필드 설명 |
| field2 | String | N | 필드 설명 |

**응답 (성공):**
```json
{
    "resultCode": "0000",
    "resultMsg": "Success",
    "data": {
        "id": "resource_123",
        "status": "CREATED"
    }
}
```

**응답 (실패):**
```json
{
    "resultCode": "E001",
    "resultMsg": "Invalid parameter",
    "errorDetail": "field1 is required"
}
```

---

## 4. 구현 상세 (Implementation Details)

### 4.1 패키지 구조
```
com.tradingpt.tpt_api.global.infrastructure.{service}/
├── client/
│   └── {Service}FeignClient.java       # Feign Client 인터페이스
├── config/
│   └── {Service}Config.java            # 설정 클래스
├── dto/
│   ├── request/
│   │   └── {Operation}RequestDTO.java  # 요청 DTO
│   └── response/
│       └── {Operation}ResponseDTO.java # 응답 DTO
├── exception/
│   ├── {Service}Exception.java         # 커스텀 예외
│   └── {Service}ErrorStatus.java       # 에러 코드
├── service/
│   └── {Service}Service.java           # 서비스 래퍼
└── util/
    └── {Service}CryptoUtil.java        # 암호화/서명 유틸
```

### 4.2 Feign Client 구현
```java
@FeignClient(
    name = "external-service",
    url = "${external-service.api.base-url}",
    configuration = ExternalServiceFeignConfig.class
)
public interface ExternalServiceFeignClient {

    @PostMapping(value = "/api/resource", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseDTO createResource(@RequestBody RequestDTO request);

    @GetMapping("/api/resource/{id}")
    ResponseDTO getResource(@PathVariable("id") String id);
}
```

### 4.3 서비스 래퍼 구현
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalServiceService {

    private final ExternalServiceFeignClient client;
    private final ExternalServiceConfig config;

    /**
     * 리소스 생성 API 호출
     */
    public ResponseDTO createResource(RequestDTO request) {
        log.info("외부 서비스 API 호출: {}", request);

        try {
            ResponseDTO response = client.createResource(request);

            if (response.isSuccess()) {
                log.info("API 호출 성공: {}", response);
                return response;
            } else {
                log.error("API 호출 실패: code={}, msg={}",
                    response.getResultCode(), response.getResultMsg());
                throw new ExternalServiceException(
                    ExternalServiceErrorStatus.fromResultCode(response.getResultCode())
                );
            }
        } catch (FeignException e) {
            log.error("API 통신 오류", e);
            throw new ExternalServiceException(ExternalServiceErrorStatus.API_CONNECTION_FAILED);
        }
    }
}
```

### 4.4 DTO 구현
```java
// Request DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {

    @Schema(description = "필드1 설명", example = "value1")
    private String field1;

    @Schema(description = "필드2 설명", example = "value2")
    private String field2;
}

// Response DTO
@Getter
@Setter
@NoArgsConstructor
public class ResponseDTO {

    private String resultCode;
    private String resultMsg;
    private DataDTO data;

    public boolean isSuccess() {
        return "0000".equals(resultCode);
    }
}
```

---

## 5. 에러 처리 (Error Handling)

### 5.1 에러 코드 매핑
| 외부 에러 코드 | 내부 에러 코드 | HTTP Status | 설명 |
|--------------|--------------|-------------|------|
| E001 | SERVICE4001 | 400 | 잘못된 파라미터 |
| E002 | SERVICE4002 | 401 | 인증 실패 |
| E003 | SERVICE4003 | 403 | 권한 없음 |
| E999 | SERVICE5001 | 500 | 시스템 오류 |

### 5.2 에러 코드 Enum
```java
@Getter
@RequiredArgsConstructor
public enum ExternalServiceErrorStatus implements BaseCodeInterface {

    // 성공
    SUCCESS(HttpStatus.OK, "SERVICE2001", "요청에 성공하였습니다."),

    // 4XX 에러
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "SERVICE4001", "잘못된 파라미터입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "SERVICE4002", "인증에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "SERVICE4003", "권한이 없습니다."),

    // 5XX 에러
    API_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SERVICE5001", "API 연결에 실패했습니다."),
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "SERVICE5002", "요청 시간이 초과되었습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVICE5999", "알 수 없는 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    /**
     * 외부 서비스 에러 코드를 내부 에러 상태로 매핑
     */
    public static ExternalServiceErrorStatus fromResultCode(String resultCode) {
        return switch (resultCode) {
            case "E001" -> INVALID_PARAMETER;
            case "E002" -> AUTHENTICATION_FAILED;
            case "E003" -> UNAUTHORIZED;
            default -> UNKNOWN_ERROR;
        };
    }

    @Override
    public BaseCode getBaseCode() {
        return new BaseCode(httpStatus, code, message);
    }
}
```

### 5.3 재시도 정책
```java
@Configuration
public class RetryConfig {

    @Bean
    public Retryer retryer() {
        // 100ms 초기 간격, 최대 1초, 최대 3회 재시도
        return new Retryer.Default(100, 1000, 3);
    }
}
```

### 5.4 타임아웃 설정
```java
@Configuration
public class FeignConfig {

    @Bean
    public Request.Options options() {
        return new Request.Options(
            5, TimeUnit.SECONDS,    // Connect timeout
            30, TimeUnit.SECONDS,   // Read timeout
            true                     // Follow redirects
        );
    }
}
```

---

## 6. 보안 고려사항 (Security Considerations)

### 6.1 민감 정보 관리
```yaml
# ❌ 절대 금지: 코드에 직접 기재
api-key: "sk_live_xxxxx"

# ✅ 환경 변수 사용
api-key: ${EXTERNAL_API_KEY}
```

### 6.2 통신 보안
- [x] HTTPS 필수 적용
- [x] TLS 1.2 이상 사용
- [x] 인증서 검증 활성화

### 6.3 데이터 보안
```java
// 민감 정보 마스킹
public String maskCardNumber(String cardNo) {
    if (cardNo == null || cardNo.length() < 10) return cardNo;
    return cardNo.substring(0, 6) + "******" + cardNo.substring(cardNo.length() - 4);
}

// 로그에 민감 정보 제외
log.info("결제 요청: cardNo={}", maskCardNumber(request.getCardNo()));
```

### 6.4 Rate Limiting 대응
```java
// Rate Limit 에러 시 지수 백오프 적용
@Retryable(
    value = RateLimitException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public ResponseDTO callApiWithRetry(RequestDTO request) {
    return client.createResource(request);
}
```

---

## 7. 테스트 검증 결과 (Test Verification)

### 7.1 테스트 환경 구성
```yaml
# application-test.yml
external-service:
  api:
    base-url: https://sandbox-api.service.com/v1
    client-id: test_client_id
    client-secret: test_client_secret
```

### 7.2 단위 테스트
```java
@ExtendWith(MockitoExtension.class)
class ExternalServiceServiceTest {

    @Mock
    private ExternalServiceFeignClient client;

    @InjectMocks
    private ExternalServiceService service;

    @Test
    @DisplayName("API 호출 성공 시 응답 반환")
    void createResource_Success() {
        // given
        RequestDTO request = RequestDTO.builder()
            .field1("value1")
            .build();

        ResponseDTO response = new ResponseDTO();
        response.setResultCode("0000");

        when(client.createResource(any())).thenReturn(response);

        // when
        ResponseDTO result = service.createResource(request);

        // then
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("API 호출 실패 시 예외 발생")
    void createResource_Failure() {
        // given
        ResponseDTO response = new ResponseDTO();
        response.setResultCode("E001");
        response.setResultMsg("Invalid parameter");

        when(client.createResource(any())).thenReturn(response);

        // when & then
        assertThrows(ExternalServiceException.class, () -> {
            service.createResource(new RequestDTO());
        });
    }
}
```

### 7.3 통합 테스트
```java
@SpringBootTest
@TestPropertySource(properties = {
    "external-service.api.base-url=https://sandbox-api.service.com/v1"
})
class ExternalServiceIntegrationTest {

    @Autowired
    private ExternalServiceService service;

    @Test
    @DisplayName("실제 API 호출 통합 테스트")
    void integrationTest() {
        // given
        RequestDTO request = createTestRequest();

        // when
        ResponseDTO response = service.createResource(request);

        // then
        assertThat(response.isSuccess()).isTrue();
    }
}
```

### 7.4 테스트 커버리지
| 테스트 유형 | 테스트 케이스 | 결과 | 비고 |
|------------|--------------|------|------|
| 단위 테스트 | 정상 응답 처리 | ✅ Pass | - |
| 단위 테스트 | 에러 응답 처리 | ✅ Pass | 에러 코드별 매핑 검증 |
| 단위 테스트 | 타임아웃 처리 | ✅ Pass | 예외 변환 검증 |
| 통합 테스트 | Sandbox API 호출 | ✅ Pass | 실제 API 통신 검증 |
| E2E 테스트 | 전체 흐름 검증 | ✅ Pass | 프론트엔드 연동 검증 |

---

## 8. 운영 가이드 (Operations Guide)

### 8.1 모니터링
```yaml
# CloudWatch 메트릭
- API 호출 성공률 (Success Rate)
- 평균 응답 시간 (Average Latency)
- 에러 코드별 발생 횟수
- Rate Limit 도달 횟수
```

### 8.2 알람 설정
```yaml
Alarms:
  - Name: ExternalService-ErrorRate
    Metric: ErrorCount / TotalCount
    Threshold: > 5%
    Period: 5 minutes
    Action: SNS Notification

  - Name: ExternalService-Latency
    Metric: p99 Latency
    Threshold: > 5 seconds
    Period: 5 minutes
    Action: SNS Notification
```

### 8.3 트러블슈팅 가이드
| 증상 | 가능한 원인 | 해결 방법 |
|------|-----------|----------|
| 401 에러 | API Key 만료/오류 | 인증 정보 확인 및 갱신 |
| 429 에러 | Rate Limit 초과 | 요청 빈도 조절, 백오프 적용 |
| 500 에러 | 외부 서비스 장애 | 외부 서비스 상태 페이지 확인, 대체 로직 |
| 타임아웃 | 네트워크/서버 지연 | 타임아웃 설정 조정, 재시도 로직 |

---

## 9. 면접 Q&A (Interview Questions)

### Q1. 이 외부 API를 연동하게 된 배경과 기술 선택 이유는?
**A**: {연동 배경, 해당 서비스를 선택한 이유, 대안 비교 분석}

**💡 포인트**:
- 비즈니스 요구사항과 기술적 판단
- 다른 서비스/라이브러리와의 비교 (비용, 기능, 안정성)
- 벤더 종속성(Vendor Lock-in) 고려

---

### Q2. API 연동 시 발생할 수 있는 문제점과 대응 방안은?
**A**: {타임아웃, 에러 응답, Rate Limiting 등의 문제와 각각의 대응 전략}

**💡 포인트**:
- **타임아웃**: 적절한 타임아웃 설정, 비동기 처리
- **에러 처리**: 재시도 정책, Circuit Breaker
- **Rate Limiting**: 지수 백오프, 요청 큐잉
- **가용성**: Fallback 로직, Graceful Degradation

---

### Q3. Feign Client를 선택한 이유는? RestTemplate, WebClient와의 차이점은?
**A**: {각 기술의 특징 비교와 프로젝트에 맞는 선택 이유}

**💡 포인트**:
| 기술 | 장점 | 단점 | 적합한 상황 |
|-----|------|------|-----------|
| Feign | 선언적, 간결함 | 동기식 | MSA, 간단한 API 호출 |
| RestTemplate | 익숙함 | Deprecated 예정 | 레거시 |
| WebClient | 비동기/반응형 | 학습 곡선 | 고성능, 비동기 필요 시 |

---

### Q4. 인증 정보(API Key, Secret)는 어떻게 관리했나요?
**A**: {민감 정보 관리 방법, 환경 변수, AWS Secrets Manager 등}

**💡 포인트**:
- 코드에 하드코딩 금지
- 환경 변수 또는 Secrets Manager 사용
- 로깅 시 마스킹 처리
- 키 로테이션 정책

---

### Q5. API 연동 테스트는 어떻게 진행했나요?
**A**: {테스트 전략, Mock 서버, Sandbox 환경 활용}

**💡 포인트**:
- **단위 테스트**: Mock을 사용한 격리된 테스트
- **통합 테스트**: Sandbox/테스트 환경 활용
- **계약 테스트**: API 스펙 변경 감지
- **부하 테스트**: Rate Limit 검증

---

### Q6. 외부 서비스 장애 시 어떻게 대응하나요?
**A**: {장애 대응 전략, 모니터링, Fallback 로직}

**💡 포인트**:
- Circuit Breaker 패턴 적용
- Fallback 로직 (캐시 데이터 반환, 기본값 제공)
- 알람 및 모니터링 체계
- 장애 복구 후 재처리 로직

---

## 📎 참고 자료 (References)

### 공식 문서
- [외부 서비스 API 문서](https://docs.service.com)
- [외부 서비스 Sandbox 가이드](https://sandbox.service.com)

### 내부 문서
- [프로젝트 아키텍처 문서](./링크)
- [관련 이슈 문서](./링크)

### 기술 레퍼런스
- [Spring Cloud OpenFeign 공식 문서](https://spring.io/projects/spring-cloud-openfeign)
- [Resilience4j Circuit Breaker](https://resilience4j.readme.io/)

---

## 📝 변경 이력 (Change Log)

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|----------|
| 1.0.0 | YYYY-MM-DD | 작성자 | 최초 작성 |
