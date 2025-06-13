package com.warehouse.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Datu pārsūtīšanas objekts noliktavas informācijai
 * Izmanto validāciju, lai nodrošinātu datu kvalitāti
 */
data class WarehouseDto(
    val id: Long? = null,

    @field:NotBlank(message = "Noliktavas nosaukums ir obligāts")
    @field:Size(min = 2, max = 100, message = "Nosaukumam jābūt no 2 līdz 100 rakstzīmēm")
    val name: String,

    @field:NotBlank(message = "Atrašanās vieta ir obligāta")
    @field:Size(min = 2, max = 255, message = "Atrašanās vietai jābūt no 2 līdz 255 rakstzīmēm")
    val location: String,

    @field:Positive(message = "Kapacitātei jābūt pozitīvai")
    val capacity: Double,

    // Papildu informācija
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,

    // Aprēķinātie lauki (tiek aizpildīti servisa slānī)
    val currentUtilization: Double? = null,
    val availableCapacity: Double? = null,
    val utilizationPercentage: Double? = null
) {
    /**
     * Aprēķina pieejamo kapacitāti
     */
    fun calculateAvailableCapacity(): Double {
        return currentUtilization?.let { capacity - it } ?: capacity
    }

    /**
     * Aprēķina izmantošanas procentuālo daļu
     */
    fun calculateUtilizationPercentage(): Double {
        return currentUtilization?.let { (it / capacity) * 100 } ?: 0.0
    }
}