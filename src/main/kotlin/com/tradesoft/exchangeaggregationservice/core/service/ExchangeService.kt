package com.tradesoft.exchangeaggregationservice.core.service

import com.tradesoft.exchangeaggregationservice.core.business.OrderBookFilter
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeSymbol

interface ExchangeService {
    fun getExchangeType(): ExchangeType

    fun getSymbols(): List<ExchangeSymbol>

    fun getOrderBook(filter: OrderBookFilter): List<ExchangeOrderBook>
}
