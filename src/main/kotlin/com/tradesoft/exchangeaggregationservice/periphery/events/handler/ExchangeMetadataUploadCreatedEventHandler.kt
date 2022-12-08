package com.tradesoft.exchangeaggregationservice.periphery.events.handler

import com.tradesoft.exchangeaggregationservice.core.service.ExchangeAsyncUpdateService
import com.tradesoft.exchangeaggregationservice.periphery.events.ExchangeMetadataUploadCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ExchangeMetadataUploadCreatedEventHandler(
    private val exchangeAsyncUpdateService: ExchangeAsyncUpdateService
) : KafkaEventHandler<ExchangeMetadataUploadCreatedEvent> {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(event: ExchangeMetadataUploadCreatedEvent) {
        log.info("Handling ExchangeMetadataUploadCreatedEvent event: $event")
        exchangeAsyncUpdateService.updateMetadata(
            event.uploadId
        )
    }
}
