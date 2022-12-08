package com.tradesoft.exchangeaggregationservice.core.util

import com.tradesoft.exchangeaggregationservice.core.business.enums.ExchangeType
import com.tradesoft.exchangeaggregationservice.core.domain.ExchangeMetadataEntity
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVFormat.DEFAULT
import java.io.InputStream

object CsvParser {
    fun readCsvToExchangeMetadataEntity(
        uploadId: Long,
        exchangeType: ExchangeType,
        inputStream: InputStream
    ): List<ExchangeMetadataEntity> =
        CSVFormat.Builder.create(DEFAULT).apply {
            setIgnoreSurroundingSpaces(true)
        }.build().parse(inputStream.reader())
            .drop(1)
            .map {
                ExchangeMetadataEntity(
                    exchangeType = exchangeType,
                    dataKey = it[0],
                    dataValue = it[1],
                    uploadId = uploadId
                )
            }
}
