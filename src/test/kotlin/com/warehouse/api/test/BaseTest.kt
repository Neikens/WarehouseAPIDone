package com.warehouse.api.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * Bāzes klase visiem testiem
 * Nodrošina kopējo konfigurāciju un funkcionalitāti
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
abstract class BaseTest {
    // Kopējā testa funkcionalitāte var tikt pievienota šeit
    // Piemēram, kopējās palīgmetodes vai konfigurācija
}