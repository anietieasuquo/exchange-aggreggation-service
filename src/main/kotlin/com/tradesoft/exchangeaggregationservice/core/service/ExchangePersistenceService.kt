package com.tradesoft.exchangeaggregationservice.core.service

import com.tradesoft.exchangeaggregationservice.config.async.AsyncProvider
import com.tradesoft.exchangeaggregationservice.core.business.TradeSoftPage
import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.business.enums.MetadataUploadStatus
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataUploadEntity
import com.tradesoft.exchangeaggregationservice.core.mapper.BlockchainDotComMapper.toExchangeMetadata
import com.tradesoft.exchangeaggregationservice.core.mapper.BlockchainDotComMapper.toExchangeMetadataUpload
import com.tradesoft.exchangeaggregationservice.core.repository.ExchangeMetadataEntityRepository
import com.tradesoft.exchangeaggregationservice.core.repository.ExchangeMetadataUploadEntityRepository
import com.tradesoft.exchangeaggregationservice.core.repository.GenericMetadataEntityRepository
import com.tradesoft.exchangeaggregationservice.exception.BusinessException
import com.tradesoft.exchangeaggregationservice.exception.ErrorCategory
import com.tradesoft.exchangeaggregationservice.exception.ErrorCategory.USER_INPUT_ERROR
import com.tradesoft.exchangeaggregationservice.exception.ErrorResponse
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeMetadata
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExchangeMetadataUpload
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.MetadataUpdateResponse
import com.tradesoft.exchangeaggregationservice.periphery.events.ExchangeMetadataUploadCreatedEvent
import com.tradesoft.exchangeaggregationservice.periphery.events.publisher.ExchangeMetadataUploadCreatedEventPublisher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ExchangePersistenceService(
    private val exchangeMetadataEntityRepository: ExchangeMetadataEntityRepository,
    private val exchangeMetadataUploadEntityRepository: ExchangeMetadataUploadEntityRepository,
    @Qualifier("genericMetadataEntityRepositoryImpl") private val genericMetadataEntityRepository: GenericMetadataEntityRepository,
    private val exchangeMetadataUploadCreatedEventPublisher: ExchangeMetadataUploadCreatedEventPublisher,
    private val asyncProvider: AsyncProvider
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val CSV_MIME_TYPE = "text/csv"
    }

    @Cacheable(
        value = ["exchange-metadata-cache"],
        key = "#exchange.toString() + '_' + #pageNumber + '_' + #pageSize"
    )
    fun getMetadata(exchange: ExchangeType, pageNumber: Int, pageSize: Int): TradeSoftPage<ExchangeMetadata> =
        exchangeMetadataEntityRepository.findAllByExchangeType(
            exchangeType = exchange,
            pageable = PageRequest.of(
                pageNumber,
                pageSize
            )
        )
            .also { log.debug("Found ${it.totalElements} records for ExchangeMetadata") }
            .let { TradeSoftPage(it.toExchangeMetadata()) }

    @Cacheable(
        value = ["exchange-metadata-upload-cache"],
        key = "#exchange.toString() + '_' + #pageNumber + '_' + #pageSize"
    )
    fun getMetadataUploads(
        exchange: ExchangeType,
        pageNumber: Int,
        pageSize: Int
    ): TradeSoftPage<ExchangeMetadataUpload> =
        exchangeMetadataUploadEntityRepository.findAllByExchangeType(
            exchangeType = exchange,
            pageable = PageRequest.of(
                pageNumber,
                pageSize
            )
        )
            .also { log.debug("Found ${it.size} records for ExchangeMetadataUpload") }
            .let { TradeSoftPage(it.toExchangeMetadataUpload()) }

    @Transactional
    fun saveMetadata(exchangeType: ExchangeType, csvFile: MultipartFile): MetadataUpdateResponse =
        runBlocking(asyncProvider.provideDefaultCoroutineDispatcher()) {
            csvFile.takeUnless { it.contentType.isNullOrEmpty() || !it.contentType.contentEquals(CSV_MIME_TYPE) }?.let {
                genericMetadataEntityRepository.fileToBlob(file = csvFile)
            }?.let { blob ->
                log.info("File has been processed: ${csvFile.originalFilename}")
                val uploadEntity = ExchangeMetadataUploadEntity(
                    exchangeType = exchangeType,
                    status = MetadataUploadStatus.PENDING,
                    file = blob
                )
                log.info("Now trying to save ExchangeMetadataUploadEntity: $uploadEntity")

                val savedUpload = exchangeMetadataUploadEntityRepository.save(uploadEntity)
                if (savedUpload.id == 0L) {
                    log.error("Failed to save entity: $uploadEntity")
                    failToCreateMetadata(exchangeType)
                }

                log.info("ExchangeMetadataUploadEntity has been saved: $savedUpload now proceeding to publish event")
                launch {
                    publishMetadataUploadCreatedEvent(
                        uploadId = savedUpload.id,
                        exchangeType = exchangeType
                    )
                }

                MetadataUpdateResponse(
                    uploadId = savedUpload.id,
                    status = savedUpload.status,
                    dateCreated = savedUpload.dateCreated
                ).also { log.info("Created metadata upload job for exchange: $exchangeType, response: $it") }
            } ?: throw BusinessException(
                ErrorResponse(
                    errorType = "UNSUPPORTED_FILE",
                    errorCategory = USER_INPUT_ERROR,
                    technicalErrorMessage = "Unsupported file mime-type: ${csvFile.contentType} for exchange: $exchangeType",
                    humanReadableMessage = "Unsupported file type. Only CSV files are allowed."
                )
            )
        }

    private fun failToCreateMetadata(exchangeType: ExchangeType) {
        throw BusinessException(
            ErrorResponse(
                errorType = "SAVE_EXCHANGE_METADATA_ERROR",
                errorCategory = ErrorCategory.INTERNAL_PROCESS_ERROR,
                technicalErrorMessage = "Failed to save metadata for exchange: $exchangeType",
                humanReadableMessage = "Failed to save metadata for exchange: $exchangeType"
            )
        )
    }

    private fun publishMetadataUploadCreatedEvent(uploadId: Long, exchangeType: ExchangeType) =
        runBlocking(asyncProvider.provideDefaultCoroutineDispatcher()) {
            try {
                launch {
                    exchangeMetadataUploadCreatedEventPublisher.publish(
                        event = ExchangeMetadataUploadCreatedEvent(
                            exchangeType = exchangeType,
                            uploadId = uploadId
                        )
                    )
                }
            } catch (ex: Exception) {
                log.error("An error occurred while publishing event for uploadId: $uploadId", ex)
            }
        }
}
