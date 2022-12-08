package com.tradesoft.exchangeaggregationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component

@Component
@RefreshScope
@ConfigurationProperties(prefix = "pagination.config", ignoreUnknownFields = true)
class PaginationProperties {
    var metadataDefaultPageSize: Int = 100
}
