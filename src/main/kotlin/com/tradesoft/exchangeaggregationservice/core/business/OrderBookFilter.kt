package com.tradesoft.exchangeaggregationservice.core.business

import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType
import com.tradesoft.exchangeaggregationservice.core.business.enums.SortOrder

data class OrderBookFilter(
    val symbol: String?,
    val orderType: OrderType?,
    val sortOrder: SortOrder?
) {
    constructor() : this(null, null, null)
}
