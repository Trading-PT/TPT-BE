# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a Spring Boot 3.5.5 trading platform API (TPT-API) using Java 17, Spring Security with OAuth2 (Kakao/Naver), JPA with QueryDSL, Redis for session management, AWS services, and ShedLock for distributed scheduling. The application follows domain-driven design with clear separation between 19 business domains and shared infrastructure.

## Development Commands

### Build & Test
- `./gradlew clean build` - Full build including tests and JAR packaging
- `./gradlew test` - Run unit and integration tests only
- `./gradlew clean` - Clean build artifacts and QueryDSL generated classes
- `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun` - Run locally with local profile

### Development
- Local development uses `application-local.yml` profile
- Swagger UI available at `/swagger-ui.html` when running
- Health check at `/actuator/health`

## Architecture & Structure

### Package Organization
- `src/main/java/com/tradingpt/tpt_api/`
  - `domain/` - Business domains (19 domains), each containing:
    - `controller/` - REST API controllers (user + admin separation)
    - `service/` - Business logic with CQRS pattern
      - `command/` - CUD operations (Create, Update, Delete)
      - `query/` - Read operations (readOnly transactions)
    - `repository/` - Data access layer (JPA + QueryDSL Custom)
    - `dto/` - Data transfer objects
      - `request/` - Request DTOs with validation
      - `response/` - Response DTOs
    - `entity/` - JPA entities
    - `enums/` - Domain-specific enumerations (optional)
    - `exception/` - Domain-specific exceptions
    - `infrastructure/` - External service integration (optional, e.g., auth domain)
    - `handler/` - Event handlers (optional, e.g., auth domain)
    - `filter/` - Custom filters (optional, e.g., auth domain)
    - `scheduler/` - Scheduled tasks (optional, e.g., lecture domain)
  - `global/` - Shared infrastructure:
    - `config/` - Spring configuration classes
    - `security/` - Security configuration and filters
    - `exception/` - Global exception handling
      - `GlobalExceptionHandler.java` - Centralized exception handler
      - `BaseException.java` - Base class for all custom exceptions
      - `code/` - Error code interfaces and enums
    - `common/` - Common DTOs and entities
      - `BaseResponse.java` - Standard API response format
      - `BaseEntity.java` - Base entity with createdAt/updatedAt
    - `infrastructure/` - External service integrations (S3, NicePay, etc.)
    - `util/` - Common utilities
    - `web/` - Web layer configuration

### Core Domains (19 domains)
- `auth` - Authentication, OAuth2 (Kakao/Naver), dual authentication system (User/Admin-Trainer)
- `user` - User management (Customer/Trainer entities with role-based access)
- `feedbackrequest`/`feedbackresponse` - Trading feedback system with best feedback selection (max 4) and trainer tracking
- `weeklytradingsummary`/`monthlytradingsummary` - Trading performance analytics with P&L feedback retrieval
- `memo` - User memo management system (NEW)
- `lecture` - Lecture and chapter management with scheduled opening via ShedLock (RECENT)
- `leveltest` - User level testing and proficiency evaluation
- `consultation` - Consultation booking system with status tracking
- `review` - User review management with status control
- `column` - Content column management
- `complaint` - Customer complaint handling with workflow status
- `payment` - Payment processing and transaction management
- `paymentmethod` - Payment method management (card types)
- `subscription`/`subscriptionplan` - Subscription management and plan definitions
- `investmenttypehistory` - Investment type tracking over time (SCALPING/DAY/SWING)
- `customermembershiphistory` - Membership level transition tracking

### Key Technologies
- **QueryDSL**: 5.0.0 with Q-classes auto-generated in `src/main/generated/` (Jakarta EE compatible)
- **Spring Security**: OAuth2 with Kakao/Naver, dual authentication managers, custom filters
- **Redis**: Session storage (7-day timeout) and caching with Spring Session
- **MySQL**: Primary database with HikariCP connection pooling (max 10, min 5 idle)
- **AWS SDK**: 2.25.40 with S3 integration for file storage
- **ShedLock**: 5.13.0 for distributed task scheduling (lecture opening automation)
- **Solapi (CoolSMS)**: 4.2.7 for SMS notifications
- **Email**: Spring Mail with Gmail SMTP integration
- **JSoup**: 1.17.2 for HTML parsing and sanitization
- **Apache Tika**: 2.9.2 for MIME type detection
- **Swagger/OpenAPI**: springdoc-openapi 2.7.0 for API documentation

