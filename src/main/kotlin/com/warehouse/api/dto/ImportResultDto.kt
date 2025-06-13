package com.warehouse.api.dto

import java.time.LocalDateTime

/**
 * Datu pārsūtīšanas objekts importa rezultātu atgriešanai
 * Satur detalizētu informāciju par importa procesu
 */
data class ImportResultDto(
    val success: Boolean,
    val message: String,
    val importedRecords: Int = 0,
    val skippedRecords: Int = 0,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime = LocalDateTime.now(),
    val durationMs: Long = 0,

    // Detalizēta statistika pa datu tipiem
    val importStatistics: Map<String, Int> = emptyMap()
) {
    /**
     * Aprēķina importa ilgumu milisekundēs
     */
    fun calculateDuration(): Long {
        return java.time.Duration.between(startTime, endTime).toMillis()
    }

    /**
     * Pārbauda, vai imports bija pilnībā veiksmīgs (bez kļūdām)
     */
    fun isCompletelySuccessful(): Boolean {
        return success && errors.isEmpty()
    }
}