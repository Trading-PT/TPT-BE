# TPT-API Codebase Analysis Guide

이 문서는 TPT-API 프로젝트의 코드베이스를 신규 개발자가 빠르게 파악할 수 있도록 실제 코드 패턴과 예시를 정리한 가이드입니다.

---

## 1. 패키지 구조 분석

### 1.1 최상위 패키지 구조

```
src/main/java/com/tradingpt/tpt_api/
├── domain/          # 비즈니스 도메인 (19개 도메인)
├── global/          # 공통 인프라 및 설정
└── TradingPtApplication.java
```

### 1.2 도메인 패키지 구조 (Domain-Driven Design)

각 도메인은 독립적이고 완결된 구조를 가지며, 다음과 같은 표준 패키지 구조를 따릅니다:

```
domain/{domain-name}/
├── controller/      # REST API 컨트롤러
├── service/         # 비즈니스 로직
│   ├── command/     # CUD 작업 (생성, 수정, 삭제)
│   └── query/       # 읽기 작업
├── repository/      # 데이터 접근 계층
├── dto/             # 데이터 전송 객체
│   ├── request/     # 요청 DTO
│   └── response/    # 응답 DTO
├── entity/          # JPA 엔티티
├── enums/           # 도메인 특화 열거형 (선택)
├── exception/       # 도메인 특화 예외
├── infrastructure/  # 외부 서비스 연동 (선택, auth 도메인)
├── handler/         # 이벤트 핸들러 (선택, auth 도메인)
├── filter/          # 커스텀 필터 (선택, auth 도메인)
├── scheduler/       # 스케줄러 (선택, lecture 도메인)
└── util/            # 도메인 유틸리티 (선택)
```

**실제 예시 - Memo 도메인 (심플한 구조)**

```
domain/memo/
├── controller/
│   └── MemoV1Controller.java
├── service/
│   ├── command/
│   │   ├── MemoCommandService.java (인터페이스)
│   │   └── MemoCommandServiceImpl.java (구현체)
│   └── query/
│       ├── MemoQueryService.java (인터페이스)
│       └── MemoQueryServiceImpl.java (구현체)
├── repository/
│   └── MemoRepository.java
├── dto/
│   ├── request/
│   │   └── MemoRequestDTO.java
│   └── response/
│       └── MemoResponseDTO.java
├── entity/
│   └── Memo.java
└── exception/
    ├── MemoErrorStatus.java
    └── MemoException.java
```

**실제 예시 - Auth 도메인 (복잡한 구조)**

```
domain/auth/
├── controller/
│   ├── AuthController.java (일반 사용자)
│   └── AdminAuthController.java (관리자)
├── service/
│   ├── AuthService.java
│   └── CustomRememberMeService.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── CustomOAuth2UserService.java
│   └── AuthSessionUser.java
├── filter/
│   ├── JsonUsernamePasswordAuthFilter.java
│   ├── AdminJsonUsernamePasswordAuthFilter.java
│   └── CsrfTokenResponseHeaderBindingFilter.java
├── handler/
│   ├── CustomSuccessHandler.java
│   ├── CustomFailureHandler.java
│   └── AdminSuccessHandler.java
├── infrastructure/
│   └── sms/
│       └── SmsService.java
├── repository/
│   └── HttpCookieOAuth2AuthorizationRequestRepository.java
├── dto/
│   ├── request/
│   │   ├── SignUpRequestDTO.java
│   │   ├── SendPhoneCodeRequestDTO.java
│   │   └── ...
│   └── response/
│       ├── MeResponse.java
│       └── ...
└── exception/
    └── code/
        └── AuthErrorStatus.java
```

**실제 예시 - Lecture 도메인 (QueryDSL 사용)**

```
domain/lecture/
├── controller/
│   ├── LectureV1Controller.java (사용자용)
│   └── AdminLectureV1Controller.java (관리자용)
├── service/
│   ├── command/
│   │   ├── LectureCommandService.java
│   │   ├── LectureCommandServiceImpl.java
│   │   ├── AdminLectureCommandService.java
│   │   ├── AdminLectureCommandServiceImpl.java
│   │   └── LectureOpenService.java (스케줄링용)
│   └── query/
│       ├── LectureQueryService.java
│       ├── LectureQueryServiceImpl.java
│       ├── AdminLectureQueryService.java
│       └── AdminLectureQueryServiceImpl.java
├── repository/
│   ├── LectureRepository.java (JPA 기본)
│   ├── LectureRepositoryCustom.java (QueryDSL 인터페이스)
│   ├── LectureRepositoryImpl.java (QueryDSL 구현체)
│   ├── ChapterRepository.java
│   └── LectureProgressRepository.java
├── scheduler/
│   └── LectureOpenScheduler.java (ShedLock)
└── ...
```

### 1.3 Global 패키지 구조

```
global/
├── config/          # Spring 설정 클래스
│   ├── SecurityConfig.java (보안 설정)
│   ├── RedisSessionConfig.java (세션 설정)
│   ├── QueryDslConfig.java (QueryDSL 설정)
│   ├── S3Config.java (AWS S3 설정)
│   ├── SchedulerConfig.java (ShedLock 설정)
│   ├── SwaggerConfig.java (API 문서)
│   ├── CorsConfig.java (CORS 설정)
│   ├── MailConfig.java (이메일 설정)
│   └── ...
├── exception/       # 전역 예외 처리
│   ├── GlobalExceptionHandler.java
│   ├── BaseException.java
│   ├── AuthException.java
│   └── code/
│       ├── BaseCodeInterface.java
│       ├── BaseCode.java
│       └── GlobalErrorStatus.java
├── common/          # 공통 DTO 및 엔티티
│   ├── BaseResponse.java (표준 API 응답)
│   └── BaseEntity.java (공통 엔티티)
├── security/        # 보안 관련 공통 클래스
│   ├── handler/
│   │   ├── JsonAuthenticationEntryPoint.java
│   │   └── JsonAccessDeniedHandler.java
│   └── csrf/
│       └── HeaderAndCookieCsrfTokenRepository.java
├── infrastructure/  # 외부 서비스 통합
│   ├── s3/
│   │   ├── S3Service.java
│   │   └── S3Client.java
│   ├── nicepay/     # 결제 통합
│   └── content/     # 콘텐츠 처리
├── util/            # 공통 유틸리티
└── web/             # 웹 계층 공통 설정
    ├── cookie/
    │   └── CookieProps.java
    └── logout/
        └── LogoutHelper.java
```

### 1.4 도메인 간 의존성 패턴

