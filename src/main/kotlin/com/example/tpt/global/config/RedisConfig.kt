package com.example.tpt.global.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

	@Bean
	fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
		return RedisTemplate<String, Any>().apply {
			setConnectionFactory(connectionFactory)
			keySerializer = StringRedisSerializer()
			valueSerializer = GenericJackson2JsonRedisSerializer()
			hashKeySerializer = StringRedisSerializer()
			hashValueSerializer = GenericJackson2JsonRedisSerializer()
		}
	}

	@Bean
	fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
		val config = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofHours(1))
			.disableCachingNullValues()

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(config)
			.build()
	}
}