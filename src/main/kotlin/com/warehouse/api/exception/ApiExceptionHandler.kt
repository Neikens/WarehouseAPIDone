package com.warehouse.api.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource not found",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Validation failed",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "Data integrity violation",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource not found",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message ?: "An unexpected error occurred",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ApiError(
    val status: Int,
    val message: String,
    val timestamp: Long
)

class ResourceNotFoundException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
