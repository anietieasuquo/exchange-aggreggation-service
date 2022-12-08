package com.tradesoft.exchangeaggregationservice.periphery.boundary.exit

import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus
import java.time.LocalDateTime

data class MetadataUpdateResponse(
    val uploadId: Long,
    val status: MetadataUploadStatus,
    val dateCreated: LocalDateTime
)
