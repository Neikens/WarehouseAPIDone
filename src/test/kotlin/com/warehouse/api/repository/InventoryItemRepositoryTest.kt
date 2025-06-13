package com.warehouse.api.repository

import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.model.InventoryItem
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

@DataJpaTest
@ActiveProfiles("test")
class InventoryItemRepositoryTest {

    @Autowired
    private lateinit var inventoryItemRepository: InventoryItemRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    private lateinit var warehouse: Warehouse
    private lateinit var product: Product

    @BeforeEach
    fun setUp() {
        warehouse = Warehouse(
            name = "Test Warehouse",
            location = "Test Location",
            capacity = 1000.0
        )
        warehouse = warehouseRepository.save(warehouse)

        product = Product(
            code = "TEST-001",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = BigDecimal("10.00")
        )
        product = productRepository.save(product)
    }

    @Test
    fun `should save and find inventory item`() {
        // Given
        val inventoryItem = InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = BigDecimal("100.00")
        )

        // When
        val saved = inventoryItemRepository.save(inventoryItem)
        val found = inventoryItemRepository.findById(saved.id!!)

        // Then
        assertTrue(found.isPresent)
        assertEquals(saved.id, found.get().id)
        assertEquals(BigDecimal("100.00"), found.get().quantity)
    }

    @Test
    fun `should find inventory items by warehouse id`() {
        // Given
        val inventoryItem = InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = BigDecimal("100.00")
        )
        inventoryItemRepository.save(inventoryItem)

        // When
        val items = inventoryItemRepository.findByWarehouseId(warehouse.id!!)

        // Then
        assertEquals(1, items.size)
        assertEquals(warehouse.id, items[0].warehouse.id)
    }

    @Test
    fun `should find inventory items by product id`() {
        // Given
        val inventoryItem = InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = BigDecimal("100.00")
        )
        inventoryItemRepository.save(inventoryItem)

        // When
        val items = inventoryItemRepository.findByProductId(product.id!!)

        // Then
        assertEquals(1, items.size)
        assertEquals(product.id, items[0].product.id)
    }

    @Test
    fun `should find inventory item by product and warehouse`() {
        // Given
        val inventoryItem = InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = BigDecimal("100.00")
        )
        inventoryItemRepository.save(inventoryItem)

        // When
        val found = inventoryItemRepository.findByProductIdAndWarehouseId(product.id!!, warehouse.id!!)

        // Then
        assertNotNull(found)
        assertEquals(product.id, found?.product?.id)
        assertEquals(warehouse.id, found?.warehouse?.id)
    }

    @Test
    fun `should find items below minimum level`() {
        // Given
        val lowStockItem = InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = BigDecimal("5.00"),
            minimumLevel = BigDecimal("10.00")
        )
        inventoryItemRepository.save(lowStockItem)

        // When
        val lowStockItems = inventoryItemRepository.findItemsBelowMinimumLevel()

        // Then
        assertEquals(1, lowStockItems.size)
        assertTrue(lowStockItems[0].quantity < lowStockItems[0].minimumLevel!!)
    }
}