**의존성 원칙**:
- 도메인은 자신의 패키지 내부와 `global/` 패키지만 의존
- 다른 도메인 참조 시 entity를 직접 참조 (예: `domain.user.entity.Customer`)
- 도메인 간 순환 참조 금지

**실제 예시**:

```java
// ✅ 올바른 의존성: memo 도메인에서 user 엔티티 참조
package com.tradingpt.tpt_api.domain.memo.service.command;

import com.tradingpt.tpt_api.domain.user.entity.Customer;  // ✅ 엔티티 직접 참조
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;  // ✅ 리포지토리 참조
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;  // ✅ 예외 참조
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.global.common.BaseResponse;  // ✅ global 참조

@Service
@RequiredArgsConstructor
public class MemoCommandServiceImpl implements MemoCommandService {
    private final CustomerRepository customerRepository;

    @Override
    public MemoResponseDTO createMemo(Long customerId, MemoRequestDTO request) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));
        // ...
    }
}
```

---

## 2. 네이밍 컨벤션 분석

### 2.1 Controller 클래스 네이밍

**패턴**: `{엔티티명}{역할}{버전}Controller`

**예시**:

```java
// 사용자용 API (일반)
@RestController
@RequestMapping("/api/v1/memos")
@Tag(name = "메모 (Memo)", description = "마이페이지 메모 관리 API")
public class MemoV1Controller { }

@RestController
@RequestMapping("/api/v1/lectures")
public class LectureV1Controller { }

// 관리자용 API (Admin 접두사)
@RestController
@RequestMapping("/api/v1/admin/lectures")
public class AdminLectureV1Controller { }

@RestController
@RequestMapping("/api/v1/admin/feedbacks")
public class AdminFeedbackRequestV1Controller { }

// 인증 관련 (버전 없음)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController { }

@RestController
@RequestMapping("/api/v1/admin")
public class AdminAuthController { }
```

**규칙**:
- `V1`, `V2` 등 API 버전 명시
- 관리자용은 `Admin` 접두사 사용
- `@RestController` + `@RequestMapping` 조합
- Swagger `@Tag` 어노테이션으로 API 그룹화

### 2.2 Service 클래스 네이밍

**패턴 (CQRS 패턴)**: Command와 Query 분리

**Query Service (읽기 전용)**:
```java
// 인터페이스
public interface MemoQueryService {
    MemoResponseDTO getMemo(Long customerId);
}

// 구현체
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // ✅ readOnly 명시
public class MemoQueryServiceImpl implements MemoQueryService {
    private final MemoRepository memoRepository;

    @Override
    public MemoResponseDTO getMemo(Long customerId) {
        // 조회 로직
    }
}
```

**Command Service (생성/수정/삭제)**:
```java
// 인터페이스
public interface MemoCommandService {
    MemoResponseDTO createOrUpdateMemo(Long customerId, MemoRequestDTO request);
    void deleteMemo(Long customerId);
}

// 구현체
@Service
@RequiredArgsConstructor
public class MemoCommandServiceImpl implements MemoCommandService {
    private final MemoRepository memoRepository;

    @Override
    @Transactional  // ✅ 쓰기 트랜잭션
    public MemoResponseDTO createOrUpdateMemo(Long customerId, MemoRequestDTO request) {
        // 생성/수정 로직
    }
}
```

**예외 패턴 (단일 Service)**:
```java
// 특수 목적 Service는 Command/Query 분리 없이 단일 클래스
@Service
public class AuthService { }

@Service
public class UserService { }

@Service
public class LectureOpenService { }  // 스케줄링 전용
```

**규칙**:
- 인터페이스 + 구현체(`Impl`) 패턴
- Query Service는 `@Transactional(readOnly = true)`
- Command Service는 `@Transactional`
- 관리자용 Service는 `Admin` 접두사

### 2.3 Repository 클래스 네이밍

**기본 JPA Repository**:
```java
public interface MemoRepository extends JpaRepository<Memo, Long> {
    Optional<Memo> findByCustomer_Id(Long customerId);
    boolean existsByCustomer_Id(Long customerId);
}
```

**QueryDSL 확장 (Custom Repository)**:
```java
// 1. Custom 인터페이스 정의
public interface LectureRepositoryCustom {
    List<ChapterBlockDTO> findCurriculum(Long userId, int page, int size);
}

// 2. JPA Repository에서 상속
public interface LectureRepository
    extends JpaRepository<Lecture, Long>, LectureRepositoryCustom {

    Page<Lecture> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT l FROM Lecture l JOIN FETCH l.chapter c ...")
    List<Lecture> findAllOrderByChapterAndLectureOrder();
}

// 3. QueryDSL 구현체
@Repository
@RequiredArgsConstructor
public class LectureRepositoryImpl implements LectureRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChapterBlockDTO> findCurriculum(Long userId, int page, int size) {
        // QueryDSL 복잡 쿼리
        List<Tuple> rows = queryFactory
            .select(chapter.id, chapter.title, lecture.id, ...)
            .from(chapter)
            .join(lecture).on(lecture.chapter.eq(chapter))
            .leftJoin(lectureProgress).on(...)
            .fetch();
        // ...
    }
}
```

**규칙**:
- `{Entity}Repository` 네이밍
- QueryDSL 사용 시: `{Entity}RepositoryCustom` + `{Entity}RepositoryImpl`
- `Impl` 구현체는 `@Repository` 어노테이션 필수

### 2.4 DTO 클래스 네이밍

**Request DTO**:
```java
@Getter
@Schema(description = "메모 생성/수정 요청 DTO")
public class MemoRequestDTO {

    @NotBlank(message = "메모 제목은 필수입니다.")
    @Size(max = 100, message = "메모 제목은 100자를 초과할 수 없습니다.")
    @Schema(description = "메모 제목", example = "오늘의 트레이딩 포인트")
    private String title;

    @NotBlank(message = "메모 내용은 필수입니다.")
    @Size(max = 5000, message = "메모 내용은 5000자를 초과할 수 없습니다.")
    @Schema(description = "메모 내용", example = "- 손절가 설정 잊지 말기")
    private String content;
}
```

**Response DTO (일반 클래스)**:
```java
@Getter
@Builder
@Schema(description = "메모 응답 DTO")
public class MemoResponseDTO {

    @Schema(description = "메모 ID", example = "1")
    private Long id;

    @Schema(description = "메모 제목", example = "오늘의 트레이딩 포인트")
    private String title;

    @Schema(description = "메모 내용")
    private String content;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    /**
     * Memo 엔티티를 MemoResponseDTO로 변환
     */
    public static MemoResponseDTO from(Memo memo) {
        return MemoResponseDTO.builder()
            .id(memo.getId())
            .title(memo.getTitle())
            .content(memo.getContent())
            .createdAt(memo.getCreatedAt())
            .updatedAt(memo.getUpdatedAt())
            .build();
    }
}
```

