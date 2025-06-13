package com.warehouse.api.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

/**
 * Globālais API kļūdu apstrādātājs
 * Nodrošina vienotu kļūdu apstrādi visai aplikācijai
 */
@ControllerAdvice
class ApiExceptionHandler {

    /**
     * Apstrādā resursa neatrašanas kļūdas
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        ex: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resurss nav atrasts",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false)
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    /**
     * Apstrādā validācijas kļūdas
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidation(
        ex: ValidationException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Validācijas kļūda",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false)
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    /**
     * Apstrādā Spring validācijas kļūdas (@Valid anotācijas)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.fieldErrors
        val errorMessage = fieldErrors.joinToString("; ") { error ->
            "${error.field}: ${error.defaultMessage}"
        }

        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Validācijas kļūda: $errorMessage",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false),
            validationErrors = fieldErrors.associate { it.field to (it.defaultMessage ?: "Nederīga vērtība") }
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    /**
     * Apstrādā datubāzes integritātes pārkāpumus
     */
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(
        ex: DataIntegrityViolationException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val message = when {
            ex.message?.contains("unique", ignoreCase = true) == true ->
                "Ieraksts ar šādiem datiem jau eksistē"
            ex.message?.contains("foreign key", ignoreCase = true) == true ->
                "Nevar dzēst ierakstu - pastāv saistītie dati"
            else -> "Datu integritātes pārkāpums"
        }

        val error = ApiError(
            status = HttpStatus.CONFLICT.value(),
            message = message,
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false)
        )
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    /**
     * Apstrādā NoSuchElementException (kad elements nav atrasts)
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(
        ex: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Pieprasītais resurss nav atrasts",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false)
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    /**
     * Apstrādā IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Nederīgi parametri",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false)
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    /**
     * Apstrādā IllegalStateException
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "Operācija nav iespējama pašreizējā stāvoklī",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false)
        )
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    /**
     * Apstrādā visas pārējās neparedzētās kļūdas
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "Radusies neparedzēta sistēmas kļūda",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false),
            details = if (isDebugMode()) ex.message else null
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * Pārbauda, vai ir ieslēgts debug režīms
     */
    private fun isDebugMode(): Boolean {
        return System.getProperty("debug", "false").toBoolean()
    }
}

/**
 * API kļūdas datu klase
 */
data class ApiError(
    val status: Int,
    val message: String,
    val timestamp: LocalDateTime,
    val path: String,
    val validationErrors: Map<String, String>? = null,
    val details: String? = null
)

/**
 * Pielāgotas izņēmumu klases
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)
class BusinessLogicException(message: String) : RuntimeException(message)