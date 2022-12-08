package com.tradesoft.exchangeaggregationservice.periphery.boundary.entry

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BlockchainDotComOrderBook(
    val symbol: String,
    val bids: List<BlockchainDotComOrderBookItem>,
    val asks: List<BlockchainDotComOrderBookItem>
) {
}

data class BlockchainDotComOrderBookItem(
    @JsonProperty("px") val price: BigDecimal,
    @JsonProperty("qty") val quantity: BigDecimal
)