**Response DTO (Record 사용 예시)**:
```java
// record는 간단한 DTO에서 사용
public record SocialInfoResponse(
    Long userId,
    String username,
    String name,
    String email,
    String password
) {}
```

**규칙**:
- Request DTO: `{Operation}{Entity}RequestDTO` (예: `CreateMemoRequestDTO`, `UpdateLectureRequestDTO`)
  - 또는 범용: `{Entity}RequestDTO`
- Response DTO: `{Entity}ResponseDTO`
- DTO는 `@Getter` + `@Builder` 조합 (또는 record)
- Swagger `@Schema` 어노테이션으로 문서화
- Validation 어노테이션 사용 (`@NotBlank`, `@Size`, `@Email` 등)
- Response DTO는 `from()` 정적 팩토리 메서드 제공

### 2.5 Entity 클래스 네이밍

**기본 패턴**:
```java
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "memo")
public class Memo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 메모 내용 업데이트
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
```

**상속 구조 (User 엔티티)**:
```java
@Entity
@Table(name = "user")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    // 비즈니스 로직 메서드
    public void changePassword(String password) {
        this.password = password;
    }

    // 추상 메서드
    public abstract Role getRole();
}
```

**규칙**:
- 엔티티명과 동일한 클래스명
- `@SuperBuilder` + `@NoArgsConstructor(PROTECTED)` + `@AllArgsConstructor`
- `BaseEntity` 상속으로 `createdAt`, `updatedAt` 자동 관리
- ID 필드는 `{entity}_id` 형식
- 비즈니스 로직은 엔티티 내부 메서드로 구현
- `@Getter`만 사용, Setter 금지 (불변성 유지)

### 2.6 Exception 클래스 네이밍

**도메인 예외 클래스**:
```java
package com.tradingpt.tpt_api.domain.memo.exception;

/**
 * 메모 도메인 전용 예외 클래스
 */
public class MemoException extends BaseException {
    public MemoException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
```

**에러 상태 코드 (ErrorStatus enum)**:
```java
@Getter
@AllArgsConstructor
public enum MemoErrorStatus implements BaseCodeInterface {

    // 메모 6000번대 에러
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO6001", "메모를 찾을 수 없습니다."),
    MEMO_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMO6002", "이미 메모가 존재합니다."),
    MEMO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEMO6003", "메모에 접근 권한이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCode getCode() {
        return BaseCode.builder()
            .httpStatus(httpStatus)
            .isSuccess(isSuccess)
            .code(code)
            .message(message)
            .build();
    }
}
```

**사용 예시**:
```java
@Service
public class MemoQueryServiceImpl implements MemoQueryService {

    @Override
    public MemoResponseDTO getMemo(Long customerId) {
        Memo memo = memoRepository.findByCustomer_Id(customerId)
            .orElseThrow(() -> new MemoException(MemoErrorStatus.MEMO_NOT_FOUND));
        return MemoResponseDTO.from(memo);
    }
}
```

**규칙**:
- 도메인별 Exception: `{Domain}Exception extends BaseException`
- 에러 코드: `{Domain}ErrorStatus enum implements BaseCodeInterface`
- 에러 코드 포맷: `{DOMAIN}{번호}` (예: `MEMO6001`, `USER4001`)
- 도메인별 에러 코드 범위:
  - AUTH: 1000번대
  - USER: 4000번대
  - MEMO: 6000번대
  - LECTURE: 7000번대
  - (각 도메인별로 고유 번호 대역 할당)

---

## 3. 전역 에러 핸들링 구조

### 3.1 에러 처리 계층 구조

```
에러 발생
    ↓
BaseException (도메인 예외)
    ↓
GlobalExceptionHandler (@RestControllerAdvice)
    ↓
BaseResponse<T> (표준 응답 포맷)
    ↓
클라이언트
```

### 3.2 GlobalExceptionHandler 주요 메서드

**파일 위치**: `/global/exception/GlobalExceptionHandler.java`

**주요 예외 처리**:

```java
@RestControllerAdvice(annotations = {RestController.class})
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 1) 비즈니스/도메인 예외 처리
    @ExceptionHandler(value = BaseException.class)
    public ResponseEntity<BaseResponse<String>> handleRestApiException(BaseException e) {
        BaseCodeInterface errorCode = e.getErrorCodeInterface();
        log.error("[handleRestApiException] Domain Exception: {}", e.getMessage(), e);
        return handleExceptionInternal(errorCode);
    }

    // 2) JSON 파싱 오류
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(...) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.of(GlobalErrorStatus.INVALID_REQUEST_BODY, null));
    }

    // 3) Bean Validation 실패 (상세 처리)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(...) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.merge(fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        (oldVal, newVal) -> oldVal + ", " + newVal);
        });
        return handleExceptionInternalArgs(GlobalErrorStatus.VALIDATION_ERROR, errors);
    }

    // 4) Spring Security 인증 실패
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<String>> handleAuthenticationException(...) {
        return handleExceptionInternal(GlobalErrorStatus._UNAUTHORIZED);
    }

    // 5) Spring Security 권한 부족
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<String>> handleAccessDeniedException(...) {
        return handleExceptionInternal(GlobalErrorStatus._FORBIDDEN);
    }

    // 6) 데이터베이스 제약조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleDataIntegrityViolation(...) {
        String message = "데이터 무결성 제약조건을 위반했습니다.";
        if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
            message = "중복된 데이터가 존재합니다.";
        }
        return handleExceptionInternal(GlobalErrorStatus.CONFLICT, message);
    }

    // 7) 모든 예상치 못한 예외 (최종 안전망)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleException(Exception e) {
        log.error("[handleException] Unexpected error occurred: {}", e.getMessage(), e);
        return handleExceptionInternalFalse(
            GlobalErrorStatus._INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요."
        );
    }
}
```

### 3.3 BaseException 계층 구조

**파일 위치**: `/global/exception/BaseException.java`

```java
/**
 * 기본 예외 클래스 - 모든 커스텀 예외의 부모
 */
public class BaseException extends RuntimeException {
    private final BaseCodeInterface errorCode;

    public BaseException(BaseCodeInterface errorCode) {
        super(errorCode.getCode().getMessage());
        this.errorCode = errorCode;
    }

    public BaseCode getErrorCode() {
        return errorCode.getCode();
    }

    public BaseCodeInterface getErrorCodeInterface() {
        return errorCode;
    }
}
```

