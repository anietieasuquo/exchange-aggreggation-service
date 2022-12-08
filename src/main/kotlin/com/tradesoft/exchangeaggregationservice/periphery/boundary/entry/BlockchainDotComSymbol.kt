package com.tradesoft.exchangeaggregationservice.periphery.boundary.entry

import com.fasterxml.jackson.annotation.JsonProperty

data class BlockchainDotComSymbol(
    @JsonProperty("base_currency") val baseCurrency: String,
    @JsonProperty("counter_currency") val counterCurrency: String
)
