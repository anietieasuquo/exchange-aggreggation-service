package com.tradesoft.exchangeaggregationservice.core.repository

import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataUploadEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ExchangeMetadataUploadEntityRepository : JpaRepository<ExchangeMetadataUploadEntity, Long>,
    GenericMetadataEntityRepository {
    fun findAllByExchangeType(
        exchangeType: ExchangeType,
        pageable: Pageable
    ): Page<ExchangeMetadataUploadEntity>

    fun findFirstByIdAndStatus(id: Long, status: MetadataUploadStatus): Optional<ExchangeMetadataUploadEntity>
}