**도메인별 Exception 확장**:
```
BaseException (추상)
    ├── AuthException
    ├── UserException
    ├── MemoException
    ├── LectureException
    └── ...
```

### 3.4 에러 코드 인터페이스 구조

**BaseCodeInterface** (인터페이스):
```java
public interface BaseCodeInterface {
    BaseCode getCode();
}
```

**BaseCode** (값 객체):
```java
@Getter
@Builder
public class BaseCode {
    private final HttpStatus httpStatus;
    private final boolean isSuccess;
    private final String code;
    private final String message;
}
```

**GlobalErrorStatus** (전역 에러):
```java
@Getter
@AllArgsConstructor
public enum GlobalErrorStatus implements BaseCodeInterface {

    // HTTP 상태 기반 에러
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러"),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 세부 에러
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON4001", "잘못된 파라미터"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON4005", "입력값 검증 실패"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON4007", "리소스를 찾을 수 없음"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5001", "데이터베이스 오류"),
    ;

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCode getCode() {
        return BaseCode.builder()
            .httpStatus(httpStatus)
            .isSuccess(isSuccess)
            .code(code)
            .message(message)
            .build();
    }
}
```

### 3.5 에러 응답 포맷 (BaseResponse)

**파일 위치**: `/global/common/BaseResponse.java`

```java
@Getter
@AllArgsConstructor
@JsonPropertyOrder({"timestamp", "code", "message", "result"})
@Schema(description = "공통 응답 DTO")
public class BaseResponse<T> {

    @Schema(description = "응답 시간", example = "2021-07-01T00:00:00")
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "응답 코드", example = "200")
    private final String code;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "응답 데이터")
    private T result;

    // 성공 응답
    public static <T> BaseResponse<T> onSuccess(T result) {
        return new BaseResponse<>("COMMON200", "요청에 성공하였습니다.", result);
    }

    public static <T> BaseResponse<T> onSuccessCreate(T result) {
        return new BaseResponse<>("COMMON201", "요청에 성공하였습니다.", result);
    }

    public static <T> BaseResponse<T> onSuccessDelete(T result) {
        return new BaseResponse<>("COMMON202", "삭제 요청에 성공하였습니다.", result);
    }

    // 실패 응답
    public static <T> BaseResponse<T> onFailure(BaseCodeInterface code, T result) {
        return new BaseResponse<>(
            code.getCode().getCode(),
            code.getCode().getMessage(),
            result
        );
    }
}
```

**성공 응답 예시**:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "code": "COMMON200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "id": 1,
    "title": "메모 제목",
    "content": "메모 내용"
  }
}
```

**에러 응답 예시**:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "code": "MEMO6001",
  "message": "메모를 찾을 수 없습니다.",
  "result": null
}
```

**Validation 에러 응답 예시**:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "code": "COMMON4005",
  "message": "입력값 검증에 실패했습니다.",
  "result": {
    "title": "메모 제목은 필수입니다.",
    "content": "메모 내용은 5000자를 초과할 수 없습니다."
  }
}
```

### 3.6 예외 발생 코드 스타일

**Service 계층에서 예외 발생**:
```java
@Service
@RequiredArgsConstructor
public class MemoCommandServiceImpl implements MemoCommandService {

    private final MemoRepository memoRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public MemoResponseDTO createMemo(Long customerId, MemoRequestDTO request) {
        // ✅ 예외 발생 패턴
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        // ✅ 조건부 예외
        if (memoRepository.existsByCustomer_Id(customerId)) {
            throw new MemoException(MemoErrorStatus.MEMO_ALREADY_EXISTS);
        }

        Memo memo = Memo.builder()
            .customer(customer)
            .title(request.getTitle())
            .content(request.getContent())
            .build();

        return MemoResponseDTO.from(memoRepository.save(memo));
    }
}
```

**Controller 계층에서 예외 응답 반환 (직접 처리)**:
```java
@RestController
public class AuthController {

    @PostMapping("/id/find")
    public ResponseEntity<BaseResponse<FindIdResponseDTO>> findIdByEmail(...) {
        FindIdResponseDTO response = userService.findUserId(req.getEmail());

        // ✅ null 체크 후 직접 실패 응답 반환
        if (response == null) {
            return ResponseEntity
                .status(UserErrorStatus.USER_NOT_FOUND.getCode().getHttpStatus())
                .body(BaseResponse.onFailure(UserErrorStatus.USER_NOT_FOUND, null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.onSuccessCreate(response));
    }
}
```

**규칙**:
- Service 계층: `throw new {Domain}Exception({Domain}ErrorStatus.XXX)`
- Repository 조회 실패: `.orElseThrow(() -> new ...)`
- Controller 계층: 예외는 GlobalExceptionHandler가 자동 처리
- 직접 실패 응답 반환 시: `BaseResponse.onFailure()` 사용

---

## 4. 인증/인가 구조 분석

### 4.1 Dual Authentication System (이중 인증 시스템)

**개요**: 일반 사용자(CUSTOMER)와 관리자/트레이너(ADMIN/TRAINER)를 별도로 인증

**SecurityConfig 핵심 구조**:
```java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // 1. 사용자 전용 AuthenticationProvider
    @Bean(name = "userAuthProvider")
    public AuthenticationProvider userAuthProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(username -> {
            UserDetails u = userDetailsService.loadUserByUsername(username);
            // ✅ ADMIN/TRAINER는 사용자 로그인 불가
            for (GrantedAuthority a : u.getAuthorities()) {
                if ("ROLE_ADMIN".equals(a.getAuthority()) ||
                    "ROLE_TRAINER".equals(a.getAuthority())) {
                    throw new UsernameNotFoundException("User-only login endpoint");
                }
            }
            return u;
        });
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    // 2. 사용자 전용 AuthenticationManager
    @Bean(name = "userAuthenticationManager")
    public AuthenticationManager userAuthenticationManager(
            @Qualifier("userAuthProvider") AuthenticationProvider userProvider) {
        return new ProviderManager(List.of(userProvider));
    }

    // 3. 관리자 전용 AuthenticationProvider
    @Bean(name = "adminAuthProvider")
    public AuthenticationProvider adminAuthProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(username -> {
            UserDetails u = userDetailsService.loadUserByUsername(username);
            // ✅ ADMIN/TRAINER만 관리자 로그인 가능
            if (!hasAdminOrTrainer(u.getAuthorities())) {
                throw new UsernameNotFoundException("Admin-only login endpoint");
            }
            return u;
        });
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    // 4. 관리자 전용 AuthenticationManager
    @Bean(name = "adminAuthenticationManager")
    public AuthenticationManager adminAuthenticationManager(
            @Qualifier("adminAuthProvider") AuthenticationProvider adminProvider) {
        return new ProviderManager(List.of(adminProvider));
    }
}
```

### 4.2 일반 로그인/회원가입 플로우

**1단계: 휴대폰 인증 (SMS)**

```java
@PostMapping("/phone/code")
public ResponseEntity<BaseResponse<Void>> sendPhoneCode(
    @Valid @RequestBody SendPhoneCodeRequestDTO req,
    HttpSession session) {
    authService.sendPhoneCode(req, session);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.onSuccessCreate(null));
}

