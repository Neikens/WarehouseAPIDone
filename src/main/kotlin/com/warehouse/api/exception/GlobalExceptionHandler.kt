package com.warehouse.api.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

/**
 * Globālais kļūdu apstrādātājs
 * Papildina ApiExceptionHandler ar drošības un sistēmas kļūdu apstrādi
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Apstrādā autentifikācijas kļūdas
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(
        e: BadCredentialsException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Neveiksmīgs autentifikācijas mēģinājums: {}", request.getDescription(false))

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Autentifikācijas kļūda",
            message = "Nederīgi akreditācijas dati",
            path = extractPath(request)
        )
        return ResponseEntity(error, HttpStatus.UNAUTHORIZED)
    }

    /**
     * Apstrādā piekļuves lieguma kļūdas
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        e: AccessDeniedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Piekļuve liegta: {}", request.getDescription(false))

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Piekļuve liegta",
            message = "Jums nav tiesību piekļūt šim resursam",
            path = extractPath(request)
        )
        return ResponseEntity(error, HttpStatus.FORBIDDEN)
    }

    /**
     * Apstrādā ValidationException (specifiska validācijas kļūda)
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        e: ValidationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.debug("Validācijas kļūda: {}", e.message)

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validācijas kļūda",
            message = e.message ?: "Nederīgi dati",
            path = extractPath(request)
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    /**
     * Apstrādā IllegalArgumentException (prioritāte pār RuntimeException)
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        e: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.debug("Nederīgs arguments: {}", e.message)

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Nederīgs pieprasījums",
            message = e.message ?: "Nederīgi pieprasījuma parametri",
            path = extractPath(request)
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    /**
     * Apstrādā IllegalStateException (piemēram, nepietiekami krājumi)
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        e: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.debug("Nederīgs stāvoklis: {}", e.message)

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Nederīgs stāvoklis",
            message = e.message ?: "Operāciju nevar izpildīt pašreizējā stāvoklī",
            path = extractPath(request)
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    /**
     * Apstrādā NoSuchElementException
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        e: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.debug("Resurss nav atrasts: {}", e.message)

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Nav atrasts",
            message = e.message ?: "Pieprasītais resurss nav atrasts",
            path = extractPath(request)
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    /**
     * Apstrādā runtime kļūdas (zemākā prioritāte)
     * SVARĪGI: Šis ir pēdējais, jo IllegalArgumentException ir RuntimeException apakšklase
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        e: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        // Pārbaudām, vai tas nav jau apstrādāts specifiskais izņēmums
        if (e is IllegalArgumentException || e is NoSuchElementException || e is IllegalStateException) {
            // Ļaujam specifiskajiem handler'iem apstrādāt
            throw e
        }

        logger.error("Runtime kļūda: ", e)

        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Sistēmas kļūda",
            message = "Radusies neparedzēta kļūda. Lūdzu, mēģiniet vēlāk.",
            path = extractPath(request)
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * Izvelk ceļu no WebRequest objekta
     */
    private fun extractPath(request: WebRequest): String {
        val description = request.getDescription(false)
        return if (description.startsWith("uri=")) {
            description.substring(4)
        } else {
            description
        }
    }
}

/**
 * Kļūdas atbildes datu klase
 */
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)