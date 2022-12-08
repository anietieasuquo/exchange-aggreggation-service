package com.tradesoft.exchangeaggregationservice.periphery.events.publisher

import com.tradesoft.exchangeaggregationservice.periphery.events.KafkaEvent

interface KafkaEventPublisher<T : KafkaEvent> {
    fun publish(event: T): Boolean
}
