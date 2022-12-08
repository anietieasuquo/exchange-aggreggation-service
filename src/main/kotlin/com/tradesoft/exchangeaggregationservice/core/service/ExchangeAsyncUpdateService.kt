package com.tradesoft.exchangeaggregationservice.core.service

import com.tradesoft.exchangeaggregationservice.config.CacheProperties
import com.tradesoft.exchangeaggregationservice.config.async.AsyncProvider
import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus.*
import com.tradesoft.exchangeaggregationservice.core.repository.ExchangeMetadataUploadEntityRepository
import com.tradesoft.exchangeaggregationservice.core.service.helper.CacheHelper
import com.tradesoft.exchangeaggregationservice.core.util.CsvParser
import com.tradesoft.exchangeaggregationservice.core.util.SynchronizedUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExchangeAsyncUpdateService(
    private val exchangeAsyncUpdateHandlerService: ExchangeAsyncUpdateHandlerService,
    private val asyncProvider: AsyncProvider,
    private val exchangeMetadataUploadEntityRepository: ExchangeMetadataUploadEntityRepository,
    private val cacheHelper: CacheHelper,
    private val cacheProperties: CacheProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun updateMetadata(uploadId: Long) = runBlocking(asyncProvider.provideDefaultCoroutineDispatcher()) {
        val synchronizedUpdater = SynchronizedUpdater()
        withContext(Dispatchers.IO) {
            exchangeMetadataUploadEntityRepository.findFirstByIdAndStatus(uploadId, PENDING).also {
                log.debug("ExchangeMetadataUploadEntity fetched for uploadId: $uploadId, entity: $it")
            }.takeIf { it.isPresent && it.get().file != null }
                ?.let { optionalMedataEntity ->
                    val upload = optionalMedataEntity.get()
                    val exchangeType = upload.exchangeType
                    log.info("Starting async process to update exchange metadata for exchangeType: $exchangeType, uploadId: $uploadId")

                    val entities = CsvParser.readCsvToExchangeMetadataEntity(
                        uploadId = uploadId,
                        exchangeType = exchangeType,
                        inputStream = upload.file!!.binaryStream
                    )
                    log.debug("Parsed CSV to metadata entities for exchangeType: $exchangeType, entities: $entities")

                    try {
                        exchangeAsyncUpdateHandlerService.handleMetadataUpdate(
                            exchangeType = exchangeType,
                            entities = entities,
                            synchronizedUpdater = synchronizedUpdater
                        )

                        launch {
                            synchronizedUpdater.updateSynchronized {
                                upload.status = COMPLETED
                                upload.dateCompleted = LocalDateTime.now()
                                upload.file = null
                                exchangeMetadataUploadEntityRepository.save(upload)
                                cacheHelper.evictCacheForExchange(
                                    cacheName = cacheProperties.exchangeMetadataUploadCacheName,
                                    exchangeType = exchangeType
                                )
                            }
                        }
                    } catch (ex: Exception) {
                        log.error("Failed to process exchange metadata upload for exchange: $exchangeType", ex)
                        launch {
                            synchronizedUpdater.updateSynchronized {
                                upload.status = FAILED
                                upload.file = null
                                exchangeMetadataUploadEntityRepository.save(upload)
                                cacheHelper.evictCacheForExchange(
                                    cacheName = cacheProperties.exchangeMetadataUploadCacheName,
                                    exchangeType = exchangeType
                                )
                            }
                        }
                    }
                } ?: also {
                log.debug("Exchange metadata upload job ($uploadId) not eligible for processing.")
            }
        }
    }
}
