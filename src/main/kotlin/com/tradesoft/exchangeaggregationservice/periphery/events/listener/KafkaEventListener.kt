package com.tradesoft.exchangeaggregationservice.periphery.events.listener

import com.tradesoft.exchangeaggregationservice.periphery.events.KafkaEvent
import org.springframework.kafka.support.Acknowledgment

interface KafkaEventListener<T : KafkaEvent> {
    fun listen(event: T, ack: Acknowledgment)
}
