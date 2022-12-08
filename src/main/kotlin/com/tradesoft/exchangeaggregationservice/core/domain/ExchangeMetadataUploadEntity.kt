package com.tradesoft.exchangeaggregationservice.core.domain

import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus
import jakarta.persistence.*
import jakarta.persistence.GenerationType.SEQUENCE
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import java.sql.Blob
import java.time.LocalDateTime

@Entity
@Table(name = "exchange_metadata_upload")
data class ExchangeMetadataUploadEntity(
    @GenericGenerator(
        name = "metadataUploadGenerator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = [
            Parameter(name = "sequence_name", value = "exchange_metadata_upload_id_seq"),
            Parameter(name = "initial_value", value = "100"),
            Parameter(name = "increment_size", value = "1")
        ]
    )
    @Id
    @GeneratedValue(generator = "metadataUploadGenerator", strategy = SEQUENCE)
    val id: Long = 0,

    @Version
    val version: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type", nullable = false)
    val exchangeType: ExchangeType,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MetadataUploadStatus,

    @Lob
    @Column(name = "file", nullable = true)
    var file: Blob?,

    @Column(name = "date_created", nullable = false, updatable = false)
    @field: CreationTimestamp
    val dateCreated: LocalDateTime = LocalDateTime.now(),

    @Column(name = "date_completed", nullable = true)
    var dateCompleted: LocalDateTime? = null
)
