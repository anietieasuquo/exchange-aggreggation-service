package com.tradesoft.exchangeaggregationservice.periphery.api

import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComSymbol
import feign.Param
import feign.RequestLine

interface BlockchainDotComServiceApi {
    @RequestLine("GET /symbols")
    fun getSymbols(): Map<String, BlockchainDotComSymbol>

    @RequestLine("GET /l3/{symbol}")
    fun getOrderBook(@Param("symbol") symbol: String): BlockchainDotComOrderBook
}
