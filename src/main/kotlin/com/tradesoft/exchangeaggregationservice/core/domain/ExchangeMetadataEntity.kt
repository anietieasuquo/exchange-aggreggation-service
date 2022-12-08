package com.tradesoft.exchangeaggregationservice.core.domain

import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import jakarta.persistence.*
import jakarta.persistence.GenerationType.SEQUENCE
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "exchange_metadata")
data class ExchangeMetadataEntity(

    @GenericGenerator(
        name = "metadataGenerator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = [
            Parameter(name = "sequence_name", value = "exchange_metadata_id_seq"),
            Parameter(name = "initial_value", value = "100"),
            Parameter(name = "increment_size", value = "1")
        ]
    )
    @Id
    @GeneratedValue(generator = "metadataGenerator", strategy = SEQUENCE)
    val id: Long = 0,

    @Version
    val version: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type", nullable = false)
    val exchangeType: ExchangeType,

    @Column(name = "data_key", nullable = false)
    val dataKey: String,

    @Column(name = "data_value", nullable = false)
    var dataValue: String,

    @Column(name = "upload_id", nullable = false)
    var uploadId: Long,

    @Column(name = "date_created", nullable = false, updatable = false)
    @field: CreationTimestamp
    val dateCreated: LocalDateTime = LocalDateTime.now(),

    @Column(name = "date_updated", nullable = false)
    @field: UpdateTimestamp
    var dateUpdated: LocalDateTime = LocalDateTime.now()
)
