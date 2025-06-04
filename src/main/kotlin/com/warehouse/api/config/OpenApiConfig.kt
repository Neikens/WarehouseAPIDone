package com.warehouse.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Warehouse Management API")
                    .description("""
                        API for managing warehouse inventory and transactions.
                        
                        Features:
                        - Product management
                        - Warehouse management
                        - Inventory tracking
                        - Transaction history
                        
                        All endpoints require basic authentication.
                    """.trimIndent())
                    .version("1.0")
                    .contact(
                        Contact()
                            .name("Warehouse Admin")
                            .email("admin@warehouse.com")
                    )
            )
    }
}