@PostMapping("/phone/verify")
public ResponseEntity<BaseResponse<Void>> verifyPhone(
    @Valid @RequestBody VerifyCodeRequestDTO req,
    HttpSession session) {
    authService.verifyPhoneCode(req, session);  // 세션에 인증 완료 플래그 저장
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.onSuccessCreate(null));
}
```

**2단계: 회원가입**

```java
@PostMapping("/signup")
public ResponseEntity<BaseResponse<Void>> signUp(
    @Valid @RequestBody SignUpRequestDTO req,
    HttpSession session) {
    authService.signUp(req, session);  // 세션에서 인증 플래그 확인
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.onSuccessCreate(null));
}
```

**3단계: 로그인 (JSON 필터)**

```java
// JsonUsernamePasswordAuthFilter가 /api/v1/auth/login 요청 가로채기
@Bean
public JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter(
        @Qualifier("userAuthenticationManager") AuthenticationManager userAuthManager) {
    var filter = new JsonUsernamePasswordAuthFilter(objectMapper);
    filter.setFilterProcessesUrl("/api/v1/auth/login");  // ✅ 사용자 로그인 엔드포인트
    filter.setAuthenticationManager(userAuthManager);    // ✅ 사용자 전용 매니저
    filter.setAuthenticationSuccessHandler(customSuccessHandler);
    filter.setAuthenticationFailureHandler(customFailureHandler);
    filter.setRememberMeServices(rememberMeServices);
    return filter;
}
```

**JSON 로그인 요청 예시**:
```json
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "password123"
}
```

### 4.3 OAuth2 로그인/회원가입 플로우

**OAuth2 설정 (Kakao/Naver)**:

```java
@Bean
@Order(1)
public SecurityFilterChain userSecurityFilterChain(...) {
    http
        .oauth2Login(o -> o
            .authorizationEndpoint(ae ->
                ae.authorizationRequestRepository(cookieAuthRequestRepository))
            .userInfoEndpoint(ui ->
                ui.userService(customOAuth2UserService))  // ✅ 사용자 정보 로드
            .successHandler(customSuccessHandler)
            .failureHandler(customFailureHandler)
        );
    return http.build();
}
```

**OAuth2 사용자 정보 로드**:
```java
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<...> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // 1. OAuth2 제공자에서 사용자 정보 가져오기
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 제공자 구분 (KAKAO/NAVER)
        String registrationId = userRequest.getClientRegistration()
            .getRegistrationId().toUpperCase();

        // 3. 사용자 정보 추출 및 DB 저장
        User user = processOAuth2User(registrationId, oAuth2User);

        // 4. Spring Security Authentication 객체 생성
        return new AuthSessionUser(user.getId(), user.getUsername(), ...);
    }
}
```

**OAuth2 로그인 URL**:
```
GET /oauth2/authorization/kakao
GET /oauth2/authorization/naver
```

### 4.4 CSRF 토큰 발급 및 검증

**CSRF 토큰 Repository (Cookie + Header)**:

```java
@Bean
public HeaderAndCookieCsrfTokenRepository csrfTokenRepository() {
    HeaderAndCookieCsrfTokenRepository repo = new HeaderAndCookieCsrfTokenRepository();
    var cookieProps = serverProperties.getServlet().getSession().getCookie();

    // 쿠키 설정
    if (cookieProps.getDomain() != null)
        repo.setCookieDomain(cookieProps.getDomain());
    repo.setCookieHttpOnly(false);  // ✅ JavaScript에서 읽기 가능
    repo.setCookieCustomizer(builder -> {
        builder.secure(cookieProps.getSecure());
        builder.sameSite("Lax");
    });
    return repo;
}
```

**CSRF 필터 (응답 헤더에 토큰 추가)**:
```java
public class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(...) {
        CsrfToken token = csrfTokenRepository.loadToken(request);
        if (token != null) {
            // ✅ 응답 헤더에 CSRF 토큰 추가
            response.setHeader("X-CSRF-TOKEN", token.getToken());
        }
        filterChain.doFilter(request, response);
    }
}
```

**CSRF 보호 제외 경로**:
```java
// 사용자 Security Chain
http.csrf(csrf -> csrf
    .ignoringRequestMatchers("/api/v1/auth/**", "/oauth2/**", "/login/**")
    .csrfTokenRepository(csrfTokenRepository)
);

// 관리자 Security Chain
http.csrf(csrf -> csrf
    .ignoringRequestMatchers("/api/v1/admin/login")
    .csrfTokenRepository(csrfTokenRepository)
);
```

**클라이언트 사용법**:
```javascript
// 1. GET 요청으로 CSRF 토큰 받기
const response = await fetch('/api/v1/some-endpoint');
const csrfToken = response.headers.get('X-CSRF-TOKEN');

// 2. POST 요청 시 토큰 포함
await fetch('/api/v1/memos', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': csrfToken  // ✅ 헤더에 토큰 포함
    },
    body: JSON.stringify({ title: '...', content: '...' })
});
```

### 4.5 Session 관리 (Redis 기반)

**RedisSessionConfig**:
```java
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 604800)  // 7일
public class RedisSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(ServerProperties serverProperties) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        var cookieProps = serverProperties.getServlet().getSession().getCookie();
        serializer.setCookieName(cookieProps.getName());  // "SESSION"
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(cookieProps.getSecure());
        serializer.setSameSite("Lax");

        return serializer;
    }
}
```

**세션 동시 접속 제어**:
```java
@Bean
public SessionRegistry sessionRegistry(
        FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
    return new SpringSessionBackedSessionRegistry<>(sessionRepository);
}

// 사용자: 최대 3개 세션
http.sessionManagement(sm -> sm
    .sessionConcurrency(sc -> sc.maximumSessions(3).sessionRegistry(sessionRegistry))
);

