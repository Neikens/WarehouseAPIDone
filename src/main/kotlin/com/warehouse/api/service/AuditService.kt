package com.warehouse.api.service

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory

/**
 * Audita serviss
 * Nodrošina sistēmas darbību un kļūdu reģistrēšanu auditēšanas nolūkos
 */
@Service
class AuditService {

    private val logger = LoggerFactory.getLogger(AuditService::class.java)
    private val auditLogger = LoggerFactory.getLogger("AUDIT")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Reģistrē lietotāja veikto darbību
     * @param action Veiktā darbība (piemēram, "CREATE_PRODUCT", "UPDATE_INVENTORY")
     * @param userId Lietotāja identifikators
     * @param details Papildu informācija par darbību
     * @param entityId Entītijas ID, ja attiecas
     */
    fun logAction(
        action: String,
        userId: String,
        details: String,
        entityId: Long? = null
    ) {
        val timestamp = LocalDateTime.now()
        val formattedTime = timestamp.format(dateTimeFormatter)

        val auditMessage = buildString {
            append("DARBĪBA: $action")
            append(" | LIETOTĀJS: $userId")
            append(" | LAIKS: $formattedTime")
            append(" | DETAĻAS: $details")
            entityId?.let { append(" | ENTĪTIJAS_ID: $it") }
        }

        auditLogger.info(auditMessage)
        logger.debug("Audita ieraksts izveidots: $action")
    }

    /**
     * Reģistrē sistēmas kļūdu
     * @param action Darbība, kuras laikā radās kļūda
     * @param userId Lietotāja identifikators
     * @param error Kļūdas apraksts
     * @param exception Izņēmuma objekts (ja pieejams)
     */
    fun logError(
        action: String,
        userId: String,
        error: String,
        exception: Exception? = null
    ) {
        val timestamp = LocalDateTime.now()
        val formattedTime = timestamp.format(dateTimeFormatter)

        val errorMessage = buildString {
            append("KĻŪDA: $action")
            append(" | LIETOTĀJS: $userId")
            append(" | LAIKS: $formattedTime")
            append(" | APRAKSTS: $error")
            exception?.let {
                append(" | IZŅĒMUMS: ${it.javaClass.simpleName}")
                append(" | ZIŅOJUMS: ${it.message}")
            }
        }

        auditLogger.error(errorMessage, exception)
        logger.error("Kļūdas audita ieraksts izveidots: $action", exception)
    }

    /**
     * Reģistrē drošības notikumu
     * @param event Drošības notikuma tips
     * @param userId Lietotāja identifikators
     * @param ipAddress IP adrese
     * @param details Papildu informācija
     */
    fun logSecurityEvent(
        event: String,
        userId: String,
        ipAddress: String? = null,
        details: String = ""
    ) {
        val timestamp = LocalDateTime.now()
        val formattedTime = timestamp.format(dateTimeFormatter)

        val securityMessage = buildString {
            append("DROŠĪBA: $event")
            append(" | LIETOTĀJS: $userId")
            append(" | LAIKS: $formattedTime")
            ipAddress?.let { append(" | IP: $it") }
            if (details.isNotEmpty()) append(" | DETAĻAS: $details")
        }

        auditLogger.warn(securityMessage)
        logger.warn("Drošības notikums reģistrēts: $event")
    }

    /**
     * Reģistrē sistēmas veiktspējas metriku
     * @param operation Operācijas nosaukums
     * @param duration Izpildes laiks milisekundēs
     * @param userId Lietotāja identifikators
     */
    fun logPerformanceMetric(
        operation: String,
        duration: Long,
        userId: String? = null
    ) {
        val timestamp = LocalDateTime.now()
        val formattedTime = timestamp.format(dateTimeFormatter)

        val performanceMessage = buildString {
            append("VEIKTSPĒJA: $operation")
            append(" | ILGUMS: ${duration}ms")
            append(" | LAIKS: $formattedTime")
            userId?.let { append(" | LIETOTĀJS: $it") }
        }

        // Brīdina, ja operācija aizņem ilgāk par 5 sekundēm
        if (duration > 5000) {
            auditLogger.warn("LĒNA_OPERĀCIJA: $performanceMessage")
        } else {
            auditLogger.info(performanceMessage)
        }
    }

    /**
     * Reģistrē datu izmaiņas
     * @param entityType Entītijas tips
     * @param entityId Entītijas ID
     * @param operation Operācijas tips (CREATE, UPDATE, DELETE)
     * @param userId Lietotāja identifikators
     * @param oldValues Vecās vērtības (UPDATE gadījumā)
     * @param newValues Jaunās vērtības
     */
    fun logDataChange(
        entityType: String,
        entityId: Long,
        operation: String,
        userId: String,
        oldValues: Map<String, Any?> = emptyMap(),
        newValues: Map<String, Any?> = emptyMap()
    ) {
        val timestamp = LocalDateTime.now()
        val formattedTime = timestamp.format(dateTimeFormatter)

        val changeMessage = buildString {
            append("DATU_IZMAIŅA: $operation")
            append(" | ENTĪTIJA: $entityType")
            append(" | ID: $entityId")
            append(" | LIETOTĀJS: $userId")
            append(" | LAIKS: $formattedTime")

            if (oldValues.isNotEmpty()) {
                append(" | VECĀS_VĒRTĪBAS: $oldValues")
            }
            if (newValues.isNotEmpty()) {
                append(" | JAUNĀS_VĒRTĪBAS: $newValues")
            }
        }

        auditLogger.info(changeMessage)
    }
}