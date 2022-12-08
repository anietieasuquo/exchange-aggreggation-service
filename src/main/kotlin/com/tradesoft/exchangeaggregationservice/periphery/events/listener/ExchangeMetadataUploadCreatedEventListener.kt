package com.tradesoft.exchangeaggregationservice.periphery.events.listener

import com.tradesoft.exchangeaggregationservice.config.async.AsyncProvider
import com.tradesoft.exchangeaggregationservice.periphery.events.ExchangeMetadataUploadCreatedEvent
import com.tradesoft.exchangeaggregationservice.periphery.events.handler.ExchangeMetadataUploadCreatedEventHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ExchangeMetadataUploadCreatedEventListener(
    private val exchangeMetadataUploadCreatedEventHandler: ExchangeMetadataUploadCreatedEventHandler,
    private val asyncProvider: AsyncProvider
) : KafkaEventListener<ExchangeMetadataUploadCreatedEvent> {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${kafka.topics.exchange-metadata}"], containerFactory = "kafkaListenerContainerFactory")
    override fun listen(event: ExchangeMetadataUploadCreatedEvent, ack: Acknowledgment): Unit =
        runBlocking(asyncProvider.provideDefaultCoroutineDispatcher()) {
            log.info("Received ExchangeMetadataUploadCreatedEvent event: $event")
            launch {
                exchangeMetadataUploadCreatedEventHandler.handle(event)
                ack.acknowledge()
            }
        }
}
