# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a Spring Boot 3.5 trading platform API (TPT-API) using Java 17, Spring Security with OAuth2 (Kakao/Naver), JPA with QueryDSL, Redis for session management, and AWS services. The application follows domain-driven design with clear separation between business domains and shared infrastructure.

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

### Core Domains
- `auth` - Authentication, OAuth2, session management
- `user` - User management (Customer/Trainer entities)
- `feedbackrequest`/`feedbackresponse` - Trading feedback system
- `weeklytradingsummary`/`monthlytradingsummary` - Trading performance analytics
- `consultation` - Consultation booking system
- `payment` - Payment method management
- `complaint` - Customer complaint handling

### Key Technologies
- **QueryDSL**: Q-classes auto-generated in `src/main/generated/` (managed by Gradle)
- **Spring Security**: OAuth2 with Kakao/Naver, custom filters
- **Redis**: Session storage and caching
- **MySQL**: Primary database with HikariCP connection pooling
- **AWS SDK**: S3 integration for file storage
- **Swagger/OpenAPI**: API documentation

### Configuration
- Environment-specific configs: `application.yml`, `application-local.yml`, `application-dev.yml`
- Requires environment variables for secrets (OAuth credentials, database URL, AWS credentials)
- CORS configured for frontend origins (localhost:3000, localhost:5173)

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
- Redis-based session management with 7-day timeout
- OAuth2 integration with custom success/failure handlers
- Remember-me functionality configured
- CSRF protection enabled with custom configuration

### Database
- Uses JPA with Hibernate
- Batch operations configured (batch_size: 20)
- Connection pooling with HikariCP
- Open-in-view disabled for performance

### File Uploads
- Multipart support enabled
- Max file size: 200MB, max request size: 300MB
- Files stored via AWS S3 integration

### Error Handling
- Global exception handling in `global.exception`
- Custom exceptions per domain
- Stack traces disabled in production (error.include-stacktrace: never)

## Development Guidelines

From AGENTS.md:
- Use Java 17 with four-space indentation and Lombok
- Constructor injection preferred over field injection
- Use record DTOs where practical
- Follow conventional-emoji commit style: `<emoji> type: short summary`
- Branch naming: `feature/#issue-description`
- Keep commits scoped to single concerns
- Include tests for new functionality
- Store secrets in environment variables, never hardcode in YAML files
- Document IAM roles and rate limits when adding external integrations