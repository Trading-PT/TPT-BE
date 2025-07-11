# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

If the user's prompt starts with “EP:”, then the user wants to enhance the prompt. Read the PROMPT_ENHANCER.md file and
follow the guidelines to enhance the user's prompt. Show the user the enhancement and get their permission to run it
before taking action on the enhanced prompt. The enhanced prompts will follow the language of the original prompt (e.g.,
Korean prompt input will output Korean prompt enhancements, English prompt input will output English prompt
enhancements, etc.)

## Core Architecture

TPT is a Spring Boot 3.5.3 application built with Kotlin 1.9.25 following clean architecture and domain-driven design
principles. The application uses PostgreSQL as the primary database with Redis for caching and session management.

### Key Architectural Patterns

- **Clean Architecture**: Domain entities are separated from infrastructure concerns
- **Repository Pattern**: Data access abstraction with QueryDSL for type-safe queries
- **CQRS**: Command Query Responsibility Segregation in service layer
- **Exception Handling**: Global exception handling with custom error codes via `BaseException` and
  `GlobalExceptionHandler`
- **API Response Standardization**: All endpoints use `BaseResponse<T>` wrapper

## Common Development Commands

### Build and Run

```bash
# Run application locally
./gradlew bootRun

# Build project
./gradlew build

# Run tests
./gradlew test

# Build without tests (CI/CD)
./gradlew clean build -x test
```

### Docker Development

```bash
# Local development environment (PostgreSQL + Redis + App)
docker-compose -f docker-compose-local.yml up -d

# Development environment
docker-compose -f docker-compose-dev.yml up -d

# Stop containers
docker-compose -f docker-compose-local.yml down
```

### Database Operations

- **Flyway migrations**: Database schema changes go in `src/main/resources/db/migration/`
- **QueryDSL**: Generated Q-classes for type-safe queries (regenerated on build)
- **JPA Auditing**: All entities should extend `BaseEntity` for automatic created/updated timestamps

## Technology Stack

### Core Framework

- **Spring Boot 3.5.3** with Jakarta EE
- **Kotlin 1.9.25** on Java 17
- **Gradle Kotlin DSL** for build configuration

### Database & Persistence

- **PostgreSQL** with HikariCP connection pooling
- **Spring Data JPA** with QueryDSL 5.1.0
- **Flyway** for database migrations
- **Redis** for caching (1-hour TTL) and session storage

### Security

- **Spring Security** with OAuth2 Client
- **JWT tokens** using jjwt 0.12.6
- **Session management** with Redis backing
- **CSRF protection** (disabled for API endpoints)

### API & Documentation

- **SpringDoc OpenAPI 2.6.0** for API documentation
- **Jackson** with Kotlin serialization support
- **Spring Validation** for request validation

## Configuration Profiles

### Environment-Specific Files

- `application.yml` - Base configuration
- `application-local.yml` - Local development settings
- `application-dev.yml` - Development environment with enhanced logging

### Key Configuration Areas

- **Database**: Connection pooling, JPA settings, and transaction management
- **Redis**: Caching configuration with JSON serialization
- **Security**: OAuth2 providers, JWT settings, and session management
- **Logging**: Structured logging with different levels per environment

## Code Organization

### Package Structure

```
com.example.tpt/
├── common/
│   ├── base/           # BaseEntity, BaseResponse
│   ├── exception/      # Global exception handling
│   ├── util/          # Utility classes
│   └── validator/     # Custom validators
├── config/            # Spring configuration classes
├── domain/            # Domain entities and business logic
├── infrastructure/    # External integrations and persistence
└── security/          # Security configuration and services
```

### Key Base Classes

- **BaseEntity**: JPA entity base with auditing fields
- **BaseResponse<T>**: Standardized API response wrapper
- **BaseException**: Custom exception hierarchy with error codes
- **GlobalExceptionHandler**: Centralized exception handling

## Development Guidelines

### Entity Development

