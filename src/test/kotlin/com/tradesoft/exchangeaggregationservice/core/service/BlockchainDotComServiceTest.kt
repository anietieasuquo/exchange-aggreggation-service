package com.tradesoft.exchangeaggregationservice.core.service

import com.nhaarman.mockitokotlin2.*
import com.tradesoft.exchangeaggregationservice.TestData.makeBlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.TestData.makeBlockchainDotComSymbol
import com.tradesoft.exchangeaggregationservice.config.async.AsyncProvider
import com.tradesoft.exchangeaggregationservice.config.async.ThreadPoolProperties
import com.tradesoft.exchangeaggregationservice.core.business.OrderBookFilter
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType.ASK
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType.BID
import com.tradesoft.exchangeaggregationservice.core.business.enums.SortOrder.ASC
import com.tradesoft.exchangeaggregationservice.core.business.enums.SortOrder.DESC
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComSymbol
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeSymbol
import com.tradesoft.exchangeaggregationservice.periphery.clients.BlockchainDotComServiceClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal.ONE
import java.math.BigDecimal.TEN

@ExtendWith(MockitoExtension::class)
class BlockchainDotComServiceTest {

    private val blockchainDotComServiceClient: BlockchainDotComServiceClient = mock()

    private val threadPoolProperties: ThreadPoolProperties = ThreadPoolProperties()

    private val asyncProvider: AsyncProvider = AsyncProvider(threadPoolProperties)

    private val blockchainDotComService: BlockchainDotComService = BlockchainDotComService(
        blockchainDotComServiceClient = blockchainDotComServiceClient,
        asyncProvider = asyncProvider
    )

    @Test
    fun `should fetch and return symbol list`() {
        // GIVEN
        val symbol = "BTC-USD"
        val baseCurrency = "BTC"
        val counterCurrency = "USD"
        val expectedExchangeSymbols: Map<String, BlockchainDotComSymbol> = makeBlockchainDotComSymbol(
            symbol = symbol,
            baseCurrency = baseCurrency,
            counterCurrency = counterCurrency
        )
        whenever(blockchainDotComServiceClient.getSymbols()).thenReturn(expectedExchangeSymbols)

        // WHEN
        val list: List<ExchangeSymbol> = blockchainDotComService.getSymbols()

        // THEN
        val expectedList: List<ExchangeSymbol> = listOf(
            ExchangeSymbol(
                symbol = symbol,
                baseCurrency = baseCurrency,
                counterCurrency = counterCurrency
            )
        )
        assertThat(list).containsExactlyInAnyOrderElementsOf(expectedList)
    }

    @Test
    fun `should fetch and return order book when filter is not provided`() {
        // GIVEN
        val symbol = "BTC-USD"
        val baseCurrency = "BTC"
        val counterCurrency = "USD"
        val price = TEN
        val quantity = ONE
        val filter = OrderBookFilter()
        val expectedExchangeSymbols: Map<String, BlockchainDotComSymbol> = makeBlockchainDotComSymbol(
            symbol = symbol,
            baseCurrency = baseCurrency,
            counterCurrency = counterCurrency
        )
        val expectedBlockchainDotComOrderBook: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = symbol,
            price = price,
            quantity = quantity
        )
        whenever(blockchainDotComServiceClient.getSymbols()).thenReturn(expectedExchangeSymbols)
        blockchainDotComServiceClient.stub {
            onBlocking { getOrderBook(symbol) }.doReturn(expectedBlockchainDotComOrderBook)
        }

        // WHEN
        val list: List<ExchangeOrderBook> = blockchainDotComService.getOrderBook(filter)

        // THEN
        val expectedList: List<ExchangeOrderBook> = listOf(
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
    fun `should fetch and return order book when order type is ask`() {
        // GIVEN
        val symbol = "BTC-USD"
        val price = TEN
        val quantity = ONE
        val filter = OrderBookFilter(
            symbol = symbol,
            orderType = ASK,
            sortOrder = DESC
        )
        val expectedBlockchainDotComOrderBook: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = symbol,
            price = price,
            quantity = quantity
        )

        blockchainDotComServiceClient.stub {
            onBlocking { getOrderBook(symbol) }.doReturn(expectedBlockchainDotComOrderBook)
        }

        // WHEN
        val list: List<ExchangeOrderBook> = blockchainDotComService.getOrderBook(filter)

        // THEN
        val expectedList: List<ExchangeOrderBook> = listOf(
            ExchangeOrderBook(
                symbol = symbol,
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            )
        )
        assertThat(list).containsExactlyElementsOf(expectedList)
        verify(blockchainDotComServiceClient, times(0)).getSymbols()
    }

