package com.warehouse.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.warehouse.api.model.InventoryItem
import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.ProductRepository
import com.warehouse.api.repository.WarehouseRepository
import com.warehouse.api.test.BaseIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.math.BigDecimal

class InventoryControllerIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var inventoryItemRepository: InventoryItemRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    private lateinit var mockMvc: MockMvc
    private lateinit var testWarehouse: Warehouse
    private lateinit var testProduct: Product
    private lateinit var testInventoryItem: InventoryItem

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        // Clean up the database
        inventoryItemRepository.deleteAll()
        productRepository.deleteAll()
        warehouseRepository.deleteAll()

        // Create test data
        testWarehouse = warehouseRepository.save(
            Warehouse(
                name = "Test Warehouse",
                location = "Test Location",
                capacity = 1000.0
            )
        )

        testProduct = productRepository.save(
            Product(
                code = "TEST-001",
                description = "Test Product",
                barcode = "12345",
                category = "Test Category",
                name = "Test Product Name",
                price = BigDecimal("9.99")
            )
        )

        testInventoryItem = inventoryItemRepository.save(
            InventoryItem(
                product = testProduct,
                warehouse = testWarehouse,
                quantity = BigDecimal("10.0")
            )
        )
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `should update inventory quantity`() {
        val updateRequest = mapOf(
            "quantity" to "15.0"
        )

        mockMvc.perform(
            put("/api/v1/inventory/${testProduct.id}/${testWarehouse.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(httpBasic("admin", "admin"))
        )
            .andDo { println(it.response.contentAsString) }
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.quantity").value("15.0"))
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `should get all inventory items`() {
        mockMvc.perform(get("/api/v1/inventory"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].quantity").value("10.0"))
            .andExpect(jsonPath("$[0].product.code").value("TEST-001"))
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `should get inventory by warehouse`() {
        mockMvc.perform(get("/api/v1/inventory/warehouse/${testWarehouse.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].quantity").value("10.0"))
    }
}
