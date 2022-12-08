package com.tradesoft.exchangeaggregationservice.periphery.controller

import com.tradesoft.exchangeaggregationservice.exception.BusinessException
import com.tradesoft.exchangeaggregationservice.exception.ErrorCategory.*
import com.tradesoft.exchangeaggregationservice.exception.ErrorResponse
import com.tradesoft.exchangeaggregationservice.periphery.boundary.exit.ExceptionMessage
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
@RestController
class RestControllerAdvice : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException, request: WebRequest): ResponseEntity<ExceptionMessage> =
        createBasicExceptionMessage(
            exception = ex,
            status = getStatusFromBusinessErrorResponse(ex.errorResponse),
            request = request,
            additionalMessage = "An error occurred"
        )

    @ExceptionHandler(Exception::class)
    fun handleAllOtherException(ex: Exception, request: WebRequest): ResponseEntity<ExceptionMessage> =
        createBasicExceptionMessage(
            exception = ex,
            status = INTERNAL_SERVER_ERROR,
            request = request,
            additionalMessage = "An error occurred"
        )

    private fun createBasicExceptionMessage(
        exception: Exception,
        status: HttpStatus,
        request: WebRequest,
        additionalMessage: String
    ): ResponseEntity<ExceptionMessage> =
        ResponseEntity(
            ExceptionMessage(
                message = exception.message ?: additionalMessage,
                status = status.value(),
                error = exception.javaClass.name,
                path = request.contextPath
            ), status
        ).also { log.error("An error occurred ", exception) }

    private fun getStatusFromBusinessErrorResponse(errorResponse: ErrorResponse): HttpStatus =
        when (errorResponse.errorCategory) {
            INTERNAL_PROCESS_ERROR -> INTERNAL_SERVER_ERROR
            THIRD_PARTY_COMMUNICATION_ERROR -> FAILED_DEPENDENCY
            USER_INPUT_ERROR -> BAD_REQUEST
        }
}
