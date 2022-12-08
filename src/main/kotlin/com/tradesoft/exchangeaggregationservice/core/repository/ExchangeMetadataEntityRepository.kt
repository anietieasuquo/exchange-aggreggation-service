package com.tradesoft.exchangeaggregationservice.core.repository

import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExchangeMetadataEntityRepository : JpaRepository<ExchangeMetadataEntity, Long> {

    fun findAllByExchangeType(exchangeType: ExchangeType, pageable: Pageable): Page<ExchangeMetadataEntity>

    fun findFirstByExchangeTypeAndDataKey(exchangeType: ExchangeType, dataKey: String): Optional<ExchangeMetadataEntity>
}
