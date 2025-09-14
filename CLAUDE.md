# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TradingPT API is a Spring Boot 3.5.5 application using Java 17 that provides a trading education platform with feedback services. The application supports OAuth2 social login (Kakao, Naver), email/SMS verification, and feedback management for trading requests.

## Build System & Dependencies

**Build Tool**: Gradle with Gradle Wrapper
**Java Version**: 17 (Amazon Corretto)
**Spring Boot**: 3.5.5

### Key Dependencies
- **Spring Boot Starters**: Web, JPA, Security, OAuth2 Client, Validation, Redis, Mail, Actuator
- **Database**: MySQL with HikariCP connection pooling
- **Authentication**: Spring Security with OAuth2 (Kakao, Naver)
- **SMS Service**: Solapi (net.nurigo:sdk:4.2.7)
- **Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Utilities**: Lombok for boilerplate reduction

## Essential Commands

### Development
```bash
# Run locally with local profile
./gradlew bootRun --args='--spring.profiles.active=local'

# Build the application
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.tradingpt.tpt_api.TradingPtApplicationTests"

# Test with coverage
./gradlew test jacocoTestReport
```

### Docker & Deployment
```bash
# Build Docker image (requires built JAR)
docker build -t tpt-api .

# The application is deployed via AWS CodeDeploy using scripts in /scripts/
# - before-install.sh: Environment setup
# - start-server.sh: Docker container deployment from ECR
# - stop-server.sh: Container cleanup
# - validate-service.sh: Health check validation
```

## Architecture & Code Structure

### Domain-Driven Design
The codebase follows DDD principles with clear domain separation:

```
src/main/java/com/tradingpt/tpt_api/
├── domain/
│   ├── auth/           # Authentication & Authorization
│   ├── feedbackrequest/ # Trading feedback requests
│   ├── feedbackresponse/ # Trainer responses
│   ├── user/           # User management (Customer, Trainer)
│   ├── monthlytradingsummary/
│   └── weeklytradingsummary/
└── global/
    ├── common/         # BaseEntity, BaseResponse
    ├── config/         # Security, CORS, Mail, Swagger configs
    ├── exception/      # Global exception handling
    └── util/
```

### Domain Structure Pattern
Each domain follows consistent structure:
- `controller/` - REST API endpoints
- `entity/` - JPA entities with inheritance hierarchies
- `enums/` - Domain-specific enumerations
- `service/` - Business logic (command/query separation)
- `repository/` - Data access layer
- `dto/` - Data transfer objects (request/response)

### Key Architectural Patterns

**Entity Inheritance**: `User` as base entity with `Customer` and `Trainer` subclasses using `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`

**Authentication Flow**:
- Custom JSON-based username/password filter
- OAuth2 integration for social login
- Remember-me functionality with custom services
- Session-based authentication with Redis backing

**Feedback System**: 
- `FeedbackRequest` with polymorphic request details (`DayRequestDetail`, `ScalpingRequestDetail`, `SwingRequestDetail`)
- Attachment support for both requests and responses
- Status tracking and grade assignment

## Configuration & Environment

### Profile Structure
- **local**: Development with local MySQL
- **dev**: AWS deployment environment
- **Application properties**: Environment-specific configs in `application-{profile}.yml`

### Required Environment Variables
```
# Database
DB_URL, DB_USERNAME, DB_PASSWORD

# OAuth2
KAKAO_CLIENT_ID, KAKAO_REDIRECT_URI
NAVER_CLIENT_ID, NAVER_CLIENT_SECRET, NAVER_REDIRECT_URI

# Email
MAIL_NAME, MAIL_PASSWORD

# SMS
SOLAPI_API_KEY, SOLAPI_API_SECRET, SOLAPI_PHONE_NUMBER

# Security
REMEMBER_ME_KEY
```

### AWS Integration
- **Parameter Store**: Environment variables managed via AWS SSM
- **ECR**: Docker images stored in Elastic Container Registry
- **CodeDeploy**: Automated deployment with health checks
- **CloudWatch**: Logging and monitoring

## Development Guidelines

### Code Patterns
- Use Lombok `@SuperBuilder` for entity inheritance
- Implement `BaseEntity` for common audit fields
- Follow command/query separation in services
- Use `@RequiredArgsConstructor` with final fields for dependency injection

### Security Considerations
- All sensitive data managed via environment variables
- OAuth2 flows properly configured with redirect URIs
- Session security with SameSite=None, Secure=true for cross-origin
- File upload limits: 200MB max file, 300MB max request

### API Documentation
- Swagger UI available at `/swagger-ui.html`
- OpenAPI docs at `/v3/api-docs`
- All endpoints documented with SpringDoc annotations

### Monitoring & Health Checks
- Spring Boot Actuator endpoints: `/actuator/health`, `/actuator/info`
- Health check script validates application startup
- Container resource limits: 700MB memory, 1GB swap