### Configuration
- Environment-specific configs: `application.yml`, `application-local.yml`, `application-dev.yml`
- Requires environment variables for secrets:
  - OAuth: `KAKAO_CLIENT_ID`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
  - Database: `LOCAL_DB_NAME`, `LOCAL_DB_USERNAME`, `LOCAL_DB_PASSWORD` (local only)
  - Security: `REMEMBER_ME_KEY`
  - AWS: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `S3_BUCKET_NAME`
  - Email: `MAIL_NAME`, `MAIL_PASSWORD`
  - SMS: `SOLAPI_API_KEY`, `SOLAPI_API_SECRET`, `SOLAPI_PHONE_NUMBER`
- CORS configured for frontend origins (localhost:3000, localhost:5173, localhost:8080)
- API endpoints: `/api/v1/` (user), `/api/v1/admin/` (admin)

### Testing Strategy
- JUnit 5 with Spring Boot Test support
- Use `@DataJpaTest` for repository tests
- Use `@WebMvcTest` for controller tests
- Use `@SpringBootTest` only for integration tests
- Mock external services (AWS, Redis, mail) in tests

## Coding Conventions & Patterns

### Naming Conventions

**Controller**:
- Pattern: `{Entity}{Role}V{Version}Controller`
- User API: `MemoV1Controller`, `LectureV1Controller`
- Admin API: `AdminLectureV1Controller`, `AdminFeedbackRequestV1Controller`
- Use `@RestController` + `@RequestMapping`
- Document with Swagger `@Tag` annotation

**Service**:
- Pattern: CQRS (Command/Query Separation) with interface + implementation
- Query Service: `{Entity}QueryService` + `{Entity}QueryServiceImpl`
  - Use `@Transactional(readOnly = true)` for read operations
- Command Service: `{Entity}CommandService` + `{Entity}CommandServiceImpl`
  - Use `@Transactional` for write operations
- Special purpose services may skip Command/Query separation (e.g., `AuthService`, `LectureOpenService`)

**Repository**:
- Basic JPA: `{Entity}Repository extends JpaRepository<Entity, ID>`
- QueryDSL extension:
  - Custom interface: `{Entity}RepositoryCustom`
  - Implementation: `{Entity}RepositoryImpl` (requires `@Repository` annotation)
  - Main repository: `extends JpaRepository<Entity, ID>, {Entity}RepositoryCustom`

**DTO**:
- Request: `{Entity}RequestDTO` or `{Operation}{Entity}RequestDTO`
  - Use `@Getter` + validation annotations (`@NotBlank`, `@Size`, `@Email`, etc.)
  - Document with Swagger `@Schema` annotation
- Response: `{Entity}ResponseDTO`
  - Use `@Getter` + `@Builder` (or record for simple DTOs)
  - Provide `from(Entity)` static factory method for entity-to-DTO conversion
  - Document with Swagger `@Schema` annotation

**Entity**:
- Use `@SuperBuilder` + `@NoArgsConstructor(access = PROTECTED)` + `@AllArgsConstructor`
- Extend `BaseEntity` for automatic `createdAt`/`updatedAt` management
- Use `@Getter` only (no `@Setter` - maintain immutability)
- ID field naming: `{entity}_id` (e.g., `memo_id`, `user_id`)
- Business logic methods inside entity class
- Example: `public void update(String title, String content) { ... }`

**Exception**:
- Domain exception: `{Domain}Exception extends BaseException`
- Error codes: `{Domain}ErrorStatus enum implements BaseCodeInterface`
- Error code format: `{DOMAIN}{number}` (e.g., `MEMO6001`, `USER4001`)
- Domain-specific error code ranges:
  - AUTH: 1000s
  - USER: 4000s
  - MEMO: 6000s
  - LECTURE: 7000s

### API Design Patterns

**Endpoint Structure**:
- User API: `/api/v1/{resource}`
- Admin API: `/api/v1/admin/{resource}`
- Follow RESTful principles (GET/POST/PUT/DELETE)

