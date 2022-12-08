package com.tradesoft.exchangeaggregationservice.config.events

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component

@Component
@RefreshScope
@ConfigurationProperties("kafka.topics")
class KafkaTopic {
    lateinit var exchangeMetadata: String
}
