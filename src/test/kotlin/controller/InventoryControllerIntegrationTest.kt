package com.warehouse.api.controller

import com.warehouse.api.Application
import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.model.InventoryItem
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.ProductRepository
import com.warehouse.api.repository.WarehouseRepository
import com.warehouse.api.repository.TransactionRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.math.BigDecimal

@SpringBootTest(classes = [Application::class])
@ActiveProfiles("test")
@Transactional
class InventoryControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var inventoryItemRepository: InventoryItemRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var warehouse: Warehouse
    private lateinit var product: Product
    private lateinit var inventoryItem: InventoryItem

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        // Clear database
        transactionRepository.deleteAll()
        inventoryItemRepository.deleteAll()
        productRepository.deleteAll()
        warehouseRepository.deleteAll()

        // Create test data
        warehouse = Warehouse(
            name = "Test Warehouse",
            location = "Test Location",
            capacity = 1000.0
        )
        warehouse = warehouseRepository.save(warehouse)

        product = Product(
            code = "TEST-001",
            description = "Test Product",
            category = "Test Category",
            name = "Test Product",
            price = BigDecimal("10.00")
        )
        product = productRepository.save(product)

        inventoryItem = InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = BigDecimal("100.00")
        )
        inventoryItem = inventoryItemRepository.save(inventoryItem)
    }

    @Test
    fun `should get all inventory items`() {
        mockMvc.perform(get("/api/v1/inventory"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `context loads`() {
        // Simple test to verify Spring context loads
    }
}