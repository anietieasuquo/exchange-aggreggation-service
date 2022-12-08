package com.tradesoft.exchangeaggregationservice.core.mapper

import com.tradesoft.exchangeaggregationservice.TestData.makeBlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.TestData.makeBlockchainDotComSymbol
import com.tradesoft.exchangeaggregationservice.TestData.makeExchangeMetadataEntityPage
import com.tradesoft.exchangeaggregationservice.TestData.makeExchangeMetadataPage
import com.tradesoft.exchangeaggregationservice.TestData.makeExchangeMetadataUpload
import com.tradesoft.exchangeaggregationservice.TestData.makeExchangeMetadataUploadEntityPage
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType.ASK
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType.BID
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataEntity
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataUploadEntity
import com.tradesoft.exchangeaggregationservice.core.mapper.BlockchainDotComMapper.toExchangeMetadata
import com.tradesoft.exchangeaggregationservice.core.mapper.BlockchainDotComMapper.toExchangeMetadataUpload
import com.tradesoft.exchangeaggregationservice.core.mapper.BlockchainDotComMapper.toExchangeOrderBook
import com.tradesoft.exchangeaggregationservice.core.mapper.BlockchainDotComMapper.toExchangeSymbols
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComSymbol
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeMetadata
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeMetadataUpload
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeSymbol
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import java.math.BigDecimal.ONE
import java.math.BigDecimal.TEN
import java.time.LocalDateTime

class BlockchainDotComMapperTest {

    @Test
    fun `should map to exchange symbol list when map is provided empty`() {
        // GIVEN
        val map: Map<String, BlockchainDotComSymbol> = emptyMap()

        // WHEN
        val list: List<ExchangeSymbol> = map.toExchangeSymbols()

        // THEN
        assertThat(list).isEmpty()
    }

    @Test
    fun `should map to exchange symbol list when provided map is not empty`() {
        // GIVEN
        val symbol = "BTC-USD"
        val baseCurrency = "BTC"
        val counterCurrency = "USD"
        val map: Map<String, BlockchainDotComSymbol> = makeBlockchainDotComSymbol(
            symbol = symbol,
            baseCurrency = baseCurrency,
            counterCurrency = counterCurrency
        )

        // WHEN
        val list: List<ExchangeSymbol> = map.toExchangeSymbols()

        // THEN
        val expectedList = listOf(
            ExchangeSymbol(
                symbol = symbol,
                counterCurrency = counterCurrency,
                baseCurrency = baseCurrency
            )
        )
        assertThat(list).containsExactlyInAnyOrderElementsOf(expectedList)
    }

    @Test
    fun `should map to order book when order type is bid`() {
        // GIVEN
        val orderType: OrderType = BID
        val symbol = "BTC-USD"
        val price = TEN
        val quantity = ONE
        val orderBook: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = symbol,
            price = price,
            quantity = quantity
        )

        // WHEN
        val list: List<ExchangeOrderBook> = orderBook.toExchangeOrderBook(orderType)

        // THEN
        val expectedList = listOf(
            ExchangeOrderBook(
                symbol = symbol,
                price = price,
                quantity = quantity,
                orderType = orderType
            )
        )
        assertThat(list).containsExactlyInAnyOrderElementsOf(expectedList)
    }

    @Test
    fun `should map to order book when order type is ask`() {
        // GIVEN
        val orderType: OrderType = ASK
        val symbol = "BTC-USD"
        val price = TEN
        val quantity = ONE
        val orderBook: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = symbol,
            price = price,
            quantity = quantity
        )

        // WHEN
        val list: List<ExchangeOrderBook> = orderBook.toExchangeOrderBook(orderType)

        // THEN
        val expectedList = listOf(
            ExchangeOrderBook(
                symbol = symbol,
                price = price + price,
                quantity = quantity + quantity,
                orderType = orderType
            )
        )
        assertThat(list).containsExactlyInAnyOrderElementsOf(expectedList)
    }

    @Test
    fun `should map to order book when order type is not provided`() {
        // GIVEN
        val orderType: OrderType? = null
        val symbol = "BTC-USD"
        val price = TEN
        val quantity = ONE
        val orderBook: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = symbol,
            price = price,
            quantity = quantity
        )

        // WHEN
        val list: List<ExchangeOrderBook> = orderBook.toExchangeOrderBook(orderType)

        // THEN
        val expectedList = listOf(
            ExchangeOrderBook(
                symbol = symbol,
                price = price,
                quantity = quantity,
                orderType = BID
            ),
            ExchangeOrderBook(
                symbol = symbol,
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            )
        )
        assertThat(list).containsExactlyInAnyOrderElementsOf(expectedList)
    }

    @Test
    fun `should map to exchange metadata`() {
        // GIVEN
        val id = 10L
        val dateCreated = LocalDateTime.now()
        val dateUpdated = LocalDateTime.now()
        val metadataPage: Page<ExchangeMetadataEntity> = makeExchangeMetadataEntityPage(
            id = id,
            dateCreated = dateCreated,
            dateUpdated = dateUpdated
        )

        // WHEN
        val page: Page<ExchangeMetadata> = metadataPage.toExchangeMetadata()

        // THEN
        val expectedPage: Page<ExchangeMetadata> = makeExchangeMetadataPage(
            id = id,
            dateCreated = dateCreated,
            dateUpdated = dateUpdated
        )
        assertThat(page).containsExactlyInAnyOrderElementsOf(expectedPage)
    }

    @Test
    fun `should map to exchange metadata upload`() {
        // GIVEN
        val id = 10L
        val dateCreated = LocalDateTime.now()
        val dateCompleted = LocalDateTime.now()
        val metadataPage: Page<ExchangeMetadataUploadEntity> = makeExchangeMetadataUploadEntityPage(
            id = id,
            dateCreated = dateCreated,
            dateCompleted = dateCompleted
        )

        // WHEN
        val page: Page<ExchangeMetadataUpload> = metadataPage.toExchangeMetadataUpload()

        // THEN
        val expectedPage: Page<ExchangeMetadataUpload> = makeExchangeMetadataUpload(
            id = id,
            dateCreated = dateCreated,
            dateCompleted = dateCompleted
        )
        assertThat(page).containsExactlyInAnyOrderElementsOf(expectedPage)
    }
}
