package com.warehouse.api.dto

data class ImportResultDto(
    val success: Boolean,
    val message: String,
    val importedRecords: Int = 0,
    val errors: List<String> = emptyList()
)
