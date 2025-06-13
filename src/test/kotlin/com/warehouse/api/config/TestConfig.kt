package com.warehouse.api.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import javax.sql.DataSource
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType

/**
 * Testu konfigurācijas klase
 * Nodrošina nepieciešamos bean'us testu izpildei
 */
@TestConfiguration
class TestConfig {

    /**
     * Paroles kodētājs testiem
     * Izmanto BCrypt algoritmu drošībai
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Testu lietotāju detaļu serviss
     * Izveido atmiņā esošu admin lietotāju testiem
     */
    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN", "USER")
            .build()

        return InMemoryUserDetailsManager(admin)
    }

    /**
     * Testu drošības konfigurācija
     * Definē piekļuves tiesības dažādiem API endpoint'iem
     */
    @Bean
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            // Atslēdz CSRF aizsardzību testiem
            csrf { disable() }

            // Definē autorizācijas noteikumus
            authorizeRequests {
                authorize("/api/v1/inventory/**", hasRole("ADMIN"))
                authorize("/api/v1/products/**", hasRole("ADMIN"))
                authorize("/api/v1/warehouses/**", hasRole("ADMIN"))
                authorize("/api/v1/transactions/**", hasRole("ADMIN"))
                authorize("/actuator/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize(anyRequest, authenticated)
            }

            // Izmanto HTTP Basic autentifikāciju
            httpBasic {}
        }
        return http.build()
    }

    /**
     * Testu datubāzes konfigurācija
     * Izmanto H2 iegulto datubāzi testiem
     */
    @Bean
    fun dataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build()
    }
}