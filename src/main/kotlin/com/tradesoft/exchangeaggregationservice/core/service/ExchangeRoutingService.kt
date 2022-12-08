package com.tradesoft.exchangeaggregationservice.core.service

import com.tradesoft.exchangeaggregationservice.core.business.OrderBookFilter
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.exception.BusinessException
import com.tradesoft.exchangeaggregationservice.exception.ErrorCategory.INTERNAL_PROCESS_ERROR
import com.tradesoft.exchangeaggregationservice.exception.ErrorResponse
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeOrderBook
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeSymbol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ExchangeRoutingService(private val exchangeServices: List<ExchangeService>) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getSymbols(exchange: ExchangeType): List<ExchangeSymbol> =
        resolveHandlerAndCheck<ExchangeService>(exchange)
            .also { log.info("Fetching symbols for exchange: $exchange") }
            .getSymbols()
            .also { log.info("Fetched symbols with result $it") }

    fun getOrderBook(exchange: ExchangeType, filter: OrderBookFilter): List<ExchangeOrderBook> =
        resolveHandlerAndCheck<ExchangeService>(exchange)
            .also { log.info("Fetching order book for exchange: $exchange") }
            .getOrderBook(filter)
            .also { log.info("Fetched order book with result $it") }


    private fun resolveHandler(exchangeType: ExchangeType): ExchangeService =
        exchangeServices.find { exchangeType == it.getExchangeType() }
            ?: throw BusinessException(
                ErrorResponse(
                    errorType = "UNSUPPORTED_EXCHANGE",
                    errorCategory = INTERNAL_PROCESS_ERROR,
                    technicalErrorMessage = "Unsupported exchange: $exchangeType",
                    humanReadableMessage = "Unsupported exchange: $exchangeType"
                )
            )

    private inline fun <reified T> resolveHandlerAndCheck(exchangeType: ExchangeType): T =
        resolveHandler(exchangeType).let {
            if (it is T) {
                it
            } else {
                log.error("Unsupported operation by exchange: $exchangeType")
                throw BusinessException(
                    ErrorResponse(
                        errorType = "UNSUPPORTED_EXCHANGE_OPERATION",
                        errorCategory = INTERNAL_PROCESS_ERROR,
                        technicalErrorMessage = "Unsupported operation by the exchange: $exchangeType",
                        humanReadableMessage = "Unsupported operation by the exchange: $exchangeType"
                    )
                )
            }
        }
}
