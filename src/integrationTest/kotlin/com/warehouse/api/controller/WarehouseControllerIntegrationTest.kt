package com.warehouse.api.controller

import com.warehouse.api.Application
import com.warehouse.api.dto.WarehouseDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WarehouseControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should create warehouse successfully`() {
        val warehouseDto = WarehouseDto(
            name = "Test Warehouse",
            location = "Test Location",
            capacity = 1000.0,
            description = "Test warehouse description"
        )

        mockMvc.perform(
            post("/api/warehouses") // Keep as /api/warehouses since context path is removed
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(warehouseDto))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Test Warehouse"))
            .andExpect(jsonPath("$.location").value("Test Location"))
            .andExpect(jsonPath("$.capacity").value(1000.0))
    }

    @Test
    fun `should get all warehouses`() {
        mockMvc.perform(get("/api/warehouses"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }
}