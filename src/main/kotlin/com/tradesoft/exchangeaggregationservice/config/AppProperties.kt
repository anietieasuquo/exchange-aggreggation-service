package com.tradesoft.exchangeaggregationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component

@Component
@RefreshScope
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true)
class AppProperties {
    var title: String? = ""
    var description: String? = ""
    var version: String? = ""
    var tosUrl: String? = ""
    var license: String? = ""
    var licenseUrl: String? = ""
    var authorName: String? = ""
    var authorUrl: String? = ""
    var authorEmail: String? = ""
}
