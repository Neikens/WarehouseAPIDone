package com.warehouse.api.service

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

@Service
class AuditService {
    private val logger = LoggerFactory.getLogger(AuditService::class.java)

    fun logAction(action: String, userId: String, details: String) {
        val timestamp = LocalDateTime.now()
        val auditMessage = "ACTION: $action, USER: $userId, TIME: $timestamp, DETAILS: $details"
        logger.info(auditMessage)
    }

    fun logError(action: String, userId: String, error: String) {
        val timestamp = LocalDateTime.now()
        val errorMessage = "ERROR: $action, USER: $userId, TIME: $timestamp, DETAILS: $error"
        logger.error(errorMessage)
    }
}