**Response Format** (using BaseResponse):
```java
// Success - 200
return BaseResponse.onSuccess(data);

// Created - 201
return BaseResponse.onSuccessCreate(data);

// Deleted - 202
return BaseResponse.onSuccessDelete(null);

// Error - handled by GlobalExceptionHandler
throw new {Domain}Exception({Domain}ErrorStatus.XXX);
```

**Standard Response Structure**:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "code": "COMMON200",
  "message": "요청에 성공하였습니다.",
  "result": { ... }
}
```

### Transaction Management

**Service Layer Patterns**:
- Query Service: `@Transactional(readOnly = true)` at class level
- Command Service: `@Transactional` at method level
- Default propagation: `REQUIRED` (joins existing transaction)
- Open-in-View: Disabled for better performance

### Validation Handling

**DTO-based Validation**:
- Declare validation in Request DTOs using Bean Validation annotations
- Use `@Valid` in controller method parameters
- GlobalExceptionHandler automatically processes validation errors
- Returns detailed field-level error messages in response

## Important Implementation Notes

### QueryDSL
- Q-classes are generated automatically by Gradle into `src/main/generated/`
- Never commit generated files
- Run `./gradlew clean` to regenerate Q-classes after entity changes

### Security & Sessions

#### Dual Authentication System

**Architecture**: Separate authentication paths for users and admin/trainers to prevent privilege escalation.

**User Authentication**:
- Endpoint: `/api/v1/auth/login` (JSON-based authentication)
- `userAuthProvider` → only allows CUSTOMER role
- `userAuthenticationManager` → uses `userAuthProvider`
- `JsonUsernamePasswordAuthFilter` → intercepts login requests
- Rejects ADMIN/TRAINER roles at authentication level

**Admin/Trainer Authentication**:
- Endpoint: `/api/v1/admin/login` (JSON-based authentication)
- `adminAuthProvider` → only allows ADMIN/TRAINER roles
- `adminAuthenticationManager` → uses `adminAuthProvider`
- `AdminJsonUsernamePasswordAuthFilter` → intercepts admin login requests
- Rejects CUSTOMER role at authentication level

**Session Management**:
- Redis-backed sessions with 7-day timeout (604,800 seconds)
- Cookie-based session tracking with SameSite=Lax policy
- Session concurrency control:
  - Users: max 3 concurrent sessions
  - Admin/Trainers: max 1 concurrent session
- Cookie configuration:
  - Name: `SESSION`
  - HttpOnly: true
  - Secure: environment-dependent
  - Path: `/`

**Authentication Flow**:

1. **SMS-based Signup** (Phone verification required):
   ```
   POST /api/v1/auth/phone/code → send verification code
   POST /api/v1/auth/phone/verify → verify code (stores flag in session)
   POST /api/v1/auth/signup → create account (checks session flag)
   ```

2. **JSON Login**:
   ```json
   POST /api/v1/auth/login
   Content-Type: application/json

   {
     "username": "user123",
     "password": "password123",
     "remember-me": true
   }
   ```

3. **OAuth2 Social Login**:
   ```
   GET /oauth2/authorization/kakao → Kakao login
   GET /oauth2/authorization/naver → Naver login
   ```
   - `CustomOAuth2UserService` handles user info extraction
   - Auto-creates user account if not exists
   - Maps to internal `User` entity with `Provider` enum

**CSRF Protection**:
- Cookie + Header dual token strategy
- `HeaderAndCookieCsrfTokenRepository` implementation
- Token delivered in response header: `X-CSRF-TOKEN`
- Client includes token in request header for state-changing operations
- Excluded paths: `/api/v1/auth/**`, `/oauth2/**`, `/api/v1/admin/login`
- Cookie properties:
  - HttpOnly: false (JavaScript-accessible for SPA)
  - SameSite: Lax
  - Secure: environment-dependent

**Remember-Me Feature**:
- Token validity: 14 days (1,209,600 seconds)
- Persistent token repository (database-backed)
- Cookie name: `remember-me`
- Secure cookie: true
- Always remember: false (requires explicit opt-in)
- `CustomRememberMeService` implementation

**Role-Based Access Control**:
- Roles: `CUSTOMER`, `TRAINER`, `ADMIN`
- User hierarchy:
  - `User` (abstract base entity)
    - `Customer` extends `User` → CUSTOMER role
    - `Trainer` extends `User` → TRAINER role
    - `Admin` extends `User` → ADMIN role
- Authorization:
  - User API: requires authentication (any role for general endpoints)
  - Admin API: requires `ADMIN` or `TRAINER` role
  - Method-level security: `@PreAuthorize("hasRole('ROLE_CUSTOMER')")`

**Principal Access in Controllers**:
```java
// Using @AuthenticationPrincipal
@GetMapping("/me")
public BaseResponse<MemoResponseDTO> getMyMemo(
    @AuthenticationPrincipal(expression = "id") Long customerId
) { ... }

// Using Authentication object
@GetMapping("/me")
public BaseResponse<MeResponse> me(Authentication authentication) {
    AuthSessionUser principal = (AuthSessionUser) authentication.getPrincipal();
    Long userId = principal.id();
    ...
}
```

**Security Configuration**:
- Two separate `SecurityFilterChain` beans with `@Order` annotation
- Order 1: Admin chain (path: `/api/v1/admin/**`)
- Order 2: User chain (all other paths)
- Custom authentication filters for JSON-based login
- Custom success/failure handlers for authentication events
- Session fixation protection enabled
- Logout handling with session invalidation and cookie clearing

### Database
- **JPA/Hibernate**: Primary ORM with Jakarta persistence API
- **Batch Operations**: batch_size: 20, order inserts/updates enabled
- **Connection Pooling**: HikariCP (max 10 connections, min 5 idle)
- **Open-in-View**: Disabled (best practice for performance)
- **Second-Level Cache**: Disabled
- **QueryDSL**: Automatic Q-class generation via annotation processors

### File Uploads
- Multipart support enabled
- Max file size: 200MB, max request size: 300MB
- Files stored via AWS S3 integration

### Error Handling

**Exception Hierarchy**:
```
BaseException (RuntimeException)
├── AuthException
├── UserException
├── MemoException
├── LectureException
└── ... (domain-specific exceptions)
```

**Error Code Structure**:
- `BaseCodeInterface` → `BaseCode` (value object with HttpStatus, code, message)
- Domain-specific: `{Domain}ErrorStatus enum implements BaseCodeInterface`
- Global errors: `GlobalErrorStatus` (COMMON-prefixed codes)

**GlobalExceptionHandler** (`@RestControllerAdvice`):
1. Domain exceptions (`BaseException`) → extract error code and return standardized response
2. Validation errors (`MethodArgumentNotValidException`) → field-level error map
3. Spring Security exceptions (`AuthenticationException`, `AccessDeniedException`)
4. Database errors (`DataIntegrityViolationException`, `SQLException`)
5. HTTP errors (message not readable, unsupported media type, method not allowed)
6. File upload size exceeded
7. Generic Exception → fallback handler (500)

**Exception Usage Pattern**:
```java
// Service layer
Customer customer = customerRepository.findById(id)
    .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

// Conditional validation
if (memoRepository.existsByCustomer_Id(customerId)) {
    throw new MemoException(MemoErrorStatus.MEMO_ALREADY_EXISTS);
}
```

**Error Response Format**:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "code": "MEMO6001",
  "message": "메모를 찾을 수 없습니다.",
  "result": null
}
```

**Validation Error Response**:
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

**Configuration**:
- Stack traces disabled in production (error.include-stacktrace: never)
- Custom exceptions per domain
- Centralized error handling in `global.exception`

### Distributed Scheduling
- **ShedLock**: 5.13.0 with JDBC provider for distributed task coordination
- **Use Case**: Automated lecture opening at scheduled times
- **Configuration**: Database-backed locking mechanism
- **Best Practice**: Use for scheduled tasks in multi-instance deployments

## Development Guidelines

### Code Style
- Use Java 17 with four-space indentation (spaces, not tabs)
- Lombok usage:
  - `@RequiredArgsConstructor` for constructor injection (preferred)
  - `@Getter` for entities and DTOs (no `@Setter` for immutability)
  - `@SuperBuilder` for entities extending `BaseEntity`
  - `@Builder` for DTOs
- Constructor injection preferred over field injection
- Use record DTOs for simple data carriers
- Follow SOLID principles and DRY

### API Development
- RESTful design: proper use of HTTP methods and status codes
- Always use `BaseResponse<T>` for standardized responses
- Version APIs (e.g., `/api/v1/`)
- Separate user and admin endpoints
- Document with Swagger/OpenAPI annotations (`@Tag`, `@Schema`, `@Operation`)
- Use `@Valid` for request body validation

### Testing
- Write tests for new features (repository, service, controller)
- Use appropriate test annotations:
  - `@DataJpaTest` for repositories
  - `@SpringBootTest` for services
  - `@WebMvcTest` for controllers
- Mock external dependencies (AWS, Redis, email)
- Test naming: `methodName_scenario_expectedResult()`

### Security Guidelines
- **Never** commit secrets to version control
- Store all sensitive data in environment variables
- Use the dual authentication system correctly (don't bypass role checks)
- Always validate and sanitize user input
- Use parameterized queries or QueryDSL to prevent SQL injection
- Apply CSRF protection to state-changing operations
- Validate file uploads (type, size, content)
- Use JSoup for HTML sanitization
- Use Apache Tika for MIME type detection
- Log security events appropriately (authentication failures, access denials)

### Performance Best Practices
- Use `@Transactional(readOnly = true)` for read-only operations
- Prevent N+1 queries:
  - Use `@EntityGraph` or `fetch join`
  - Use QueryDSL for complex queries
- Apply pagination (Pageable, Slice, Page)
- Use Redis caching for frequently accessed, rarely changed data
- Configure HikariCP connection pool appropriately (current: max 10, min 5)
- Disable Open-in-View for better performance
- Use batch operations for bulk inserts/updates

### Git Workflow
- Branch naming: `feature/#issue-number-description`
- Commit style: Conventional Emoji format
  ```
  <emoji> type: short summary

  Examples:
  ✨ feat: add weekly P&L feedback API
  🐛 fix: resolve CSRF token validation issue
  ♻️ refactor: improve QueryDSL query performance
  📝 docs: update CLAUDE.md with authentication flow
  ✅ test: add integration tests for memo service
  🎨 style: format code according to style guide
  ⚡ perf: optimize lecture query performance
  🔧 chore: update Gradle dependencies
  ```
- Keep commits focused on single concerns
- Write meaningful commit messages
- Reference issue numbers in commits

### Adding New Domains Checklist
1. Create domain package structure:
   - `controller/` (user + admin if needed)
   - `service/command/` and `service/query/`
   - `repository/` (+ Custom + Impl for QueryDSL)
   - `dto/request/` and `dto/response/`
   - `entity/`
   - `exception/` (Exception class + ErrorStatus enum)
2. Follow naming conventions for all classes
3. Implement service interfaces and implementations
4. Add validation annotations to Request DTOs
5. Create static `from()` factory in Response DTOs
6. Write comprehensive tests (repository, service, controller)
7. Document APIs with Swagger annotations
8. Update CLAUDE.md if introducing new patterns

### External Integration Guidelines
- Document IAM roles and permissions for AWS services
- Note rate limits and quotas for external APIs
- Implement circuit breakers for external service calls
- Log external service failures appropriately
- Never hardcode API keys or credentials
- Use configuration properties for service URLs and settings

## Recent Features & Updates

### Major Features (Recent Additions)
1. **Lecture Management System** (Feature #103)
   - Complete lecture and chapter structure
   - Scheduled lecture opening via ShedLock
   - Progress tracking for users
   - Lecture exposure control and admin management APIs
   - File attachments support

2. **Memo System** (NEW)
   - User-specific memo management
   - Full CRUD operations
   - Location: `domain/memo/`

3. **Enhanced Feedback System**
   - Weekly P&L feedback retrieval API
   - Best feedback selection system (max 4 via constant)
   - Trainer-written feedback tracking (`isTrainerWritten` field)
   - Investment type discrimination (SCALPING/DAY/SWING)

### Current Development
- Branch: `feature/#124-feat-정기-결제-기능` (Recurring payment feature)
- Working on: Recurring payment functionality implementation

### Domain Structure Pattern
Each domain follows consistent organization:
- `controller/` - REST controllers
- `service/` - Business logic (query/command separation in some domains)
- `repository/` - Data access with QueryDSL support
- `dto/` - Request/response DTOs
- `entity/` - JPA entities
- `enums/` - Domain-specific enumerations
- `exception/` - Domain-specific exceptions