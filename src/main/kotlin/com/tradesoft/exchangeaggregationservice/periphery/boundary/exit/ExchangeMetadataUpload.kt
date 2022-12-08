package com.tradesoft.exchangeaggregationservice.periphery.boundary.exit

import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus
import java.io.Serializable
import java.time.LocalDateTime

data class ExchangeMetadataUpload(
    val id: Long,
    val status: MetadataUploadStatus,
    val dateCreated: LocalDateTime,
    val dateCompleted: LocalDateTime?
) : Serializable
