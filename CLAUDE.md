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
  - `domain/` - Business domains, each containing:
    - Controllers, Services, Repositories
    - DTOs (request/response), Entities, Exceptions
    - Domain-specific infrastructure components
  - `global/` - Shared infrastructure:
    - `config/` - Spring configuration classes
    - `security/` - Security configuration and filters
    - `exception/` - Global exception handling
    - `infrastructure/` - External service integrations
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

## Important Implementation Notes

### QueryDSL
- Q-classes are generated automatically by Gradle into `src/main/generated/`
- Never commit generated files
- Run `./gradlew clean` to regenerate Q-classes after entity changes

### Security & Sessions
- **Dual Authentication System**: Separate authentication managers for User and Admin/Trainer
- **Redis-based Session**: 7-day timeout, cookie-based tracking with same-site policy
- **OAuth2 Integration**: Kakao and Naver social login with custom success/failure handlers
- **Remember-Me**: 14-day validity (1,209,600 seconds) with persistent token
- **CSRF Protection**: Custom header and cookie-based repository
- **Role-Based Access**: CUSTOMER, TRAINER, ADMIN roles

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
- Global exception handling in `global.exception`
- Custom exceptions per domain
- Stack traces disabled in production (error.include-stacktrace: never)

### Distributed Scheduling
- **ShedLock**: 5.13.0 with JDBC provider for distributed task coordination
- **Use Case**: Automated lecture opening at scheduled times
- **Configuration**: Database-backed locking mechanism
- **Best Practice**: Use for scheduled tasks in multi-instance deployments

## Development Guidelines

From AGENTS.md:
- Use Java 17 with four-space indentation and Lombok
- Constructor injection preferred over field injection
- Use record DTOs where practical
- Follow conventional-emoji commit style: `<emoji> type: short summary`
  - Example: `:sparkles: feat: add weekly P&L feedback API`
- Branch naming: `feature/#issue-description`
- Keep commits scoped to single concerns
- Include tests for new functionality
- Store secrets in environment variables, never hardcode in YAML files
- Document IAM roles and rate limits when adding external integrations

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
- Branch: `feature/#112-feat-매매일지-완성` (Trading journal completion)
- Working on: Trading journal feature completion

### Domain Structure Pattern
Each domain follows consistent organization:
- `controller/` - REST controllers
- `service/` - Business logic (query/command separation in some domains)
- `repository/` - Data access with QueryDSL support
- `dto/` - Request/response DTOs
- `entity/` - JPA entities
- `enums/` - Domain-specific enumerations
- `exception/` - Domain-specific exceptions