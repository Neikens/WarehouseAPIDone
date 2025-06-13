package com.warehouse.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "basicAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("Noliktavas Pārvaldības API")
                    .description("""
                        REST API sistmēa noliktavas krājumu un transakciju pārvaldībai.
                        
                        🔐 AUTORIZĀCIJA:
                        Visi API galkapunkti prasa Basic autentifikāciju.
                        
                        Pieejamie lietotāji:
                        - admin / admin123 (ADMIN, USER roles)
                        - user / user123 (USER role)
                        
                        📋 INSTRUKCIJAS:
                        1. Noklikšķiniet uz "Authorize" pogas
                        2. Ievadiet: admin / admin123
                        3. Noklikšķiniet "Authorize"
                        
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Warehouse Admin")
                            .email("admin@warehouse.lv")
                    )
            )
            .addServersItem(
                Server()
                    .url("/")
                    .description("Warehouse API Server")
            )
            .components(
                Components()
                    .addSecuritySchemes(securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("basic")
                            .description("HTTP Basic Authentication - admin/admin123")
                    )
            )
            // Apply security globally to show the lock icons
            .security(listOf(SecurityRequirement().addList(securitySchemeName)))
    }
}