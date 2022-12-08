package com.tradesoft.exchangeaggregationservice.core.service

import com.nhaarman.mockitokotlin2.*
import com.tradesoft.exchangeaggregationservice.config.CacheProperties
import com.tradesoft.exchangeaggregationservice.config.async.AsyncProvider
import com.tradesoft.exchangeaggregationservice.config.async.ThreadPoolProperties
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType.BLOCKCHAIN_DOT_COM
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataEntity
import com.tradesoft.exchangeaggregationservice.core.repository.ExchangeMetadataEntityRepository
import com.tradesoft.exchangeaggregationservice.core.service.helper.CacheHelper
import com.tradesoft.exchangeaggregationservice.core.util.SynchronizedUpdater
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ExchangeAsyncUpdateHandlerServiceTest {

    private val exchangeMetadataEntityRepository: ExchangeMetadataEntityRepository = mock()

    private val cacheHelper: CacheHelper = mock()

    private val cacheProperties: CacheProperties = CacheProperties().apply {
        exchangeMetadataCacheName = "cache1"
        exchangeMetadataUploadCacheName = "cache2"
    }

    private val threadPoolProperties: ThreadPoolProperties = ThreadPoolProperties()

    private val asyncProvider: AsyncProvider = AsyncProvider(threadPoolProperties)

    private val exchangeAsyncUpdateHandlerService: ExchangeAsyncUpdateHandlerService =
        ExchangeAsyncUpdateHandlerService(
            exchangeMetadataEntityRepository = exchangeMetadataEntityRepository,
            cacheHelper = cacheHelper,
            cacheProperties = cacheProperties,
            asyncProvider = asyncProvider
        )

    @Test
    fun `should perform update`() {
        // GIVEN
        val id = 10L
        val dateCreated = LocalDateTime.now()
        val dateUpdated = LocalDateTime.now()
        val exchangeType: ExchangeType = BLOCKCHAIN_DOT_COM
        val dataKey = "dataKey"
        val dataValue = "dataValue"
        val entity = ExchangeMetadataEntity(
            id = id,
            version = 2,
            exchangeType = exchangeType,
            dataKey = dataKey,
            dataValue = dataValue,
            uploadId = 10L,
            dateCreated = dateCreated,
            dateUpdated = dateUpdated
        )
        val entities: List<ExchangeMetadataEntity> = listOf(entity)
        val synchronizedUpdater = SynchronizedUpdater()
        whenever(
            exchangeMetadataEntityRepository.findFirstByExchangeTypeAndDataKey(
                exchangeType = exchangeType,
                dataKey = dataKey
            )
        ).thenReturn(Optional.of(entity))

        whenever(exchangeMetadataEntityRepository.save(any<ExchangeMetadataEntity>())).thenReturn(entity)
        exchangeMetadataEntityRepository.stub {
            onBlocking { save(any<ExchangeMetadataEntity>()) }.doReturn(entity)
        }
        cacheHelper.stub {
            onBlocking {
                evictCacheForExchange(
                    cacheName = cacheProperties.exchangeMetadataCacheName,
                    exchangeType = exchangeType
                )
            }.doAnswer {}
        }

        // WHEN
        exchangeAsyncUpdateHandlerService.handleMetadataUpdate(
            entities = entities,
            exchangeType = exchangeType,
            synchronizedUpdater = synchronizedUpdater
        )

        // THEN
        verify(exchangeMetadataEntityRepository).findFirstByExchangeTypeAndDataKey(
            exchangeType = exchangeType,
            dataKey = dataKey
        )
        verify(exchangeMetadataEntityRepository).save(any())
        verify(cacheHelper, times(1)).evictCacheForExchange(any(), any())
    }
}
