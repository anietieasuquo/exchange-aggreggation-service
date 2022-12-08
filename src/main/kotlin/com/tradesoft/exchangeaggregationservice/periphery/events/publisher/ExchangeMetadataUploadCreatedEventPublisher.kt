package com.tradesoft.exchangeaggregationservice.periphery.events.publisher

import com.tradesoft.exchangeaggregationservice.config.events.KafkaTopic
import com.tradesoft.exchangeaggregationservice.periphery.events.ExchangeMetadataUploadCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ExchangeMetadataUploadCreatedEventPublisher(
    private val exchangeMetaDataUploadCreatedEventTemplate: KafkaTemplate<String, ExchangeMetadataUploadCreatedEvent>,
    private val kafkaTopic: KafkaTopic
) : KafkaEventPublisher<ExchangeMetadataUploadCreatedEvent> {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(event: ExchangeMetadataUploadCreatedEvent): Boolean = runCatching {
        kafkaTopic.exchangeMetadata.takeUnless { it.isNullOrEmpty() }?.let {
            log.info("Publishing ExchangeMetadataUploadCreatedEvent: $event")
            exchangeMetaDataUploadCreatedEventTemplate.send(it, event)
            true
        } ?: false.also { log.error("Failed to publish for ExchangeMetadataUploadCreatedEvent: $event, invalid topic") }
    }.onFailure {
        log.error("Failed to publish for ExchangeMetadataUploadCreatedEvent: $event", it)
    }.getOrThrow()
}
