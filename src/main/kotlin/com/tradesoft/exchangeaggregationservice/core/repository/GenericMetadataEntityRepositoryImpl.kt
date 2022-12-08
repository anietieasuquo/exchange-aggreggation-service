package com.tradesoft.exchangeaggregationservice.core.repository

import com.tradesoft.exchangeaggregationservice.exception.BusinessException
import com.tradesoft.exchangeaggregationservice.exception.ErrorCategory
import com.tradesoft.exchangeaggregationservice.exception.ErrorCategory.USER_INPUT_ERROR
import com.tradesoft.exchangeaggregationservice.exception.ErrorResponse
import jakarta.annotation.Resource
import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.sql.Blob

@Repository
class GenericMetadataEntityRepositoryImpl(@Resource private val entityManager: EntityManager) :
    GenericMetadataEntityRepository {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val FILE_UPLOAD_ERROR = "FILE_UPLOAD_ERROR"
    }

    @Transactional(readOnly = true)
    override fun fileToBlob(file: MultipartFile): Blob {
        log.info("Starting processing for file: ${file.originalFilename}")
        checkFileUploadNamePrecondition(file)
        return try {
            createBlob(file)
        } catch (ex: IOException) {
            val errorMessage = "Failed to save file ${file.originalFilename}."
            throw BusinessException(
                ErrorResponse(
                    errorType = ex.javaClass.canonicalName ?: FILE_UPLOAD_ERROR,
                    errorCategory = ErrorCategory.INTERNAL_PROCESS_ERROR,
                    technicalErrorMessage = ex.message ?: errorMessage,
                    humanReadableMessage = errorMessage
                )
            )
        }.also { log.info("File processed successfully with result: $it") }
    }

    private fun checkFileUploadNamePrecondition(file: MultipartFile) {
        if (file.originalFilename.isNullOrEmpty()) {
            val errorMessage = "Invalid file name"
            throw BusinessException(
                ErrorResponse(
                    errorType = FILE_UPLOAD_ERROR,
                    errorCategory = USER_INPUT_ERROR,
                    technicalErrorMessage = errorMessage,
                    humanReadableMessage = errorMessage
                )
            )
        }
    }

    private fun createBlob(file: MultipartFile): Blob {
        val sessionFactory = entityManager.unwrap(Session::class.java).sessionFactory
        return sessionFactory.currentSession.lobHelper.createBlob(file.inputStream, file.size)
    }
}