// 관리자: 최대 1개 세션
http.sessionManagement(sm -> sm
    .sessionConcurrency(sc -> sc.maximumSessions(1).sessionRegistry(sessionRegistry))
);
```

### 4.6 Remember-Me 기능

**RememberMeConfig**:
```java
@Configuration
public class RememberMeConfig {

    @Bean
    public CustomRememberMeService customRememberMeService(...) {
        CustomRememberMeService service = new CustomRememberMeService(
            rememberMeKey,
            userDetailsService,
            persistentTokenRepository
        );
        service.setTokenValiditySeconds(1_209_600);  // 14일
        service.setCookieName("remember-me");
        service.setParameter("remember-me");
        service.setUseSecureCookie(true);
        service.setAlwaysRemember(false);
        return service;
    }
}
```

**사용법 (로그인 요청)**:
```json
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "password123",
  "remember-me": true
}
```

### 4.7 권한 체계

**Role 정의**:
```java
public enum Role {
    CUSTOMER,   // 일반 고객
    TRAINER,    // 트레이너
    ADMIN       // 관리자
}
```

**Controller 권한 제어**:
```java
@RestController
@RequestMapping("/api/v1/memos")
public class MemoV1Controller {

    // ✅ CUSTOMER 권한만 접근 가능
    @GetMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public BaseResponse<MemoResponseDTO> getMemo(
        @AuthenticationPrincipal(expression = "id") Long customerId
    ) {
        return BaseResponse.onSuccess(memoQueryService.getMemo(customerId));
    }
}

@RestController
@RequestMapping("/api/v1/admin/lectures")
public class AdminLectureV1Controller {

    // ✅ SecurityFilterChain 레벨에서 이미 ADMIN/TRAINER만 허용
    @GetMapping
    public BaseResponse<List<LectureDTO>> getAllLectures() {
        // ...
    }
}
```

**SecurityFilterChain 권한 설정**:
```java
// 관리자 API
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/admin/login").permitAll()
    .anyRequest().hasAnyRole("ADMIN", "TRAINER")  // ✅ 모든 관리자 API
);

// 사용자 API
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/auth/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/v1/feedback-requests").permitAll()
    .anyRequest().authenticated()  // ✅ 로그인 필수
);
```

### 4.8 인증 정보 접근 (AuthenticationPrincipal)

**Controller에서 현재 사용자 ID 가져오기**:
```java
@GetMapping("/me")
public BaseResponse<MemoResponseDTO> getMyMemo(
    @AuthenticationPrincipal(expression = "id") Long customerId
) {
    return BaseResponse.onSuccess(memoQueryService.getMemo(customerId));
}

// 또는 Authentication 객체 사용
@GetMapping("/me")
public BaseResponse<MeResponse> me(Authentication authentication) {
    AuthSessionUser principal = (AuthSessionUser) authentication.getPrincipal();
    MeResponse response = userService.getMe(principal.id());
    return BaseResponse.onSuccess(response);
}
```

**AuthSessionUser** (Security Principal):
```java
public record AuthSessionUser(
    Long id,
    String username,
    String password,
    Collection<? extends GrantedAuthority> authorities
) implements UserDetails {
    // UserDetails 인터페이스 구현
}
```

---

## 5. 기타 중요 프로젝트 특성

### 5.1 QueryDSL 사용 패턴

**Q-class 생성 위치**:
- `src/main/generated/` (Gradle 자동 생성)
- `.gitignore`에 포함 (버전 관리 제외)

**Gradle 설정**:
```gradle
dependencies {
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
}
```

**QueryDSL Config**:
```java
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```

**Repository 구현 예시**:
```java
// 1. Custom 인터페이스 정의
public interface LectureRepositoryCustom {
    List<ChapterBlockDTO> findCurriculum(Long userId, int page, int size);
}

// 2. QueryDSL 구현체
@Repository
@RequiredArgsConstructor
public class LectureRepositoryImpl implements LectureRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChapterBlockDTO> findCurriculum(Long userId, int page, int size) {
        // ✅ Q-class 임포트 (자동 생성)
        // import static com.tradingpt.tpt_api.domain.lecture.entity.QChapter.chapter;
        // import static com.tradingpt.tpt_api.domain.lecture.entity.QLecture.lecture;

        List<Tuple> rows = queryFactory
            .select(
                chapter.id,
                chapter.title,
                lecture.id,
                lecture.title,
                lectureProgress.watchedSeconds
            )
            .from(chapter)
            .join(lecture).on(lecture.chapter.eq(chapter))
            .leftJoin(lectureProgress)
                .on(lectureProgress.lecture.eq(lecture)
                    .and(lectureProgress.customer.id.eq(userId)))
            .orderBy(chapter.chapterOrder.asc(), lecture.lectureOrder.asc())
            .fetch();

        // DTO 변환 로직
        return convertToDTO(rows);
    }
}

// 3. JPA Repository에서 상속
public interface LectureRepository
    extends JpaRepository<Lecture, Long>, LectureRepositoryCustom {
    // JPA 메서드 + QueryDSL 메서드 모두 사용 가능
}
```

**Q-class 재생성**:
```bash
./gradlew clean        # Q-class 삭제 및 재생성
./gradlew compileJava  # Q-class만 재생성
```

### 5.2 AWS S3 파일 업로드 구조

**S3Config**:
```java
@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }
}
```

**S3Service 사용 예시**:
```java
@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드
     */
    public String uploadFile(MultipartFile file, String folderPath) {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String key = folderPath + "/" + fileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

            s3Client.putObject(request, RequestBody.fromInputStream(
                file.getInputStream(), file.getSize()));

            return getFileUrl(key);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build();
        s3Client.deleteObject(request);
    }

    private String getFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
            bucket, region, key);
    }
}
```

**파일 업로드 설정**:
```yaml
# application.yml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 200MB
      max-request-size: 300MB
```

### 5.3 ShedLock 분산 스케줄링

**ShedLock Config**:
```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }
}
```

**Scheduler 구현**:
```java
@Component
@RequiredArgsConstructor
public class LectureOpenScheduler {

    private final LectureOpenService lectureOpenService;

    // 매일 새벽 0시 실행
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(
        name = "weeklyLectureOpenJob",      // ✅ 고유 작업 이름
        lockAtLeastFor = "PT5S",            // ✅ 최소 락 시간
        lockAtMostFor = "PT30S"             // ✅ 최대 락 시간
    )
    public void openWeeklyLectures() {
        lectureOpenService.openWeeklyForActiveSubscriptions();
    }
}
```

**ShedLock 테이블 (자동 생성)**:
```sql
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

### 5.4 Redis 캐싱 전략

