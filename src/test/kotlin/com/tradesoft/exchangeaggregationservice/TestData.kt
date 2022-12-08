package com.tradesoft.exchangeaggregationservice

import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType.BLOCKCHAIN_DOT_COM
import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus.PENDING
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataEntity
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataUploadEntity
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBookItem
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComSymbol
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeMetadata
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeMetadataUpload
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDateTime

object TestData {
    fun makeBlockchainDotComSymbol(
        symbol: String,
        baseCurrency: String,
        counterCurrency: String
    ): Map<String, BlockchainDotComSymbol> = BlockchainDotComSymbol(
        counterCurrency = counterCurrency,
        baseCurrency = baseCurrency
    ).let { mapOf(symbol to it) }

    fun makeBlockchainDotComOrderBook(
        symbol: String,
        price: BigDecimal,
        quantity: BigDecimal
    ): BlockchainDotComOrderBook =
        BlockchainDotComOrderBook(
            symbol = symbol,
            bids = listOf(
                BlockchainDotComOrderBookItem(
                    price = price,
                    quantity = quantity
                )
            ),
            asks = listOf(
                BlockchainDotComOrderBookItem(
                    price = price + price,
                    quantity = quantity + quantity
                )
            )
        )

    fun makeExchangeMetadataEntityPage(
        id: Long,
        dateCreated: LocalDateTime,
        dateUpdated: LocalDateTime
    ): Page<ExchangeMetadataEntity> =
        PageImpl(
            listOf(
                ExchangeMetadataEntity(
                    id = id,
                    version = 2,
                    exchangeType = BLOCKCHAIN_DOT_COM,
                    dataKey = "dataKey",
                    dataValue = "dataValue",
                    uploadId = 10L,
                    dateCreated = dateCreated,
                    dateUpdated = dateUpdated
                )
            ), PageRequest.of(1, 100), 1
        )

    fun makeExchangeMetadataUploadEntityPage(
        id: Long,
        dateCreated: LocalDateTime,
        dateCompleted: LocalDateTime
    ): Page<ExchangeMetadataUploadEntity> =
        PageImpl(
            listOf(
                ExchangeMetadataUploadEntity(
                    id = id,
                    version = 2,
                    exchangeType = BLOCKCHAIN_DOT_COM,
                    status = PENDING,
                    file = null,
                    dateCreated = dateCreated,
                    dateCompleted = dateCompleted
                )
            ), PageRequest.of(1, 100), 1
        )

    fun makeExchangeMetadataPage(
        id: Long,
        dateCreated: LocalDateTime,
        dateUpdated: LocalDateTime
    ): Page<ExchangeMetadata> =
        PageImpl(
            listOf(
                ExchangeMetadata(
                    key = "dataKey",
                    value = "dataValue",
                    dateCreated = dateCreated,
                    dateUpdated = dateUpdated
                )
            ), PageRequest.of(1, 100), 1
        )

    fun makeExchangeMetadataUpload(
        id: Long,
        dateCreated: LocalDateTime,
        dateCompleted: LocalDateTime
    ): Page<ExchangeMetadataUpload> =
        PageImpl(
            listOf(
                ExchangeMetadataUpload(
                    id = id,
                    status = PENDING,
                    dateCreated = dateCreated,
                    dateCompleted = dateCompleted
                )
            ), PageRequest.of(1, 100), 1
        )
}
