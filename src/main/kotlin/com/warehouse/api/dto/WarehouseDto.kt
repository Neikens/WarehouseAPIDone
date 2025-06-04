package com.warehouse.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class WarehouseDto(
    val id: Long? = null,
    
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    @field:NotBlank(message = "Location is required")
    val location: String,
    
    @field:Positive(message = "Capacity must be positive")
    val capacity: Double
)
