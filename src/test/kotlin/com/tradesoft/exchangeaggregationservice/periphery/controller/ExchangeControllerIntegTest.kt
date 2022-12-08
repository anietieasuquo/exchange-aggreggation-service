package com.tradesoft.exchangeaggregationservice.periphery.controller

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.tradesoft.exchangeaggregationservice.AbstractIntegrationTest
import com.tradesoft.exchangeaggregationservice.TestData.makeBlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.TestData.makeBlockchainDotComSymbol
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType.BLOCKCHAIN_DOT_COM
import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus.PENDING
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComSymbol
import com.tradesoft.exchangeaggregationservice.periphery.controller.ExchangeController.Companion.BASE_PATH
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal.valueOf

class ExchangeControllerIntegTest : AbstractIntegrationTest() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        initExchangeMetadataEntity()
        initExchangeMetadataUploadEntity()
    }

    @Test
    fun `should fetch symbols for BlockchainDotCom`() {
        // GIVEN
        val exchangeType = BLOCKCHAIN_DOT_COM
        val symbol = "BTC-USD"
        val baseCurrency = "BTC"
        val counterCurrency = "USD"
        val symbolEth = "ETH-USD"
        val baseCurrencyEth = "ETH"
        val expectedSymbols: Map<String, BlockchainDotComSymbol> = makeBlockchainDotComSymbol(
            symbol = symbol,
            baseCurrency = baseCurrency,
            counterCurrency = counterCurrency
        ) + makeBlockchainDotComSymbol(
            symbol = symbolEth,
            baseCurrency = baseCurrencyEth,
            counterCurrency = counterCurrency
        )

        stubFor(
            get(urlEqualTo("/symbols"))
                .willReturn(okJson(objectMapper.writeValueAsString(expectedSymbols)))
        )

        // WHEN
        mockMvc.perform(
            get(symbolsEndpoint(exchangeType))
                .contentType(APPLICATION_JSON)
        )
            // THEN
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[0].symbol").value(symbol))
            .andExpect(jsonPath("$.[0].baseCurrency").value(baseCurrency))
            .andExpect(jsonPath("$.[0].counterCurrency").value(counterCurrency))
            .andExpect(jsonPath("$.[1].symbol").value(symbolEth))
            .andExpect(jsonPath("$.[1].baseCurrency").value(baseCurrencyEth))
            .andExpect(jsonPath("$.[1].counterCurrency").value(counterCurrency))
    }

    @Test
    fun `should fetch order book for BlockchainDotCom`() {
        // GIVEN
        val exchangeType = BLOCKCHAIN_DOT_COM
        val symbol = "BTC-USD"
        val baseCurrency = "BTC"
        val counterCurrency = "USD"
        val symbolEth = "ETH-USD"
        val baseCurrencyEth = "ETH"
        val price = 10L
        val quantity = 20L
        val priceEth = 30L
        val quantityEth = 40L
        val expectedSymbols: Map<String, BlockchainDotComSymbol> = makeBlockchainDotComSymbol(
            symbol = symbol,
            baseCurrency = baseCurrency,
            counterCurrency = counterCurrency
        ) + makeBlockchainDotComSymbol(
            symbol = symbolEth,
            baseCurrency = baseCurrencyEth,
            counterCurrency = counterCurrency
        )

        val expectedBlockchainDotComOrderBook: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = symbol,
            price = valueOf(price),
            quantity = valueOf(quantity)
        )

        val expectedBlockchainDotComOrderBookEth: BlockchainDotComOrderBook = makeBlockchainDotComOrderBook(
            symbol = symbolEth,
            price = valueOf(priceEth),
            quantity = valueOf(quantityEth)
        )

        stubFor(
            get(urlEqualTo("/symbols"))
                .willReturn(okJson(objectMapper.writeValueAsString(expectedSymbols)))
        )

        stubFor(
            get(urlEqualTo("/l3/BTC-USD"))
                .willReturn(okJson(objectMapper.writeValueAsString(expectedBlockchainDotComOrderBook)))
        )

        stubFor(
            get(urlEqualTo("/l3/ETH-USD"))
                .willReturn(okJson(objectMapper.writeValueAsString(expectedBlockchainDotComOrderBookEth)))
        )

        // WHEN
        mockMvc.perform(
            get(orderBookEndpoint(exchangeType))
                .contentType(APPLICATION_JSON)
        )
            // THEN
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[0].symbol").value(symbol))
            .andExpect(jsonPath("$.[0].price").value(price + price))
            .andExpect(jsonPath("$.[0].quantity").value(quantity + quantity))
            .andExpect(jsonPath("$.[0].orderType").value("ASK"))
            .andExpect(jsonPath("$.[1].symbol").value(symbol))
            .andExpect(jsonPath("$.[1].price").value(price))
            .andExpect(jsonPath("$.[1].quantity").value(quantity))
            .andExpect(jsonPath("$.[1].orderType").value("BID"))
            .andExpect(jsonPath("$.[2].symbol").value(symbolEth))
            .andExpect(jsonPath("$.[2].price").value(priceEth + priceEth))
            .andExpect(jsonPath("$.[2].quantity").value(quantityEth + quantityEth))
            .andExpect(jsonPath("$.[2].orderType").value("ASK"))
            .andExpect(jsonPath("$.[3].symbol").value(symbolEth))
            .andExpect(jsonPath("$.[3].price").value(priceEth))
            .andExpect(jsonPath("$.[3].quantity").value(quantityEth))
            .andExpect(jsonPath("$.[3].orderType").value("BID"))
    }

    @Test
    fun `should fetch metadata for BlockchainDotCom`() {
        // GIVEN
        val exchangeType = BLOCKCHAIN_DOT_COM

        // WHEN
        mockMvc.perform(
            get(metadataEndpoint(exchangeType))
                .contentType(APPLICATION_JSON)
        )
            // THEN
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.[0].key").value("Country"))
            .andExpect(jsonPath("$.content.[0].value").value("UK"))
    }

    @Test
    fun `should fetch metadata upload for BlockchainDotCom`() {
        // GIVEN
        val exchangeType = BLOCKCHAIN_DOT_COM

        // WHEN
        mockMvc.perform(
            get(metadataUploadEndpoint(exchangeType))
                .contentType(APPLICATION_JSON)
        )
            // THEN
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.[0].status").value(PENDING.toString()))
    }

    @Test
    fun `should upload metadata for BlockchainDotCom`() {
        // GIVEN
        val exchangeType = BLOCKCHAIN_DOT_COM
        val fileName = "test.csv"
        val byte: ByteArray = getTestMetadataUploadFile(fileName)
        val file = MockMultipartFile("file", fileName, "text/csv", byte)

        // WHEN
        mockMvc.perform(
            MockMvcRequestBuilders.multipart(metadataEndpoint(exchangeType))
                .file(file)
                .contentType(MULTIPART_FORM_DATA)
        )
            // THEN
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(PENDING.toString()))
    }

    private fun symbolsEndpoint(exchangeType: ExchangeType) = "$BASE_PATH/$exchangeType/symbols"
    private fun orderBookEndpoint(exchangeType: ExchangeType) = "$BASE_PATH/$exchangeType/order-books"
    private fun metadataEndpoint(exchangeType: ExchangeType) = "$BASE_PATH/$exchangeType/metadata"
    private fun metadataUploadEndpoint(exchangeType: ExchangeType) = "$BASE_PATH/$exchangeType/metadata-uploads"
}
