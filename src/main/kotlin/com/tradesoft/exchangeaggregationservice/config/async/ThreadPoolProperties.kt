package com.tradesoft.exchangeaggregationservice.config.async

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component

@Component
@RefreshScope
@ConfigurationProperties(prefix = "concurrency.thread.pool", ignoreUnknownFields = true)
class ThreadPoolProperties {
    var corePoolSize: Int = 3
    var maxPoolSize: Int = 10
    var keepAliveTimeInSeconds: Long = 30
    var queueCapacity: Int = 30
    var threadNamePrefix: String = "exchange-metadata-executor-"
}
