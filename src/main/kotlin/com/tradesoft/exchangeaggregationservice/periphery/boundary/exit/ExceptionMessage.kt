package com.tradesoft.exchangeaggregationservice.periphery.boundary.exit

import java.time.LocalDateTime

data class ExceptionMessage(
    val message: String,
    val error: String,
    val timestamp: LocalDateTime? = LocalDateTime.now(),
    val status: Int,
    val path: String
)
