package com.warehouse.api.service

import com.warehouse.api.model.InventoryItem
import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.ProductRepository
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
class InventoryServiceTest {

    @Mock
    private lateinit var inventoryItemRepository: InventoryItemRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var warehouseRepository: WarehouseRepository

    private lateinit var inventoryService: InventoryService

    private lateinit var testProduct: Product
    private lateinit var testWarehouse: Warehouse
    private lateinit var testInventoryItem: InventoryItem

    @BeforeEach
    fun setup() {
        inventoryService = InventoryService(
            inventoryItemRepository,
            productRepository,
            warehouseRepository
        )

        testProduct = Product(
            id = 1L,
            code = "TEST001",
            description = "Test Product",
            barcode = "123456789",
            category = "Test Category",
            name = "Test Product Name",
            price = BigDecimal("9.99")
        )

        testWarehouse = Warehouse(
            id = 1L,
            name = "Test Warehouse",
            location = "Test Location",
            capacity = 1000.0
        )

        testInventoryItem = InventoryItem(
            id = 1L,
            product = testProduct,
            warehouse = testWarehouse,
            quantity = BigDecimal.TEN
        )
    }

    @Test
    fun `getAllInventoryItems should return all items`() {
        `when`(inventoryItemRepository.findAll()).thenReturn(listOf(testInventoryItem))

        val result = inventoryService.getAllInventoryItems()

        assertEquals(1, result.size)
        assertEquals(testInventoryItem, result[0])
        verify(inventoryItemRepository).findAll()
    }

    @Test
    fun `getInventoryItemById should return item when exists`() {
        `when`(inventoryItemRepository.findById(1L))
            .thenReturn(Optional.of(testInventoryItem))

        val result = inventoryService.getInventoryItemById(1L)

        assertEquals(testInventoryItem, result)
        verify(inventoryItemRepository).findById(1L)
    }

    @Test
    fun `getInventoryItemById should throw exception when item doesn't exist`() {
        `when`(inventoryItemRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            inventoryService.getInventoryItemById(1L)
        }
    }

    @Test
    fun `updateInventoryQuantity should update existing item`() {
        val newQuantity = BigDecimal("15.0")

        `when`(productRepository.findById(1L)).thenReturn(Optional.of(testProduct))
        `when`(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse))
        `when`(inventoryItemRepository.findByProductIdAndWarehouseId(1L, 1L))
            .thenReturn(testInventoryItem)
        `when`(inventoryItemRepository.save(any())).thenReturn(
            testInventoryItem.copy(quantity = newQuantity)
        )

        val result = inventoryService.updateInventoryQuantity(1L, 1L, newQuantity)

        assertEquals(newQuantity, result.quantity)
        verify(inventoryItemRepository).save(any())
    }
}
