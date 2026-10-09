package dev.txu.common

import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val code: String,
    val message: String,
    val correlationId: String,
    val fields: Map<String, String>? = null,
)

@RestControllerAdvice
class ApiErrorHandler {
    @ExceptionHandler(ApiException::class)
    fun apiError(
        error: ApiException,
        request: HttpServletRequest,
    ) = response(error.status, error.code, error.message, request)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(
        error: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val fields =
            error.bindingResult.allErrors.associate { item ->
                val name = (item as? FieldError)?.field ?: "request"
                name to (item.defaultMessage ?: "Invalid value")
            }
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Check the highlighted fields.", request, fields)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun forbidden(
        error: AccessDeniedException,
        request: HttpServletRequest,
    ) = response(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this action.", request)

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun conflict(
        error: DataIntegrityViolationException,
        request: HttpServletRequest,
    ) = response(HttpStatus.CONFLICT, "CONFLICT", "That change conflicts with the current record.", request)

    @ExceptionHandler(Exception::class)
    fun unexpected(
        error: Exception,
        request: HttpServletRequest,
    ) = response(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong. Try again.", request)

    private fun response(
        status: HttpStatus,
        code: String,
        message: String,
        request: HttpServletRequest,
        fields: Map<String, String>? = null,
    ) = ResponseEntity.status(status).body(ErrorResponse(code, message, correlationId(request), fields))

    private fun correlationId(request: HttpServletRequest) = request.getAttribute(CorrelationFilter.ATTRIBUTE)?.toString() ?: "unavailable"
}
