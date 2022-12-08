package com.tradesoft.exchangeaggregationservice.core.service.helper

import com.tradesoft.exchangeaggregationservice.AbstractIntegrationTest
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType.BLOCKCHAIN_DOT_COM
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate

class CacheHelperTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @Autowired
    private lateinit var cacheHelper: CacheHelper

    companion object {
        private const val CACHE_KEY = "exchange-metadata-cache::BLOCKCHAIN_DOT_COM_1_2"
    }

    @BeforeEach
    fun init() {
        redisTemplate.opsForValue().set(CACHE_KEY, "hello")
        val checkCache = redisTemplate.opsForValue().get(CACHE_KEY)
        assertThat(checkCache).isEqualTo("hello")
    }

    @Test
    fun evictCacheForExchange_ShouldEvictCacheForExchange() {
        // GIVEN
        val exchangeType: ExchangeType = BLOCKCHAIN_DOT_COM
        val cacheName = "exchange-metadata-cache"

        // WHEN
        cacheHelper.evictCacheForExchange(
            cacheName = cacheName,
            exchangeType = exchangeType
        )

        // THEN
        val checkCache = redisTemplate.opsForValue().get(CACHE_KEY)
        assertThat(checkCache).isNull()
    }
}
