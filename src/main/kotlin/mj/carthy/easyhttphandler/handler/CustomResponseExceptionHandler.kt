package mj.carthy.easyhttphandler.handler

import com.mongodb.MongoWriteException
import mj.carthy.easyutils.exception.CustomException
import mj.carthy.easyutils.helper.Errors.Companion.SERVER_ERROR
import mj.carthy.easyutils.helper.Errors.Companion.VALIDATION_ERROR
import mj.carthy.easyutils.helper.error
import mj.carthy.easyutils.helper.toMutableSet
import mj.carthy.easyutils.model.CustomFieldError
import mj.carthy.easyutils.model.ErrorDetails
import org.apache.commons.lang3.StringUtils.EMPTY
import org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class CustomResponseExceptionHandler {

    companion object {
        private const val OPEN_BRACKET = "{"
        private const val DOUBLE_DOT = ":"

        private fun createCustomFieldError(fieldError: FieldError): CustomFieldError {
            val defaultMessage: String = fieldError.defaultMessage ?: EMPTY
            val code: String = fieldError.code ?: EMPTY
            return CustomFieldError(fieldError.field, defaultMessage, code)
        }
    }

    @ExceptionHandler(CustomException::class) fun handleCustomException(
            ex: CustomException,
            request: ServerHttpRequest
    ): ResponseEntity<ErrorDetails> = ResponseEntity(error(ex.message, request, ex.httpCode(), ex.code), ex.httpCode())

    @ExceptionHandler(ConstraintViolationException::class) fun handleCustomException(
            ex: ConstraintViolationException,
            request: ServerHttpRequest
    ): ResponseEntity<ErrorDetails> = ResponseEntity(error(ex, request, BAD_REQUEST, SERVER_ERROR), BAD_REQUEST)

    @ExceptionHandler(MethodArgumentNotValidException::class) fun handleMethodArgumentNotValidException(
            ex: MethodArgumentNotValidException,
            request: ServerHttpRequest
    ): ResponseEntity<ErrorDetails> = ResponseEntity(error(
            ex,
            request,
            BAD_REQUEST,
            SERVER_ERROR
    ).copy(fieldErrors = ex.bindingResult.fieldErrors.stream().map { elem ->
        createCustomFieldError(elem)
    }.toMutableSet()), BAD_REQUEST)

    @ExceptionHandler(WebExchangeBindException::class) fun handleWebExchangeBindException(
            ex: WebExchangeBindException,
            request: ServerHttpRequest
    ): ResponseEntity<ErrorDetails> = ResponseEntity(error(
            ex,
            request,
            BAD_REQUEST,
            VALIDATION_ERROR
    ).copy(fieldErrors = ex.bindingResult.fieldErrors.stream().map { elem ->
        createCustomFieldError(elem)
    }.toMutableSet()), BAD_REQUEST)

    @ExceptionHandler(MongoWriteException::class) fun handleMongoWriteException(
        ex: MongoWriteException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorDetails> {
        val localizedMessage: String = ex.localizedMessage
        val field: String = localizedMessage.substring(localizedMessage.lastIndexOf(OPEN_BRACKET) + INTEGER_ONE, localizedMessage.lastIndexOf(DOUBLE_DOT))
        val message: String = ex.error.category.toString() + field
        return ResponseEntity(error(message, request, INTERNAL_SERVER_ERROR, SERVER_ERROR), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(NullPointerException::class) fun handleNullPointerException(
            ex: NullPointerException,
            request: ServerHttpRequest
    ): ResponseEntity<ErrorDetails> = ResponseEntity(error(ex, request, BAD_REQUEST, SERVER_ERROR), BAD_REQUEST)
}