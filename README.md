## 코드 컨벤션

### Kotlin 스타일 가이드

#### 클래스 설계

```kotlin
// ✅ 좋은 예시 - 명확한 책임과 구조
 @Entity @Table(name = "family_questions")
class FamilyQuestion(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    val question: Question,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    val family: Family,

    @Column(name = "assigned_at")
    val assignedAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING) @Column(name = "status")
    var status: FamilyQuestionStatus = FamilyQuestionStatus.ASSIGNED
) : BaseEntity() {

    fun complete() {
        this.status = FamilyQuestionStatus.COMPLETED
    }

    companion object {
        fun create(question: Question, family: Family): FamilyQuestion {
            return FamilyQuestion(
                question = question,
                family = family
            )
        }
    }
}
```

#### 서비스 계층 설계 (CQRS)

```kotlin
// ✅ 좋은 예시 - Command/Query 책임 분리

// QueryService: 조회 및 데이터 변환 책임
@Service
@Transactional(readOnly = true)
class QuestionQueryService(
    private val questionRepository: QuestionRepository
) {
    fun findQuestionById(id: Long): QuestionResponse {
        val question = questionRepository.findByIdOrThrow(id)
        return QuestionResponse.from(question)
    }
}

// CommandService: 상태 변경 책임
@Service
@Transactional
class QuestionCommandService(
    private val questionRepository: QuestionRepository,
    private val familyRepository: FamilyRepository
) {
    fun createQuestion(request: CreateQuestionRequest): Long {
        val family = familyRepository.findByIdOrThrow(request.familyId)
        val question = Question(
            content = request.content,
            category = request.category,
            family = family
        )
        return questionRepository.save(question).id
    }
}
```

#### Repository 계층 (QueryDSL)

```kotlin
// ✅ 좋은 예시 - 복잡한 쿼리의 타입 안전성
 @Repository
class FamilyQuestionRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : FamilyQuestionRepositoryCustom {

    override fun findRecentByFamilyId(
        familyId: Long,
        pageable: Pageable
    ): List<FamilyQuestion> {
        return queryFactory
            .selectFrom(familyQuestion)
            .join(familyQuestion.question, question).fetchJoin()
            .where(familyQuestion.family.id.eq(familyId))
            .orderBy(familyQuestion.assignedAt.desc())
            .limit(pageable.pageSize.toLong())
            .offset(pageable.offset)
            .fetch()
    }
}
```

### ️ 아키텍처 원칙

#### 계층별 책임

```kotlin
// Controller: HTTP 요청/응답 처리, 서비스 호출
@RestController
@RequestMapping("/api/v1/questions")
class QuestionController(
    private val questionQueryService: QuestionQueryService,
    private val questionCommandService: QuestionCommandService
) {
    @GetMapping("/{id}")
    fun getQuestion(@PathVariable id: Long) = 
        ResponseEntity.ok(BaseResponse.onSuccess(questionQueryService.findQuestionById(id)))

    @PostMapping
    fun createQuestion(@RequestBody request: CreateQuestionRequest) =
        ResponseEntity.ok(BaseResponse.onSuccess(questionCommandService.createQuestion(request)))
}

// Service: 비즈니스 로직, 트랜잭션 관리
// - QueryService: 조회 로직
// - CommandService: 변경 로직

// Repository: 데이터 접근만
@Repository
interface QuestionRepository : JpaRepository<Question, Long>, QuestionRepositoryCustom
```

#### 예외 처리 전략

```kotlin
// 도메인별 커스텀 예외
class QuestionException(
    errorStatus: QuestionErrorStatus
) : BaseException(errorStatus)

enum class QuestionErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_001", "질문을 찾을 수 없습니다."),
    QUESTION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "QUESTION_002", "질문 생성에 실패했습니다.")
}

// 전역 예외 처리
 @RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(QuestionException::class)
    fun handleQuestionException(e: QuestionException): ResponseEntity<BaseResponse<Nothing>> {
        return ResponseEntity.status(e.getErrorCode().httpStatus)
            .body(BaseResponse.onFailure(e.getErrorCode()))
    }
}
```

### 네이밍 규칙

#### 파일/클래스 네이밍