**Redis 설정**:
```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

**캐싱 사용 예시**:
```java
@Service
public class LectureQueryService {

    // ✅ 캐싱 적용
    @Cacheable(value = "lectures", key = "#lectureId")
    public LectureDetailDTO getLectureDetail(Long lectureId) {
        // DB 조회
        return ...;
    }

    // ✅ 캐시 무효화
    @CacheEvict(value = "lectures", key = "#lectureId")
    public void updateLecture(Long lectureId, LectureRequestDTO request) {
        // 업데이트 로직
    }
}
```

### 5.5 테스트 코드 구조 및 패턴

**Repository 테스트**:
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemoRepositoryTest {

    @Autowired
    private MemoRepository memoRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void findByCustomerId_메모가_존재하면_조회된다() {
        // given
        Customer customer = createCustomer();
        Memo memo = createMemo(customer);

        // when
        Optional<Memo> result = memoRepository.findByCustomer_Id(customer.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Memo");
    }
}
```

**Service 테스트**:
```java
@SpringBootTest
@Transactional
class MemoCommandServiceTest {

    @Autowired
    private MemoCommandService memoCommandService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void createMemo_정상적으로_메모가_생성된다() {
        // given
        Customer customer = createAndSaveCustomer();
        MemoRequestDTO request = new MemoRequestDTO("제목", "내용");

        // when
        MemoResponseDTO result = memoCommandService.createMemo(customer.getId(), request);

        // then
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getContent()).isEqualTo("내용");
    }
}
```

**Controller 테스트**:
```java
@WebMvcTest(MemoV1Controller.class)
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 제외
class MemoV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemoQueryService memoQueryService;

    @Test
    void getMemo_메모_조회_성공() throws Exception {
        // given
        MemoResponseDTO response = createMemoResponse();
        given(memoQueryService.getMemo(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/memos")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result.title").value("Test Memo"));
    }
}
```

### 5.6 API 응답 포맷 (BaseResponse 활용)

**성공 응답**:
```java
@GetMapping("/memos")
public BaseResponse<MemoResponseDTO> getMemo(...) {
    MemoResponseDTO memo = memoQueryService.getMemo(customerId);
    return BaseResponse.onSuccess(memo);  // ✅ COMMON200
}

@PostMapping("/memos")
public BaseResponse<MemoResponseDTO> createMemo(...) {
    MemoResponseDTO memo = memoCommandService.createMemo(...);
    return BaseResponse.onSuccessCreate(memo);  // ✅ COMMON201
}

@DeleteMapping("/memos")
public BaseResponse<Void> deleteMemo(...) {
    memoCommandService.deleteMemo(customerId);
    return BaseResponse.onSuccessDelete(null);  // ✅ COMMON202
}
```

### 5.7 Validation 처리 방식

**DTO에서 Validation 선언**:
```java
@Getter
public class MemoRequestDTO {

    @NotBlank(message = "메모 제목은 필수입니다.")
    @Size(max = 100, message = "메모 제목은 100자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "메모 내용은 필수입니다.")
    @Size(max = 5000, message = "메모 내용은 5000자를 초과할 수 없습니다.")
    private String content;
}
```

**Controller에서 검증**:
```java
@PostMapping("/memos")
public BaseResponse<MemoResponseDTO> createMemo(
    @Valid @RequestBody MemoRequestDTO request  // ✅ @Valid 어노테이션
) {
    // Validation 실패 시 GlobalExceptionHandler가 자동 처리
    return BaseResponse.onSuccessCreate(memoCommandService.createMemo(...));
}
```

**Validation 에러 응답**:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "code": "COMMON4005",
  "message": "입력값 검증에 실패했습니다.",
  "result": {
    "title": "메모 제목은 필수입니다.",
    "content": "메모 내용은 5000자를 초과할 수 없습니다."
  }
}
```

### 5.8 트랜잭션 관리 패턴

**Service 계층 트랜잭션**:
```java
@Service
@RequiredArgsConstructor
public class MemoCommandServiceImpl implements MemoCommandService {

    // ✅ Command Service: 쓰기 트랜잭션
    @Override
    @Transactional
    public MemoResponseDTO createMemo(Long customerId, MemoRequestDTO request) {
        // 트랜잭션 내에서 여러 Repository 호출 가능
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(...);
        Memo memo = memoRepository.save(...);
        return MemoResponseDTO.from(memo);
    }
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // ✅ Query Service: 읽기 전용
public class MemoQueryServiceImpl implements MemoQueryService {

    @Override
    public MemoResponseDTO getMemo(Long customerId) {
        // readOnly 최적화 적용
        Memo memo = memoRepository.findByCustomer_Id(customerId)
            .orElseThrow(...);
        return MemoResponseDTO.from(memo);
    }
}
```

**트랜잭션 전파 패턴**:
```java
@Service
@Transactional
public class LectureCommandService {

    // ✅ 부모 트랜잭션
    public void openLectureForUser(Long userId, Long lectureId) {
        // 강의 오픈
        lectureRepository.updateOpenStatus(lectureId, true);

        // 자식 트랜잭션 호출 (기본 REQUIRED 전파)
        lectureProgressService.createProgress(userId, lectureId);
    }
}

@Service
@Transactional
public class LectureProgressService {

