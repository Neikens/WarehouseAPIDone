package com.warehouse.api.test

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests : BaseTest() {

    @Test
    fun contextLoads() {
        // This test will fail if the application context cannot start
    }
}