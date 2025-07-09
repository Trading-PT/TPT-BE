import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	kotlin("kapt") version "1.9.25" // QueryDSL을 위한 kapt 플러그인
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot Starters
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator") // Actuator 추가
	implementation("org.springframework.boot:spring-boot-starter-data-redis") // Redis 추가

	// Flyway - 데이터베이스 마이그레이션
	implementation("org.flywaydb:flyway-core:10.21.0")
	implementation("org.flywaydb:flyway-database-postgresql:10.21.0") // PostgreSQL 지원

	// Kotlin Support
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Jackson 추가 모듈
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // Java 8 Time 지원
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8") // Optional 등 지원

	// QueryDSL
	implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
	implementation("com.querydsl:querydsl-core:5.1.0")
	kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
	kapt("jakarta.annotation:jakarta.annotation-api")
	kapt("jakarta.persistence:jakarta.persistence-api")

	// SpringDoc OpenAPI (Swagger)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	implementation("org.springdoc:springdoc-openapi-starter-common:2.6.0")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// Logging
	implementation("net.logstash.logback:logstash-logback-encoder:8.0") // JSON 로깅

	// Database
	runtimeOnly("org.postgresql:postgresql")

	// Development
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Kotlin 컴파일러 옵션
kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
	}
}

// JPA 엔티티 클래스 open
allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

// 테스트 설정
tasks.withType<Test> {
	useJUnitPlatform()
}

// Kotlin 컴파일 태스크 설정
tasks.withType<KotlinCompile> {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
	}
}

// QueryDSL Q클래스 생성 경로 설정
kotlin {
	sourceSets.main {
		kotlin.srcDirs("build/generated/source/kapt/main")
	}
}

// KAPT 설정
kapt {
	keepJavacAnnotationProcessors = true
	arguments {
		arg("querydsl.entityAccessors", "true")
		arg("querydsl.useKotlinCollections", "true")
	}
}

// Clean 시 생성된 Q클래스도 삭제
tasks.clean {
	doLast {
		file("build/generated").deleteRecursively()
	}
}