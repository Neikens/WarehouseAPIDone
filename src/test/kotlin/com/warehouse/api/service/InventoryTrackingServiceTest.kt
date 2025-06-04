package com.warehouse.api.service

import com.warehouse.api.model.InventoryItem
import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.WarehouseRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class InventoryTrackingServiceTest {

    @Mock
    private lateinit var inventoryItemRepository: InventoryItemRepository

    @Mock
    private lateinit var warehouseRepository: WarehouseRepository

    @Mock
    private lateinit var metricsService: MetricsService

    private lateinit var inventoryTrackingService: InventoryTrackingService

    @BeforeEach
    fun setup() {
        inventoryTrackingService = InventoryTrackingService(
            inventoryItemRepository,
            warehouseRepository,
            metricsService
        )
    }

    @Test
    fun `checkLowStock should return items below threshold`() {
        // Given
        val threshold = BigDecimal("10.0")
        val lowStockItems = listOf(
            createTestInventoryItem(BigDecimal("5.0")),
            createTestInventoryItem(BigDecimal("8.0"))
        )

        `when`(inventoryItemRepository.findItemsBelowThreshold(threshold))
            .thenReturn(lowStockItems)

        // When
        val result = inventoryTrackingService.checkLowStock(threshold)

        // Then
        assertEquals(2, result.size)
        verify(inventoryItemRepository).findItemsBelowThreshold(threshold)
    }

    @Test
    fun `generateInventoryReport should return correct report structure`() {
        // Given
        val warehouseId = 1L
        val warehouse = createTestWarehouse(warehouseId)
        val inventoryItems = listOf(
            createTestInventoryItem(BigDecimal("10.0")),
            createTestInventoryItem(BigDecimal("20.0"))
        )

        `when`(warehouseRepository.findById(warehouseId))
            .thenReturn(Optional.of(warehouse))
        `when`(inventoryItemRepository.findByWarehouseId(warehouseId))
            .thenReturn(inventoryItems)

        // When
        val report = inventoryTrackingService.generateInventoryReport(warehouseId)

        // Then
        assertNotNull(report["timestamp"])
        assertEquals(warehouseId, (report["warehouse"] as Map<*, *>)["id"])
        assertEquals(2, (report["inventoryItems"] as List<*>).size)
        assertEquals(BigDecimal("30.0"), report["totalQuantity"])
    }

    private fun createTestInventoryItem(quantity: BigDecimal): InventoryItem {
        return InventoryItem(
            id = 1L,
            product = Product(
                id = 1L,
                code = "TEST-CODE",
                description = "Test Description",
                barcode = "TEST-BARCODE",
                category = "Test Category",
                name = "Test Product Name",
                price = BigDecimal("9.99")
            ),
            warehouse = createTestWarehouse(1L),
            quantity = quantity
        )
    }

    private fun createTestWarehouse(id: Long): Warehouse {
        return Warehouse(
            id = id,
            name = "Test Warehouse",
            location = "Test Location",
            capacity = 1000.0
        )
    }
}
