package com.tradesoft.exchangeaggregationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component

@Component
@RefreshScope
@ConfigurationProperties(prefix = "cache", ignoreUnknownFields = true)
class CacheProperties {
    lateinit var exchangeMetadataCacheName: String
    lateinit var exchangeMetadataUploadCacheName: String
    var exchangeMetadataCacheDurationInMinutes: Long = 60L
    var exchangeMetadataUploadCacheDurationInMinutes: Long = 60L
}
