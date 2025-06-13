package com.warehouse.api.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * Bāzes klase integrācijas testiem
 * Nodrošina kopējo konfigurāciju visiem integrācijas testiem
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Automātiski atceļ transakcijas pēc katra testa
abstract class BaseIntegrationTest