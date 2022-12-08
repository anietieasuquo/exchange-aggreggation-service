package com.tradesoft.exchangeaggregationservice.core.repository

import org.springframework.web.multipart.MultipartFile
import java.sql.Blob

interface GenericMetadataEntityRepository {
    fun fileToBlob(file: MultipartFile): Blob?
}
