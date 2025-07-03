# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

If the user's prompt starts with “EP:”, then the user wants to enhance the prompt. Read the PROMPT_ENHANCER.md file and
follow the guidelines to enhance the user's prompt. Show the user the enhancement and get their permission to run it
before taking action on the enhanced prompt. The enhanced prompts will follow the language of the original prompt (e.g.,
Korean prompt input will output Korean prompt enhancements, English prompt input will output English prompt
enhancements, etc.)

## Project Overview

TPT-BE is a Spring Boot backend application written in Kotlin that provides RESTful APIs with authentication and data persistence capabilities.

- **Framework**: Spring Boot 3.5.3
- **Language**: Kotlin 1.9.25
- **Build Tool**: Gradle with Kotlin DSL
- **Java Version**: 17
- **Database**: PostgreSQL with JPA/Hibernate

## Essential Commands

### Development
```bash
# Run the application
./gradlew bootRun

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.example.tpt.SomeTestClass"

# Run a specific test method
./gradlew test --tests "com.example.tpt.SomeTestClass.testMethod"

# Clean build artifacts
./gradlew clean

# Build without running tests
./gradlew build -x test
```

### Code Quality
```bash
# Check Kotlin code style (if ktlint is configured)
./gradlew ktlintCheck

# Format Kotlin code (if ktlint is configured)
./gradlew ktlintFormat

# Run static code analysis (if detekt is configured)
./gradlew detekt
```

## Architecture & Structure

### Package Organization
The codebase follows standard Spring Boot conventions with the base package `com.example.tpt`:

```
src/main/kotlin/com/example/tpt/
├── config/          # Spring configuration classes
├── controller/      # REST API endpoints
├── service/         # Business logic layer
├── repository/      # Data access layer (JPA repositories)
├── entity/          # JPA entities
├── dto/             # Data Transfer Objects
├── security/        # Security configuration and utilities
└── TptApplication.kt # Main application class
```

### Key Technologies & Patterns

1. **Security Architecture**
    - Spring Security with OAuth2 client support
    - JWT token-based authentication
    - Security configuration in the `security` package

2. **API Design**
    - RESTful endpoints following standard conventions
    - API versioning (e.g., `/api/v1/`)
    - Request validation using Spring Validation

3. **Data Layer**
    - JPA entities with Kotlin data classes
    - Spring Data JPA repositories
    - PostgreSQL as the primary database

4. **Configuration**
    - Environment-specific properties in `application.properties`
    - Spring profiles for different environments

### Development Workflow

1. **Issue Management**: Use GitHub issue templates in `.github/ISSUE_TEMPLATE/`
2. **Pull Requests**: Follow the comprehensive checklist in `.github/PULL_REQUEST_TEMPLATE.md`
3. **Commits**: Use conventional commit format
4. **Labels**: Follow the Korean labeling system defined in `.github/LABELS.md`

### Important Notes

- The project uses Korean language for documentation and issue templates
- Spring Boot DevTools is included for hot reloading during development
- JPA entities require the `@Entity`, `@MappedSuperclass`, or `@Embeddable` annotations to be open (configured in build.gradle.kts)
- Kotlin compiler is configured with strict JSR-305 nullability checks