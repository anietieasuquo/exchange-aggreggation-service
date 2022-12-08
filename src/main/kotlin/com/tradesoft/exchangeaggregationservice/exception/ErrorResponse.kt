package com.tradesoft.exchangeaggregationservice.exception

data class ErrorResponse(
    val errorType: String,
    val errorCategory: ErrorCategory,
    val technicalErrorMessage: String,
    val humanReadableMessage: String? = null,
    val retryable: Boolean? = false
)