    // ✅ 부모 트랜잭션에 참여
    public void createProgress(Long userId, Long lectureId) {
        LectureProgress progress = LectureProgress.builder()
            .userId(userId)
            .lectureId(lectureId)
            .build();
        lectureProgressRepository.save(progress);
    }
}
```

---

## 6. 개발 가이드라인 요약

### 6.1 새로운 도메인 추가 시 체크리스트

- [ ] `domain/{domain-name}/` 패키지 생성
- [ ] Controller (사용자용 + 관리자용 분리)
- [ ] Service (Command + Query 분리, 인터페이스 + 구현체)
- [ ] Repository (JPA + QueryDSL Custom 필요 시)
- [ ] DTO (Request + Response, Validation 포함)
- [ ] Entity (BaseEntity 상속, @SuperBuilder)
- [ ] Exception (`{Domain}Exception`, `{Domain}ErrorStatus`)
- [ ] 테스트 코드 (Repository, Service, Controller)

### 6.2 코딩 컨벤션

**Java**:
- 4칸 들여쓰기 (탭 대신 스페이스)
- Constructor Injection 우선 (`@RequiredArgsConstructor`)
- Lombok 적극 활용 (`@Getter`, `@Builder`, `@SuperBuilder`)

**Import 규칙** (필수):
- **항상 import 문 사용** - 메서드 바디에서 전체 패키지 경로 절대 금지
- 코드 가독성 향상 및 길이 감소를 위한 필수 규칙

```java
// ❌ BAD: 전체 패키지 경로 사용
List<com.tradingpt.tpt_api.domain.consultation.entity.Consultation> consultations =
    consultationRepository.findByCustomerIdOrderByConsultationDateDescConsultationTimeDesc(customerId);

// ✅ GOOD: import 문 사용
import com.tradingpt.tpt_api.domain.consultation.entity.Consultation;
// ...
List<Consultation> consultations =
    consultationRepository.findByCustomerIdOrderByConsultationDateDescConsultationTimeDesc(customerId);
```

**DTO 패턴** (필수):
- Response DTO는 **반드시 static factory method** 제공
- **Service에서 직접 Builder 사용 금지** - 코드가 너무 길어짐
- Entity를 받아서 DTO로 변환하는 `from()` 메서드 작성

```java
// ❌ BAD: Service에서 직접 Builder 사용
@Service
public class CustomerQueryServiceImpl {
    private CustomerResponseDTO toDTO(Customer customer) {
        return CustomerResponseDTO.builder()
            .customerId(customer.getId())
            .name(customer.getName())
            .phoneNumber(customer.getPhoneNumber())
            .hasAttemptedLevelTest(hasAttemptedLevelTest)
            .levelTestInfo(levelTestInfo)
            .hasConsultation(hasConsultation)
            .assignedTrainerName(customer.getAssignedTrainer() != null
                ? customer.getAssignedTrainer().getName()
                : null)
            .build();  // 코드가 너무 길어짐!
    }
}

// ✅ GOOD: DTO에 static factory method 작성
@Getter
@Builder
public class CustomerResponseDTO {
    private Long customerId;
    private String name;
    private String phoneNumber;
    private Boolean hasAttemptedLevelTest;
    private LevelTestInfo levelTestInfo;
    private Boolean hasConsultation;
    private String assignedTrainerName;

    /**
     * Customer 엔티티로부터 DTO 생성
     */
    public static CustomerResponseDTO from(
        Customer customer,
        boolean hasAttemptedLevelTest,
        LevelTestInfo levelTestInfo,
        boolean hasConsultation
    ) {
        return CustomerResponseDTO.builder()
            .customerId(customer.getId())
            .name(customer.getName())
            .phoneNumber(customer.getPhoneNumber())
            .hasAttemptedLevelTest(hasAttemptedLevelTest)
            .levelTestInfo(levelTestInfo)
            .hasConsultation(hasConsultation)
            .assignedTrainerName(customer.getAssignedTrainer() != null
                ? customer.getAssignedTrainer().getName()
                : null)
            .build();
    }
}

// Service에서 간결하게 사용
@Service
public class CustomerQueryServiceImpl {
    private CustomerResponseDTO toDTO(Customer customer) {
        // 레벨테스트 정보 조회
        boolean hasAttemptedLevelTest = leveltestAttemptRepository.existsByCustomer_Id(customer.getId());
        LevelTestInfo levelTestInfo = getLevelTestInfo(customer);
        boolean hasConsultation = hasConsultation(customer);

        // DTO factory method 사용 - 한 줄로 간결!
        return CustomerResponseDTO.from(customer, hasAttemptedLevelTest, levelTestInfo, hasConsultation);
    }
}
```

**네이밍**:
- Controller: `{Entity}{Role}V{version}Controller`
- Service: `{Entity}{Command|Query}Service` + `Impl`
- Repository: `{Entity}Repository` + `Custom` + `Impl` (QueryDSL)
- DTO: `{Entity}RequestDTO`, `{Entity}ResponseDTO`
- Exception: `{Domain}Exception`, `{Domain}ErrorStatus`

**API 설계**:
- 사용자 API: `/api/v1/{resource}`
- 관리자 API: `/api/v1/admin/{resource}`
- RESTful 원칙 준수 (GET/POST/PUT/DELETE)

**응답 포맷**:
- 성공: `BaseResponse.onSuccess()` (200)
- 생성 성공: `BaseResponse.onSuccessCreate()` (201)
- 삭제 성공: `BaseResponse.onSuccessDelete()` (202)
- 실패: `throw new {Domain}Exception(...)` → GlobalExceptionHandler

### 6.3 보안 관련 주의사항

- 민감 정보는 환경 변수로 관리 (`application-*.yml`)
- 비밀번호는 절대 로그 출력 금지
- CSRF 토큰 검증 (로그인/회원가입 제외)
- SQL Injection 방지 (QueryDSL 또는 Prepared Statement)
- XSS 방지 (JSoup HTML sanitization)
- 파일 업로드 시 MIME 타입 검증 (Apache Tika)

### 6.4 성능 최적화 팁

- `@Transactional(readOnly = true)` 적극 활용
- N+1 문제 해결 (`@EntityGraph`, `fetch join`, QueryDSL)
- 페이징 적용 (Pageable, Slice)
- Redis 캐싱 적용 (변경 빈도 낮은 데이터)
- Connection Pool 설정 (HikariCP: max 10, min 5)

### 6.5 Git 커밋 컨벤션

```
<emoji> type: short summary

예시:
✨ feat: add weekly P&L feedback API
🐛 fix: resolve CSRF token validation issue
♻️ refactor: improve QueryDSL query performance
📝 docs: update CLAUDE.md with codebase analysis
```

**Conventional Emoji**:
- `✨ feat`: 새로운 기능
- `🐛 fix`: 버그 수정
- `♻️ refactor`: 리팩토링
- `📝 docs`: 문서 수정
- `✅ test`: 테스트 추가/수정
- `🎨 style`: 코드 포맷팅
- `⚡ perf`: 성능 개선
- `🔧 chore`: 빌드/설정 변경

---

## 7. 참고 자료

**주요 파일 위치**:
- 전역 예외 처리: `/global/exception/GlobalExceptionHandler.java`
- 보안 설정: `/global/config/SecurityConfig.java`
- Redis 세션: `/global/config/RedisSessionConfig.java`
- QueryDSL 설정: `/global/config/QueryDslConfig.java`
- 공통 응답: `/global/common/BaseResponse.java`
- 공통 엔티티: `/global/common/BaseEntity.java`

**도메인 예시**:
- 심플한 구조: `/domain/memo/`
- 복잡한 구조: `/domain/auth/`, `/domain/lecture/`
- QueryDSL 활용: `/domain/lecture/repository/LectureRepositoryImpl.java`

**외부 문서**:
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- QueryDSL: http://querydsl.com/
- ShedLock: https://github.com/lukas-krecan/ShedLock