```kotlin
// 컨트롤러
class FamilyQuestionController      // {Domain}{Feature}Controller

// 서비스 (CQRS)
interface QuestionQueryService      // {Domain}QueryService
interface QuestionCommandService    // {Domain}CommandService
class QuestionQueryServiceImpl    // {Domain}QueryServiceImpl
class QuestionCommandServiceImpl  // {Domain}CommandServiceImpl

// 리포지토리
interface FamilyQuestionRepository  // {Domain}Repository
class FamilyQuestionRepositoryImpl  // {Domain}RepositoryImpl

// DTO
class QuestionResponse              // {Action/Entity}Response
class CreateQuestionRequest         // {Action/Entity}Request
```

#### 메서드 네이밍

```kotlin
// ✅ 좋은 예시 - 의도가 명확한 네이밍
fun findQuestionById(id: Long): QuestionResponse
fun createQuestion(request: CreateQuestionRequest): Long
fun assignQuestionsToFamily(family: Family, questions: List<Question>)

// ❌ 나쁜 예시 - 모호한 네이밍
fun process(data: Any): Any
fun handle(request: Request): Response
fun doSomething(): Result
```

## 커밋 컨벤션

### Conventional Commits 2.0 + Gitmoji

#### 기본 형식

```
<type>(<scope>): <gitmoji> <description>

[optional body]

[optional footer(s)]
```

#### 커밋 타입 정의

| Type       | 설명         | Gitmoji | 예시                                                     |
|------------|--------------|---------|----------------------------------------------------------|
| `feat`     | 새로운 기능 추가 | ✨      | `feat(auth): ✨ Kakao OAuth2 로그인 구현`                    |
| `fix`      | 버그 수정      | 🐛      | `fix(question): 🐛 질문 풀 초기화 오류 수정`                  |
| `perf`     | 성능 개선      | ⚡️      | `perf(question): ⚡️ 질문 생성 응답시간 60초→9ms 개선`          |
| `refactor` | 코드 리팩토링    | ♻️      | `refactor(service): ♻️ FamilyService 메서드 분리`         |
| `docs`     | 문서 변경      | 📝      | `docs: 📝 AI 질문 시스템 성능 최적화 문서 추가`                |
| `test`     | 테스트 추가/수정 | ✅      | `test(question): ✅ QuestionPoolService 단위 테스트`      |
| `chore`    | 빌드/설정 변경   | 🔧      | `chore: 🔧 Gradle 의존성 업데이트`                         |
| `style`    | 코드 포맷팅     | 🎨      | `style: 🎨 Ktlint 규칙 적용`                             |

#### 실제 커밋 메시지 예시

```bash
# ✅ 우수한 커밋 메시지
feat(question): ✨ AI 질문 생성 성능 최적화 시스템 구현

- QuestionPoolInitializer로 앱 시작시 질문 풀 사전 생성
- 3-Tier 캐싱 전략으로 응답시간 60초→9ms 개선 (99.985%)
- Redis 기반 카테고리별 질문 풀 관리
- 백그라운드 비동기 질문 보충 메커니즘
- OpenAI API 장애 시 폴백 질문 시스템

Performance: 첫 질문 생성 60초 → 9ms
Availability: 3계층 폴백으로 99.9% 가용성 보장

Resolves: #45, #67
Co-authored-by: TeamMate <teammate@email.com>

# ✅ 간단한 수정
fix(auth): 🐛 JWT 토큰 만료 시간 설정 오류 수정

토큰 만료 시간이 24시간으로 설정되지 않던 문제 해결

# ✅ 문서 업데이트  
docs: 📝 성능 최적화 기술 문서 추가

PERFORMANCE_OPTIMIZATION_QUESTION_GENERATION.md 파일 생성:
- 60초→9ms 성능 개선 과정 상세 기록
- 3-Tier 캐싱 전략 설명
- 시스템 아키텍처 다이어그램
- 모니터링 및 운영 가이드

# ❌ 좋지 않은 커밋 메시지
fix bug
update service
add new feature
change config
```

#### 브랜치 네이밍 전략

```bash
# 기능 개발
feature/question-generation-optimization
feature/kakao-oauth2-integration  
feature/family-todo-system

# 버그 수정
bugfix/jwt-token-expiration
bugfix/redis-connection-timeout
bugfix/file-upload-validation

# 핫픽스
hotfix/critical-security-patch
hotfix/memory-leak-fix

# 릴리즈
release/v1.0.0
release/v1.1.0-beta
```