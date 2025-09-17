# Repository Guidelines

## Project Structure & Module Organization
This Spring Boot 3.5 service lives under `src/main/java/com/tradingpt/tpt_api`, with feature-specific packages in `domain` (auth, feedbackrequest, feedbackresponse, trading summaries, user) and shared infrastructure in `global`. Each domain keeps controllers, services, repositories, DTOs, and exceptions grouped together. QueryDSL generates Q-types into `src/main/generated`; let Gradle manage that directory and avoid committing its artifacts. Configuration files reside in `src/main/resources` (`application.yml`, `application-local.yml`, `application-dev.yml`). Integration tests mirror production packages in `src/test/java`. Deployment and health-check helpers are in `scripts/` alongside the `Dockerfile` and CodeDeploy `appspec.yml`.

## Build, Test, and Development Commands
- `./gradlew clean build`: compile sources, run the entire JUnit suite, and package the runnable jar.
- `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`: launch the API with local overrides for manual verification.
- `./gradlew test`: execute unit and slice tests only; use when iterating quickly.
- `./gradlew clean`: purge compiled classes and regenerated QueryDSL artifacts before switching branches.

## Coding Style & Naming Conventions
Target Java 17 with four-space indentation and Lombok for boilerplate. Use descriptive package names (`domain.<feature>`) and suffix classes by role (`*Controller`, `*Service`, `*Repository`, `*Exception`). Favor constructor injection, record DTOs, and immutable value objects where practical. JSON fields should mirror camelCase Java properties; document any deviations via `@JsonProperty`.

## Testing Guidelines
JUnit 5 with Spring Boot and Spring Security support is preconfigured. Place test fixtures under the matching package in `src/test/java` and name test classes `<ClassName>Test`. Prefer `@SpringBootTest` only for cross-layer scenarios; otherwise use `@DataJpaTest`, `@WebMvcTest`, or security slices to keep builds fast. Mock external integrations (AWS S3, Redis, mail) using Spring test utilities. Run `./gradlew test` before pushing.

## Commit & Pull Request Guidelines
Follow the existing conventional-emoji style: `<emoji> type: short, imperative summary` (e.g., `:sparkles: feat: add weekly summary API`). Reference tickets or branches using the `feature/#issue` pattern when relevant. Keep commits scoped to a single concern and include generated migrations or QueryDSL classes only when necessary. Pull requests should state intent, summarize functional changes, link issues, and add screenshots or API samples when UI or contract outputs change. Checklist items: tests passing, profile configs updated, and deployment scripts reviewed if behavior shifts.

## Security & Configuration Tips
Store secrets in environment variables or secrets managers; never hardcode credentials in `application*.yml`. Verify AWS, Redis, and OAuth client properties per environment before deploying. When adding new external integrations, document required IAM roles and rate limits in the PR description.