- Extend `BaseEntity` for automatic auditing
- Use QueryDSL for complex queries
- Follow JPA naming conventions

### API Development

- Wrap all responses in `BaseResponse<T>`
- Use custom exception codes from `code/` package
- Apply validation annotations for request DTOs
- Document APIs with SpringDoc annotations

### Security Integration

- OAuth2 configuration in `security/config/SecurityConfig.kt`
- Custom user service implementation for OAuth2
- JWT token management with Redis session backing
- Role-based access control setup

## Testing

### Test Structure

- **Unit Tests**: JUnit 5 with Kotlin Test
- **Integration Tests**: Spring Boot Test with test containers
- **Security Tests**: Spring Security Test for authentication flows

### Test Configuration

- Test profiles for different testing scenarios
- MockMvc for API endpoint testing
- Separate test configuration for database and Redis

## CI/CD Pipeline

### GitHub Actions Workflow

- **Trigger**: Push to `develop` branch
- **Build**: JDK 17 with Gradle caching
- **Deploy**: AWS ECR → CodeDeploy → EC2

### AWS Integration

- **ECR**: Container registry for Docker images
- **Parameter Store**: Secure configuration management
- **CodeDeploy**: Automated deployment with health checks
- **S3**: Deployment artifact storage

### Deployment Scripts

- `scripts/load-env.sh` - Load environment variables from AWS Parameter Store
- `scripts/start-app.sh` - ECR login, image pull, and container startup
- `scripts/validate.sh` - Health check validation with retry logic

## Monitoring and Logging

### Observability

- **Spring Boot Actuator** for application metrics
- **CloudWatch** integration for AWS environments
- **Structured logging** with different levels per environment

### Health Checks

- Application health endpoint: `/actuator/health`
- Database connectivity validation
- Redis connectivity validation
- Custom health indicators as needed

## Deployment Troubleshooting

### Common CodeDeploy Issues

**Script Not Found Error:**
```
ScriptMissing: Script does not exist at specified location
```
**Solution:**
1. Verify script files have execute permissions: `chmod +x scripts/*.sh`
2. Check appspec.yml script paths match actual file names
3. Ensure GitHub Actions includes scripts/ directory in deployment package
4. Confirm all required scripts exist:
   - scripts/stop-containers.sh (BeforeInstall)
   - scripts/load-env.sh (AfterInstall)
   - scripts/start-app.sh (ApplicationStart)
   - scripts/validate.sh (ValidateService)

**Deployment Package Issues:**
```bash
# Verify deployment package contents
unzip -l deploy.zip | grep scripts/
```

**Parameter Store Access:**
- Ensure EC2 instance has appropriate IAM role for Parameter Store access
- Check parameter names match script expectations:
  - `/dev/ecr/registry`
  - `/dev/ecr/repository`
  - `/dev/image/tag`

**Permissions Duplicate Error:**
```
The permissions setting for (file) is specified more than once in the application specification file
```
**Solution:**
1. Check appspec.yml permissions section for duplicate entries
2. Remove overlapping permission settings:
   - Avoid setting permissions for both parent directory and files with patterns
   - Use either directory-level permissions OR file-pattern permissions, not both
3. Example fix:
   ```yaml
   permissions:
     - object: /home/ubuntu/app
       owner: ubuntu
       group: ubuntu
       mode: 755
     - object: /home/ubuntu/app/scripts
       pattern: "*.sh"
       mode: 755  # Don't specify owner/group again
   ```

**Health Check Failures:**
- Verify application starts within timeout period (600s)
- Check container logs: `docker logs tpt-app`
- Validate database connectivity and Redis availability
- Ensure all required environment variables are loaded

### Quick Diagnosis Commands

```bash
# Check script permissions
ls -la scripts/

# Verify deployment package
zip -r test-deploy.zip docker-compose-dev.yml appspec.yml scripts/
unzip -l test-deploy.zip

# Check application health
curl -f http://localhost:8080/actuator/health || echo "Health check failed"

# Container status
docker ps -a | grep tpt
```