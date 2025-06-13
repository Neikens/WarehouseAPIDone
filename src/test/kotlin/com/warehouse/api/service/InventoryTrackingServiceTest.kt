package com.warehouse.api.service

import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.model.InventoryItem
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.WarehouseRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.util.Optional // ADD THIS IMPORT

class InventoryTrackingServiceTest {

    private val inventoryItemRepository: InventoryItemRepository = mockk()
    private val warehouseRepository: WarehouseRepository = mockk()
    private val metricsService: MetricsService = mockk(relaxed = true)
    private val auditService: AuditService = mockk(relaxed = true)

    private lateinit var inventoryTrackingService: InventoryTrackingService

    @BeforeEach
    fun setUp() {
        inventoryTrackingService = InventoryTrackingService(
            inventoryItemRepository,
            warehouseRepository,
            metricsService,
            auditService
        )
    }

    private fun createTestProduct(id: Long = 1L, code: String = "TEST-001"): Product {
        val product = Product(
            code = code,
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = BigDecimal("10.00")
        )
        product.javaClass.getDeclaredField("id").apply {
            isAccessible = true
            set(product, id)
        }
        return product
    }

    private fun createTestWarehouse(id: Long = 1L, name: String = "Test Warehouse"): Warehouse {
        val warehouse = Warehouse(
            name = name,
            location = "Test Location",
            capacity = 1000.0
        )
        warehouse.javaClass.getDeclaredField("id").apply {
            isAccessible = true
            set(warehouse, id)
        }
        return warehouse
    }

    private fun createTestInventoryItem(
        id: Long = 1L,
        product: Product,
        warehouse: Warehouse,
        quantity: BigDecimal = BigDecimal("100.00"),
        minimumLevel: BigDecimal? = null
    ): InventoryItem {
        val item = InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = quantity,
            minimumLevel = minimumLevel
        )
        item.javaClass.getDeclaredField("id").apply {
            isAccessible = true
            set(item, id)
        }
        return item
    }

    @Test
    fun `should check low stock items`() {
        // Given
        val threshold = BigDecimal("10.00")
        val product = createTestProduct()
        val warehouse = createTestWarehouse()
        val lowStockItem = createTestInventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = BigDecimal("5.00"),
            minimumLevel = BigDecimal("10.00")
        )

        every { inventoryItemRepository.findItemsBelowThreshold(threshold) } returns listOf(lowStockItem)

        // When
        val result = inventoryTrackingService.checkLowStock(threshold)

        // Then
        assertEquals(1, result.size)
        assertEquals(lowStockItem, result[0])
        verify { inventoryItemRepository.findItemsBelowThreshold(threshold) }
    }

    @Test
    fun `should generate inventory report for warehouse`() {
        // Given
        val warehouseId = 1L
        val product = createTestProduct()
        val warehouse = createTestWarehouse()
        val inventoryItem = createTestInventoryItem(product = product, warehouse = warehouse)

        // FIXED: Changed from existsById to findById and return Optional
        every { warehouseRepository.findById(warehouseId) } returns Optional.of(warehouse)
        every { inventoryItemRepository.findByWarehouseId(warehouseId) } returns listOf(inventoryItem)

        // When
        val result = inventoryTrackingService.generateInventoryReport(warehouseId)

        // Then
        assertNotNull(result)
        assertTrue(result.containsKey("warehouse"))
        assertEquals(warehouseId, (result["warehouse"] as Map<*, *>)["id"])
        // FIXED: Changed verify call to match the actual method being called
        verify { warehouseRepository.findById(warehouseId) }
        verify { inventoryItemRepository.findByWarehouseId(warehouseId) }
    }

    @Test
    fun `should return empty list when no low stock items found`() {
        // Given
        val threshold = BigDecimal("10.00")
        every { inventoryItemRepository.findItemsBelowThreshold(threshold) } returns emptyList()

        // When
        val result = inventoryTrackingService.checkLowStock(threshold)

        // Then
        assertTrue(result.isEmpty())
        verify { inventoryItemRepository.findItemsBelowThreshold(threshold) }
    }
}