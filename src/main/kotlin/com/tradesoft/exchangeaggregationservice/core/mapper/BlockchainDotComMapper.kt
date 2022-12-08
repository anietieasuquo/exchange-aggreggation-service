package com.tradesoft.exchangeaggregationservice.core.mapper

import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType.ASK
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType.BID
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataEntity
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataUploadEntity
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBookItem
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComSymbol
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeMetadata
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeMetadataUpload
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeSymbol
import org.springframework.data.domain.Page

object BlockchainDotComMapper {
    fun Map<String, BlockchainDotComSymbol>.toExchangeSymbols(): List<ExchangeSymbol> =
        this.takeIf { it.isNotEmpty() }?.let {
            it.map { entry ->
                ExchangeSymbol(
                    symbol = entry.key,
                    counterCurrency = entry.value.counterCurrency,
                    baseCurrency = entry.value.baseCurrency
                )
            }
        } ?: emptyList()

    fun BlockchainDotComOrderBook.toExchangeOrderBook(
        orderType: OrderType?
    ): List<ExchangeOrderBook> = when (orderType) {
        BID -> this.bids.toOrderBookItems(
            symbol = this.symbol,
            orderType = BID
        )

        ASK -> this.asks.toOrderBookItems(
            symbol = this.symbol,
            orderType = ASK
        )

        else -> this.asks.toOrderBookItems(
            symbol = this.symbol,
            orderType = ASK
        ) + this.bids.toOrderBookItems(
            symbol = this.symbol,
            orderType = BID
        )
    }

    fun Page<ExchangeMetadataEntity>.toExchangeMetadata(): Page<ExchangeMetadata> =
        this.map {
            ExchangeMetadata(
                key = it.dataKey,
                value = it.dataValue,
                dateCreated = it.dateCreated,
                dateUpdated = it.dateUpdated
            )
        }

    fun Page<ExchangeMetadataUploadEntity>.toExchangeMetadataUpload(): Page<ExchangeMetadataUpload> =
        this.map {
            ExchangeMetadataUpload(
                id = it.id,
                status = it.status,
                dateCreated = it.dateCreated,
                dateCompleted = it.dateCompleted
            )
        }

    private fun List<BlockchainDotComOrderBookItem>.toOrderBookItems(
        symbol: String,
        orderType: OrderType
    ): List<ExchangeOrderBook> =
        this.map {
            ExchangeOrderBook(
                symbol = symbol,
                price = it.price,
                quantity = it.quantity,
                orderType = orderType
            )
        }

}
