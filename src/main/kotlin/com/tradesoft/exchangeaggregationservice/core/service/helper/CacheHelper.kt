package com.tradesoft.exchangeaggregationservice.core.service.helper

import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class CacheHelper(
    private val cacheManager: CacheManager,
    private val redisTemplate: RedisTemplate<String, Any>
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun evictCacheForExchange(cacheName: String, exchangeType: ExchangeType) {
        val metadataCache = cacheManager.getCache(cacheName) ?: return
        log.debug("Cache name found: ${metadataCache.name}")

        redisTemplate.keys("${cacheName}::${exchangeType}*").also {
            log.debug("Found cache keys for exchange: $exchangeType, keys: ${it.joinToString(",")}")
        }.forEach {
            log.debug("Evicting cache key: $it for exchange: $exchangeType")
            redisTemplate.opsForValue().getAndDelete(it)
        }
    }
}
