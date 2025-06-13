package com.warehouse.api.controller

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/debug")
@Tag(name = "System", description = "System monitoring and health check endpoints")
class DebugController {

    @GetMapping("/health")
    @Operation(summary = "API health check - PUBLIC ACCESS")
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "timestamp" to LocalDateTime.now().toString(),
            "version" to "1.0.0",
            "service" to "Warehouse Management API"
        )
    }

    @GetMapping("/auth-status")
    @Operation(summary = "Check current authentication status - PUBLIC ACCESS")
    fun getAuthStatus(): Map<String, Any?> {
        val auth: Authentication? = SecurityContextHolder.getContext().authentication

        val isReallyAuthenticated = auth != null &&
                auth.isAuthenticated &&
                auth !is AnonymousAuthenticationToken

        return mapOf(
            "isAuthenticated" to isReallyAuthenticated,
            "user" to if (isReallyAuthenticated) auth?.name else "anonymous",
            "roles" to if (isReallyAuthenticated) auth?.authorities?.map { it.authority } else listOf("ROLE_ANONYMOUS"),
            "authType" to (auth?.javaClass?.simpleName ?: "None"),
            "timestamp" to LocalDateTime.now().toString()
        )
    }
}