    @Test
    fun `should fetch and return order book when order type is bid`() {
        // GIVEN
        val symbol = "BTC-USD"
        val price = TEN
        val quantity = ONE
        val filter = OrderBookFilter(
            symbol = symbol,
            orderType = BID,
            sortOrder = DESC
        )
        val expectedBlockchainDotComOrderBook: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = symbol,
            price = price,
            quantity = quantity
        )

        blockchainDotComServiceClient.stub {
            onBlocking { getOrderBook(symbol) }.doReturn(expectedBlockchainDotComOrderBook)
        }

        // WHEN
        val list: List<ExchangeOrderBook> = blockchainDotComService.getOrderBook(filter)

        // THEN
        val expectedList: List<ExchangeOrderBook> = listOf(
            ExchangeOrderBook(
                symbol = symbol,
                price = price,
                quantity = quantity,
                orderType = BID
            )
        )
        assertThat(list).containsExactlyElementsOf(expectedList)
        verify(blockchainDotComServiceClient, times(0)).getSymbols()
    }

    @Test
    fun `should fetch and return order book when sort type is descending`() {
        // GIVEN
        val baseCurrency = "BTC"
        val counterCurrency = "USD"
        val price = TEN
        val quantity = ONE
        val filter = OrderBookFilter(
            symbol = null,
            orderType = ASK,
            sortOrder = DESC
        )
        val expectedBlockchainDotComOrderBookB: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "BTC-USD",
            price = price,
            quantity = quantity
        )

        val expectedBlockchainDotComOrderBookZ: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "ZCH-USD",
            price = price,
            quantity = quantity
        )

        val expectedBlockchainDotComOrderBookA: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "ACH-USD",
            price = price,
            quantity = quantity
        )
        val exchangeSymbol = BlockchainDotComSymbol(
            counterCurrency = counterCurrency,
            baseCurrency = baseCurrency
        )
        val expectedExchangeSymbols: Map<String, BlockchainDotComSymbol> = mapOf(
            "BTC-USD" to exchangeSymbol,
            "ZCH-USD" to exchangeSymbol,
            "ACH-USD" to exchangeSymbol
        )
        whenever(blockchainDotComServiceClient.getSymbols()).thenReturn(expectedExchangeSymbols)
        blockchainDotComServiceClient.stub {
            onBlocking { getOrderBook("BTC-USD") }.doReturn(expectedBlockchainDotComOrderBookB)
            onBlocking { getOrderBook("ZCH-USD") }.doReturn(expectedBlockchainDotComOrderBookZ)
            onBlocking { getOrderBook("ACH-USD") }.doReturn(expectedBlockchainDotComOrderBookA)
        }

        // WHEN
        val list: List<ExchangeOrderBook> = blockchainDotComService.getOrderBook(filter)

        // THEN
        val expectedList: List<ExchangeOrderBook> = listOf(
            ExchangeOrderBook(
                symbol = "ZCH-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            ),
            ExchangeOrderBook(
                symbol = "BTC-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            ),
            ExchangeOrderBook(
                symbol = "ACH-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            )
        )
        assertThat(list).containsExactlyElementsOf(expectedList)
    }

    @Test
    fun `should fetch and return order book when sort type is ascending`() {
        // GIVEN
        val baseCurrency = "BTC"
        val counterCurrency = "USD"
        val price = TEN
        val quantity = ONE
        val filter = OrderBookFilter(
            symbol = null,
            orderType = ASK,
            sortOrder = ASC
        )
        val expectedBlockchainDotComOrderBookB: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "BTC-USD",
            price = price,
            quantity = quantity
        )

        val expectedBlockchainDotComOrderBookZ: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "ZCH-USD",
            price = price,
            quantity = quantity
        )

        val expectedBlockchainDotComOrderBookA: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "ACH-USD",
            price = price,
            quantity = quantity
        )
        val exchangeSymbol = BlockchainDotComSymbol(
            counterCurrency = counterCurrency,
            baseCurrency = baseCurrency
        )
        val expectedExchangeSymbols: Map<String, BlockchainDotComSymbol> = mapOf(
            "BTC-USD" to exchangeSymbol,
            "ZCH-USD" to exchangeSymbol,
            "ACH-USD" to exchangeSymbol
        )
        whenever(blockchainDotComServiceClient.getSymbols()).thenReturn(expectedExchangeSymbols)
        blockchainDotComServiceClient.stub {
            onBlocking { getOrderBook("BTC-USD") }.doReturn(expectedBlockchainDotComOrderBookB)
            onBlocking { getOrderBook("ZCH-USD") }.doReturn(expectedBlockchainDotComOrderBookZ)
            onBlocking { getOrderBook("ACH-USD") }.doReturn(expectedBlockchainDotComOrderBookA)
        }

        // WHEN
        val list: List<ExchangeOrderBook> = blockchainDotComService.getOrderBook(filter)

        // THEN
        val expectedList: List<ExchangeOrderBook> = listOf(
            ExchangeOrderBook(
                symbol = "ACH-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            ),
            ExchangeOrderBook(
                symbol = "BTC-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            ),
            ExchangeOrderBook(
                symbol = "ZCH-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            )
        )
        assertThat(list).containsExactlyElementsOf(expectedList)
    }

    @Test
    fun `should fetch and return order book when no order type is provided`() {
        // GIVEN
        val baseCurrency = "BTC"
        val counterCurrency = "USD"
        val price = TEN
        val quantity = ONE
        val filter = OrderBookFilter(
            symbol = null,
            orderType = null,
            sortOrder = ASC
        )
        val expectedBlockchainDotComOrderBookB: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "BTC-USD",
            price = price,
            quantity = quantity
        )

        val expectedBlockchainDotComOrderBookZ: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "ZCH-USD",
            price = price,
            quantity = quantity
        )

        val expectedBlockchainDotComOrderBookA: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = "ACH-USD",
            price = price,
            quantity = quantity
        )
        val exchangeSymbol = BlockchainDotComSymbol(
            counterCurrency = counterCurrency,
            baseCurrency = baseCurrency
        )
        val expectedExchangeSymbols: Map<String, BlockchainDotComSymbol> = mapOf(
            "BTC-USD" to exchangeSymbol,
            "ZCH-USD" to exchangeSymbol,
            "ACH-USD" to exchangeSymbol
        )
        whenever(blockchainDotComServiceClient.getSymbols()).thenReturn(expectedExchangeSymbols)
        blockchainDotComServiceClient.stub {
            onBlocking { getOrderBook("BTC-USD") }.doReturn(expectedBlockchainDotComOrderBookB)
            onBlocking { getOrderBook("ZCH-USD") }.doReturn(expectedBlockchainDotComOrderBookZ)
            onBlocking { getOrderBook("ACH-USD") }.doReturn(expectedBlockchainDotComOrderBookA)
        }

        // WHEN
        val list: List<ExchangeOrderBook> = blockchainDotComService.getOrderBook(filter)

        // THEN
        val expectedList: List<ExchangeOrderBook> = listOf(
            ExchangeOrderBook(
                symbol = "ACH-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            ),
            ExchangeOrderBook(
                symbol = "ACH-USD",
                price = price,
                quantity = quantity,
                orderType = BID
            ),
            ExchangeOrderBook(
                symbol = "BTC-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            ),
            ExchangeOrderBook(
                symbol = "BTC-USD",
                price = price,
                quantity = quantity,
                orderType = BID
            ),
            ExchangeOrderBook(
                symbol = "ZCH-USD",
                price = price + price,
                quantity = quantity + quantity,
                orderType = ASK
            ),
            ExchangeOrderBook(
                symbol = "ZCH-USD",
                price = price,
                quantity = quantity,
                orderType = BID
            )
        )
        assertThat(list).containsExactlyElementsOf(expectedList)
    }
}
