package com.tradesoft.exchangeaggregationservice.periphery.boundary.exit

data class ExchangeSymbol(
    val symbol: String,
    val baseCurrency: String,
    val counterCurrency: String
)
