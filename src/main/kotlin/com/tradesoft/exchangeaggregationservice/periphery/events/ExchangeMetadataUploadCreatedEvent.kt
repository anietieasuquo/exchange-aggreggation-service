package com.tradesoft.exchangeaggregationservice.periphery.events

import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType

data class ExchangeMetadataUploadCreatedEvent(
    val exchangeType: ExchangeType,
    val uploadId: Long
) : KafkaEvent()
