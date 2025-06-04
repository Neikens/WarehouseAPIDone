package com.warehouse.api.repository

import com.warehouse.api.model.InventoryItem
import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
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

    private lateinit var testWarehouse: Warehouse
    private lateinit var testProduct: Product

    @BeforeEach
    fun setup() {
        inventoryItemRepository.deleteAll()
        productRepository.deleteAll()
        warehouseRepository.deleteAll()

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
    }

    @Test
    fun `should save and retrieve inventory item`() {
        val inventoryItem = InventoryItem(
            product = testProduct,
            warehouse = testWarehouse,
            quantity = BigDecimal("10.0")
        )

        val savedItem = inventoryItemRepository.save(inventoryItem)
        val retrievedItem = inventoryItemRepository.findById(savedItem.id!!).get()

        assertEquals(BigDecimal("10.0"), retrievedItem.quantity)
        assertEquals(testProduct.id, retrievedItem.product.id)
        assertEquals(testWarehouse.id, retrievedItem.warehouse.id)
    }
}