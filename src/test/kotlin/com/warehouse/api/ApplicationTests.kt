package com.warehouse.api.test

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Galvenie aplikācijas testi
 * Pārbauda, vai aplikācijas konteksts ielādējas pareizi
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests : BaseTest() {

	/**
	 * Testē aplikācijas konteksta ielādi
	 * Ja šis tests neizdodas, tas nozīmē, ka ir konfigurācijas problēmas
	 */
	@Test
	fun contextLoads() {
		// Šis tests neizdosies, ja aplikācijas konteksts nevar startēt
		// Nav nepieciešams papildu kods - Spring Boot automātiski pārbauda kontekstu
	}
}