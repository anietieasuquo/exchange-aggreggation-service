package com.tradesoft.exchangeaggregationservice.periphery.events.handler

import com.tradesoft.exchangeaggregationservice.periphery.events.KafkaEvent

interface KafkaEventHandler<T : KafkaEvent> {
    fun handle(event: T)
}
