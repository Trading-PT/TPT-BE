package com.tradingpt.tpt_api.global.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DnsResolvers;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

/**
 * Redis 연결 설정
 * - 운영 환경: SSL/TLS 활성화 (ElastiCache Transit Encryption)
 * - JDK DNS Resolver 사용 (Netty DNS 문제 해결)
 * - Connection Pool 설정 지원
 */
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${spring.data.redis.timeout:2000ms}")
    private Duration timeout;

    @Value("${spring.data.redis.lettuce.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:2}")
    private int minIdle;

    /**
     * ClientResources 설정
     * - JDK DNS Resolver 사용하여 VPC/Docker 환경 DNS 해석 문제 해결
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources() {
        return ClientResources.builder()
                .dnsResolver(DnsResolvers.JVM_DEFAULT)  // Netty DNS 대신 JDK DNS 사용
                .build();
    }

    /**
     * Redis Connection Factory 설정
     * - SSL/TLS 지원 (운영 환경 ElastiCache)
     * - Connection Pool 설정
     * - JDK DNS Resolver 사용
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        // Redis 서버 설정
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);

        // Connection Pool 설정
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);

        // Lettuce Client 설정 빌더 (Pool 포함)
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                LettucePoolingClientConfiguration.builder()
                        .poolConfig(poolConfig)
                        .commandTimeout(timeout)
                        .clientResources(clientResources)
                        .clientOptions(ClientOptions.builder()
                                .socketOptions(SocketOptions.builder()
                                        .connectTimeout(Duration.ofSeconds(10))
                                        .build())
                                .build());

        // SSL/TLS 활성화 (운영 환경)
        if (sslEnabled) {
            builder.useSsl();
        }

        return new LettuceConnectionFactory(redisConfig, builder.build());
    }
}
