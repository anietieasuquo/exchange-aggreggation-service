package com.tradesoft.exchangeaggregationservice.periphery.boundary.exit

import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType
import java.math.BigDecimal

data class ExchangeOrderBook(
    val symbol: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val orderType: OrderType
)
