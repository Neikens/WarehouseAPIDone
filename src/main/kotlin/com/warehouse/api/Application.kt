package com.warehouse.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import java.net.ServerSocket

@SpringBootApplication
class Application {
	@Bean
	fun webServerFactoryCustomizer() = WebServerFactoryCustomizer<TomcatServletWebServerFactory> { factory ->
		var port = 8082 // Your default port
		while (!isPortAvailable(port)) {
			port++
		}
		factory.setPort(port)
		println("Server starting on port: $port") // This will show you which port is being used
	}

	private fun isPortAvailable(port: Int): Boolean = try {
		ServerSocket(port).use { true }
	} catch (e: Exception) {
		false
	}
}

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}