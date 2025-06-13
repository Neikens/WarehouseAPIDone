package com.warehouse.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SwaggerConfig : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // Redirect root to swagger-ui (which now requires auth)
        registry.addRedirectViewController("/", "/swagger-ui/index.html")
        registry.addRedirectViewController("/swagger-ui", "/swagger-ui/index.html")
    }
}