package com.tradesoft.exchangeaggregationservice.core.service

import com.tradesoft.exchangeaggregationservice.config.async.AsyncProvider
import com.tradesoft.exchangeaggregationservice.core.business.OrderBookFilter
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType.BLOCKCHAIN_DOT_COM
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType
import com.tradesoft.exchangeaggregationservice.core.business.enums.SortOrder.ASC
import com.tradesoft.exchangeaggregationservice.core.business.enums.SortOrder.DESC
import com.tradesoft.exchangeaggregationservice.core.mapper.BlockchainDotComMapper.toExchangeOrderBook
import com.tradesoft.exchangeaggregationservice.core.mapper.BlockchainDotComMapper.toExchangeSymbols
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeSymbol
import com.tradesoft.exchangeaggregationservice.periphery.clients.BlockchainDotComServiceClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BlockchainDotComService(
    private val blockchainDotComServiceClient: BlockchainDotComServiceClient,
    private val asyncProvider: AsyncProvider
) : ExchangeService {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun getExchangeType(): ExchangeType = BLOCKCHAIN_DOT_COM

    override fun getSymbols(): List<ExchangeSymbol> =
        blockchainDotComServiceClient.getSymbols().toExchangeSymbols()
            .also { log.debug("Symbols response from Blockchain.com: $it") }

    override fun getOrderBook(filter: OrderBookFilter): List<ExchangeOrderBook> =
        filter.let {
            if (it.symbol.isNullOrEmpty()) {
                fetchAllSymbolsOrderBook(orderType = filter.orderType)
            } else {
                fetchSymbolsOrderBook(
                    symbol = it.symbol,
                    orderType = filter.orderType
                )
            }
        }.let { orderBooks ->
            when (filter.sortOrder) {
                ASC -> orderBooks.sortedBy { orderBook -> orderBook.symbol }

                DESC -> orderBooks.sortedByDescending { orderBook -> orderBook.symbol }

                else -> orderBooks
            }
        }

    private fun fetchAllSymbolsOrderBook(orderType: OrderType?): List<ExchangeOrderBook> =
        blockchainDotComServiceClient.getSymbols().takeIf { it.isNotEmpty() }?.let { symbols ->
            runBlocking(asyncProvider.provideDefaultCoroutineDispatcher()) {
                symbols.entries.map { symbol ->
                    async {
                        fetchSymbolsOrderBook(
                            symbol = symbol.key,
                            orderType = orderType
                        )
                    }
                }.awaitAll()
            }.flatten()
        } ?: emptyList()

    private fun fetchSymbolsOrderBook(symbol: String, orderType: OrderType?): List<ExchangeOrderBook> =
        blockchainDotComServiceClient.getOrderBook(symbol).toExchangeOrderBook(orderType = orderType)
}
