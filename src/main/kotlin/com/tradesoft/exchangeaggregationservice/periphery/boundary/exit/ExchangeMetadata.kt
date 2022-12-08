package com.tradesoft.exchangeaggregationservice.periphery.boundary.exit

import java.io.Serializable
import java.time.LocalDateTime

data class ExchangeMetadata(
    val key: String,
    val value: String,
    val dateCreated: LocalDateTime,
    val dateUpdated: LocalDateTime
) : Serializable
