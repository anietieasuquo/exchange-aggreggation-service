package com.tradesoft.exchangeaggregationservice.periphery.events

import java.io.Serializable
import java.time.LocalDateTime

open class KafkaEvent : Serializable {
    var dateCreated: LocalDateTime = LocalDateTime.now()
}
