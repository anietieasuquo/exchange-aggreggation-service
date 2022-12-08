package com.tradesoft.exchangeaggregationservice.core.service

import com.tradesoft.exchangeaggregationservice.config.CacheProperties
import com.tradesoft.exchangeaggregationservice.config.async.AsyncProvider
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataEntity
import com.tradesoft.exchangeaggregationservice.core.repository.ExchangeMetadataEntityRepository
import com.tradesoft.exchangeaggregationservice.core.service.helper.CacheHelper
import com.tradesoft.exchangeaggregationservice.core.util.SynchronizedUpdater
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ExchangeAsyncUpdateHandlerService(
    private val asyncProvider: AsyncProvider,
    private val exchangeMetadataEntityRepository: ExchangeMetadataEntityRepository,
    private val cacheHelper: CacheHelper,
    private val cacheProperties: CacheProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handleMetadataUpdate(
        entities: List<ExchangeMetadataEntity>,
        synchronizedUpdater: SynchronizedUpdater,
        exchangeType: ExchangeType
    ) = runBlocking(asyncProvider.provideDefaultCoroutineDispatcher()) {
        entities.forEach { entity ->
            run {
                exchangeMetadataEntityRepository.findFirstByExchangeTypeAndDataKey(
                    exchangeType = exchangeType,
                    dataKey = entity.dataKey
                ).takeIf { it.isPresent }?.let {
                    launch {
                        synchronizedUpdater.updateSynchronized {
                            val metadataEntity = it.get()
                            log.debug("Found an upload for exchange: $exchangeType, entity: $metadataEntity proceeding to update")
                            metadataEntity.dataValue = entity.dataValue
                            metadataEntity.dateUpdated = LocalDateTime.now()
                            exchangeMetadataEntityRepository.save(metadataEntity)
                            log.debug("Updated an upload for exchange: $exchangeType, entity: $metadataEntity.")
                            log.debug("Beginning cache eviction process for exchange: $exchangeType")
                            cacheHelper.evictCacheForExchange(
                                cacheName = cacheProperties.exchangeMetadataCacheName,
                                exchangeType = exchangeType
                            )
                        }
                    }
                } ?: also {
                    log.debug("Did not find an upload for exchange: $exchangeType, and dataKey: ${entity.dataKey} proceeding to create new")
                    val create = exchangeMetadataEntityRepository.save(entity)
                    if (create.id > 0) {
                        log.debug("New metadata created for exchange: $exchangeType, data: $create")
                    } else {
                        log.error("Could not create metadata for exchange: $exchangeType, and dataKey: ${entity.dataKey}")
                    }
                }
            }
        }
    }
}
