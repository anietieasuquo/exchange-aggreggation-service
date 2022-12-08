package com.tradesoft.exchangeaggregationservice.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.WRAP_EXCEPTIONS
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.EVERYTHING
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.lettuce.core.ClientOptions
import io.lettuce.core.ClientOptions.DisconnectedBehavior.REJECT_COMMANDS
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration


@AutoConfigureAfter(RedisAutoConfiguration::class)
@EnableCaching
@Configuration
class CacheConfiguration(
    val cacheProperties: CacheProperties,
    val redisProperties: RedisProperties
) {

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisProperties.host, redisProperties.port)
        val jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().build()
        val factory = JedisConnectionFactory(config, jedisClientConfiguration)
        factory.afterPropertiesSet()
        return factory
    }

    @Bean
    fun redisTemplate(connectionFactory: JedisConnectionFactory): RedisTemplate<String, Any> {
        val template: RedisTemplate<String, Any> = RedisTemplate()
        template.setConnectionFactory(connectionFactory)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer(getObjectMapper())
        return template
    }

    @Bean
    fun clientOptions(): ClientOptions {
        return ClientOptions.builder()
            .disconnectedBehavior(REJECT_COMMANDS)
            .autoReconnect(true)
            .build()
    }

    @Bean
    fun cacheManager(connectionFactory: JedisConnectionFactory): CacheManager {
        return RedisCacheManagerBuilder
            .fromConnectionFactory(connectionFactory)
            .withInitialCacheConfigurations(
                mapOf(
                    cacheProperties.exchangeMetadataCacheName to RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(cacheProperties.exchangeMetadataCacheDurationInMinutes))
                        .serializeValuesWith(fromSerializer(GenericJackson2JsonRedisSerializer(getObjectMapper())))
                        .serializeKeysWith(fromSerializer(StringRedisSerializer()))
                        .disableCachingNullValues(),
                    cacheProperties.exchangeMetadataUploadCacheName to RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(cacheProperties.exchangeMetadataUploadCacheDurationInMinutes))
                        .serializeValuesWith(fromSerializer(GenericJackson2JsonRedisSerializer(getObjectMapper())))
                        .serializeKeysWith(fromSerializer(StringRedisSerializer()))
                        .disableCachingNullValues()
                )
            )
            .enableStatistics()
            .build()
    }

    private fun getObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModules(
            JavaTimeModule(),
            ParameterNamesModule()
        ).activateDefaultTyping(
            objectMapper.polymorphicTypeValidator,
            EVERYTHING,
            JsonTypeInfo.As.PROPERTY
        )
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(WRAP_EXCEPTIONS, true)
        return objectMapper
    }
}
