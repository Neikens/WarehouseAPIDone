package com.warehouse.api.dto

import jakarta.validation.constraints.NotBlank

/**
 * Datu pārsūtīšanas objekts ārējās datubāzes savienojuma parametriem
 * Izmanto datu importēšanai no citām sistēmām
 */
data class DatabaseConnectionDto(
    @field:NotBlank(message = "JDBC URL ir obligāts")
    val jdbcUrl: String,

    @field:NotBlank(message = "Lietotājvārds ir obligāts")
    val username: String,

    @field:NotBlank(message = "Parole ir obligāta")
    val password: String,

    @field:NotBlank(message = "Draivera klases nosaukums ir obligāts")
    val driverClassName: String,

    // Papildu parametri savienojuma konfigurācijai
    val connectionTimeout: Int = 30, // sekundes
    val maxRetries: Int = 3,
    val testQuery: String = "SELECT 1"
)