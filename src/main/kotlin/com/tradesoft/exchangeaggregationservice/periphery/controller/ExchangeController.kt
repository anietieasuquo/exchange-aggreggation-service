package com.tradesoft.exchangeaggregationservice.periphery.controller

import com.tradesoft.exchangeaggregationservice.core.business.OrderBookFilter
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.business.enums.OrderType
import com.tradesoft.exchangeaggregationservice.core.business.enums.SortOrder
import com.tradesoft.exchangeaggregationservice.core.service.ExchangePersistenceService
import com.tradesoft.exchangeaggregationservice.core.service.ExchangeRoutingService
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.*
import com.tradesoft.exchangeaggregationservice.periphery.controller.ExchangeController.Companion.BASE_PATH
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(
    value = [BASE_PATH],
    produces = [APPLICATION_JSON_VALUE]
)
@Tag(name = "Exchange Controller")
class ExchangeController(
    private val exchangeRoutingService: ExchangeRoutingService,
    private val exchangePersistenceService: ExchangePersistenceService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val BASE_PATH = "/api/v1/exchanges"
    }

    @Operation(
        summary = "Exchange symbols",
        description = "Get symbols for an exchange",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Symbols",
            useReturnTypeSchema = true
        )]
    )
    @GetMapping("/{exchangeName}/symbols")
    @ResponseBody
    fun getSymbols(
        @PathVariable(
            "exchangeName",
            required = true
        ) exchangeName: ExchangeType
    ): ResponseEntity<List<ExchangeSymbol>> =
        also { log.info("Received request to fetch symbols for exchange: {}", it) }
            .let { exchangeRoutingService.getSymbols(exchangeName) }
            .also { log.debug("Symbols response for exchange [{}]: {}", exchangeName, it) }
            .let { ResponseEntity.ok(it) }

    @Operation(
        summary = "Exchange order book",
        description = "Get order books for an exchange",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Order book",
            useReturnTypeSchema = true
        )]
    )
    @GetMapping("/{exchangeName}/order-books")
    @ResponseBody
    fun getOrderBook(
        @PathVariable(
            "exchangeName",
            required = true
        ) exchangeName: ExchangeType,
        @RequestParam(name = "symbol", required = false) symbol: String?,
        @RequestParam(name = "orderType", required = false) orderType: OrderType?,
        @RequestParam(name = "sortOrder", required = false) sortOrder: SortOrder?,
    ): ResponseEntity<List<ExchangeOrderBook>> =
        also {
            log.info(
                "Received request to fetch order book for exchange: {}, orderType: {}, sortOrder: {}",
                exchangeName, orderType, sortOrder
            )
        }.let {
            exchangeRoutingService.getOrderBook(
                exchange = exchangeName,
                filter = OrderBookFilter(
                    symbol = symbol,
                    orderType = orderType,
                    sortOrder = sortOrder
                )
            )
        }.also {
            log.debug(
                "Order book response for exchange [{}], symbol [{}], orderType[{}], sortOrder [{}]: {}",
                exchangeName,
                symbol,
                orderType,
                sortOrder,
                it
            )
        }.let { ResponseEntity.ok(it) }

    @Operation(
        summary = "Exchange metadata",
        description = "Get metadata for an exchange",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Metadata",
            useReturnTypeSchema = true
        )]
    )
    @GetMapping("/{exchangeName}/metadata")
    @ResponseBody
    fun getMetadata(
        @PathVariable(
            "exchangeName",
            required = true
        ) exchangeName: ExchangeType,
        @RequestParam(name = "pageNumber", required = false) pageNumber: Int?,
        @RequestParam(name = "pageSize", required = false) pageSize: Int?,
    ): ResponseEntity<Page<ExchangeMetadata>> =
        also {
            log.info("Received request to fetch metadata for exchange: {}", exchangeName)
        }.let {
            exchangePersistenceService.getMetadata(
                exchange = exchangeName,
                pageNumber = pageNumber,
                pageSize = pageSize
            )
        }.also {
            log.debug(
                "Metadata response for exchange [{}], pageNumber [{}], pageSize[{}]: {}",
                exchangeName,
                pageNumber,
                pageSize,
                it
            )
        }.let { ResponseEntity.ok(it) }

    @Operation(
        summary = "Create Exchange Metadata",
        description = "Create metadata for an exchange",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Metadata",
            useReturnTypeSchema = true
        )]
    )
    @PostMapping(value = ["/{exchangeName}/metadata"], consumes = [MULTIPART_FORM_DATA_VALUE])
    @ResponseBody
    fun saveMetadata(
        @PathVariable(
            "exchangeName",
            required = true
        ) exchangeName: ExchangeType,
        @RequestParam(name = "file") file: MultipartFile
    ): ResponseEntity<MetadataUpdateResponse> =
        also { log.info("Received request to save metadata for exchange: {}", exchangeName) }.let {
            exchangePersistenceService.saveMetadata(
                exchangeType = exchangeName,
                csvFile = file
            )
        }.also {
            log.debug("Metadata creation response for exchange [{}]: {}", exchangeName, it)
        }.let { ResponseEntity.ok(it) }

    @Operation(
        summary = "Exchange metadata uploads",
        description = "Get metadata uploads for an exchange",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Metadata uploads",
            useReturnTypeSchema = true
        )]
    )
    @GetMapping("/{exchangeName}/metadata-uploads")
    @ResponseBody
    fun getMetadataUploads(
        @PathVariable(
            "exchangeName",
            required = true
        ) exchangeName: ExchangeType,
        @RequestParam(name = "pageNumber", required = false) pageNumber: Int?,
        @RequestParam(name = "pageSize", required = false) pageSize: Int?,
    ): ResponseEntity<Page<ExchangeMetadataUpload>> =
        also {
            log.info("Received request to fetch metadata uploads for exchange: {}", exchangeName)
        }.let {
            exchangePersistenceService.getMetadataUploads(
                exchange = exchangeName,
                pageNumber = pageNumber,
                pageSize = pageSize
            )
        }.also {
            log.debug(
                "Metadata uploads response for exchange [{}], pageNumber [{}], pageSize[{}]: {}",
                exchangeName,
                pageNumber,
                pageSize,
                it
            )
        }.let { ResponseEntity.ok(it) }
}
