package com.warehouse.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.scheduling.annotation.EnableAsync
import java.net.ServerSocket
import java.io.IOException

/**
 * Galvenā lietojumprogrammas klase
 * Konfigurē Spring Boot lietojumprogrammu noliktavu pārvaldības sistēmai
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableJpaAuditing
class Application {

	/**
	 * Konfigurē web serveri ar automātisku porta atrašanu
	 * Tikai production un development profilos
	 */
	@Bean
	@Profile("!integration & !test")
	fun webServerFactoryCustomizer() = WebServerFactoryCustomizer<TomcatServletWebServerFactory> { factory ->
		var port = 8082

		while (!isPortAvailable(port)) {
			port++
			if (port > 8100) {
				throw RuntimeException("Nevar atrast brīvu portu diapazonā 8082-8100")
			}
		}

		factory.setPort(port)
		println("🚀 Noliktavu pārvaldības sistēmas serveris startē uz porta: $port")
		println("📊 Swagger UI būs pieejams: http://localhost:$port/swagger-ui.html")
		println("📋 API dokumentācija: http://localhost:$port/api-docs")
	}

	private fun isPortAvailable(port: Int): Boolean = try {
		ServerSocket(port).use {
			println("✅ Ports $port ir pieejams")
			true
		}
	} catch (e: IOException) {
		println("❌ Ports $port nav pieejams: ${e.message}")
		false
	}
}

fun main(args: Array<String>) {
	println("🏭 Startē noliktavu pārvaldības sistēma...")
	runApplication<Application>(*args)
}