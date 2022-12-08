package com.tradesoft.exchangeaggregationservice.exception

class BusinessException(override val errorResponse: ErrorResponse) : RuntimeException(errorResponse.toString()),
    ErrorResponseException {
}
