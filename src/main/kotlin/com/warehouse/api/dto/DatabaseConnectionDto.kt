package com.warehouse.api.dto

data class DatabaseConnectionDto(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String
)
