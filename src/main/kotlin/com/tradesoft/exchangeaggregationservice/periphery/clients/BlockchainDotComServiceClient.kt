package com.tradesoft.exchangeaggregationservice.periphery.clients

import com.tradesoft.exchangeaggregationservice.exception.BusinessException
import com.tradesoft.exchangeaggregationservice.exception.ErrorCategory.THIRD_PARTY_COMMUNICATION_ERROR
import com.tradesoft.exchangeaggregationservice.exception.ErrorResponse
import com.tradesoft.exchangeaggregationservice.periphery.api.BlockchainDotComServiceApi
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.entry.BlockchainDotComSymbol
import feign.Feign
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.micrometer.core.annotation.Timed
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class BlockchainDotComServiceClient(
    @Value("\${blockchaindotcom.service.url}") url: String,
    authInterceptorFeignBuilder: Feign.Builder
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val blockchainDotComServiceApi: BlockchainDotComServiceApi =
        authInterceptorFeignBuilder.target(
            BlockchainDotComServiceApi::class.java,
            url
        )

    @CircuitBreaker(name = "blockchaindotcomservice_symbols")
    @Timed(value = "exchange.blockchaindotcomservice.get-symbols")
    fun getSymbols(): Map<String, BlockchainDotComSymbol> =
        runCatching {
            blockchainDotComServiceApi.getSymbols()
        }.onFailure { throwable ->
            val errorMessage = "Failed to fetch Blockchain.com symbols"
            throw BusinessException(
                ErrorResponse(
                    errorType = throwable.cause?.javaClass?.canonicalName ?: "FETCH_SYMBOLS_ERROR",
                    errorCategory = THIRD_PARTY_COMMUNICATION_ERROR,
                    technicalErrorMessage = throwable.message ?: errorMessage,
                    humanReadableMessage = errorMessage
                )
            ).also { log.error("Blockchain.com symbols response: $it") }
        }.getOrThrow().also { log.debug("Blockchain.com symbols response: $it") }

    @CircuitBreaker(name = "blockchaindotcomservice_order_book")
    @Timed(value = "exchange.blockchaindotcomservice.get-order-book")
    fun getOrderBook(symbol: String): BlockchainDotComOrderBook =
        runCatching {
            blockchainDotComServiceApi.getOrderBook(symbol)
        }.onFailure { throwable ->
            val errorMessage = "Failed to fetch order books for Blockchain.com symbol $symbol"
            throw BusinessException(
                ErrorResponse(
                    errorType = throwable.cause?.javaClass?.canonicalName ?: "FETCH_ORDER_BOOKS_ERROR",
                    errorCategory = THIRD_PARTY_COMMUNICATION_ERROR,
                    technicalErrorMessage = throwable.message ?: errorMessage,
                    humanReadableMessage = errorMessage
                )
            ).also { log.error("Blockchain.com order book response: $it") }
        }.getOrThrow().also { log.debug("Blockchain.com order book response: $it") }
}
