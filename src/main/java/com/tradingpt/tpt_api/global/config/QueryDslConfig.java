package com.tradingpt.tpt_api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * QueryDSL 설정 클래스
 * JPAQueryFactory Bean을 등록하여 QueryDSL을 사용할 수 있도록 설정
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory Bean 등록
     * QueryDSL에서 JPQL을 생성하고 실행하기 위한 팩토리
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}