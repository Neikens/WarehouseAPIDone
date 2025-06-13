package com.warehouse.api.service

import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.model.InventoryItem
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.ProductRepository
import com.warehouse.api.repository.WarehouseRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.util.*

class InventoryServiceTest {

    private val inventoryItemRepository: InventoryItemRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private val warehouseRepository: WarehouseRepository = mockk()
    private val auditService: AuditService = mockk(relaxed = true)
    private val metricsService: MetricsService = mockk(relaxed = true)

    private lateinit var inventoryService: InventoryService

    private lateinit var product: Product
    private lateinit var warehouse: Warehouse
    private lateinit var inventoryItem: InventoryItem

    @BeforeEach
    fun setUp() {
        inventoryService = InventoryService(
            inventoryItemRepository,
            productRepository,
            warehouseRepository,
            auditService,
            metricsService
        )

        // Create test objects with proper initialization
        product = createTestProduct()
        warehouse = createTestWarehouse()
        inventoryItem = createTestInventoryItem()
    }

    private fun createTestProduct(): Product {
        val testProduct = Product(
            code = "TEST-001",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = BigDecimal("10.00")
        )
        // Use reflection to set the id since it's likely a private setter
        testProduct.javaClass.getDeclaredField("id").apply {
            isAccessible = true
            set(testProduct, 1L)
        }
        return testProduct
    }

    private fun createTestWarehouse(): Warehouse {
        val testWarehouse = Warehouse(
            name = "Test Warehouse",
            location = "Test Location",
            capacity = 1000.0
        )
        // Use reflection to set the id
        testWarehouse.javaClass.getDeclaredField("id").apply {
            isAccessible = true
            set(testWarehouse, 1L)
        }
        return testWarehouse
    }

    private fun createTestInventoryItem(): InventoryItem {
        val testItem = InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = BigDecimal("100.00")
        )
        // Use reflection to set the id
        testItem.javaClass.getDeclaredField("id").apply {
            isAccessible = true
            set(testItem, 1L)
        }
        return testItem
    }

    @Test
    fun `should get all inventory items`() {
        // Given
        val inventoryItems = listOf(inventoryItem)
        every { inventoryItemRepository.findAll() } returns inventoryItems

        // When
        val result = inventoryService.getAllInventoryItems()

        // Then
        assertEquals(1, result.size)
        assertEquals(inventoryItem, result[0])
        verify { inventoryItemRepository.findAll() }
    }

    @Test
    fun `should get inventory item by id`() {
        // Given
        every { inventoryItemRepository.findById(1L) } returns Optional.of(inventoryItem)

        // When
        val result = inventoryService.getInventoryItemById(1L)

        // Then
        assertEquals(inventoryItem, result)
        verify { inventoryItemRepository.findById(1L) }
    }

    @Test
    fun `should throw exception when inventory item not found`() {
        // Given
        every { inventoryItemRepository.findById(999L) } returns Optional.empty()

        // When & Then
        assertThrows(NoSuchElementException::class.java) {
            inventoryService.getInventoryItemById(999L)
        }
    }

    @Test
    fun `should get inventory by warehouse`() {
        // Given
        val inventoryItems = listOf(inventoryItem)
        every { warehouseRepository.existsById(1L) } returns true
        every { inventoryItemRepository.findByWarehouseId(1L) } returns inventoryItems

        // When
        val result = inventoryService.getInventoryByWarehouse(1L)

        // Then
        assertEquals(1, result.size)
        assertEquals(inventoryItem, result[0])
        verify { warehouseRepository.existsById(1L) }
        verify { inventoryItemRepository.findByWarehouseId(1L) }
    }

    @Test
    fun `should get inventory by product`() {
        // Given
        val inventoryItems = listOf(inventoryItem)
        every { productRepository.existsById(1L) } returns true
        every { inventoryItemRepository.findByProductId(1L) } returns inventoryItems

        // When
        val result = inventoryService.getInventoryByProduct(1L)

        // Then
        assertEquals(1, result.size)
        assertEquals(inventoryItem, result[0])
        verify { productRepository.existsById(1L) }
        verify { inventoryItemRepository.findByProductId(1L) }
    }
}