package com.warehouse.api

import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.model.InventoryItem
import java.math.BigDecimal

/**
 * Test helper utilities for creating test data
 */
object TestHelper {

    /**
     * Creates a test product with default values
     */
    fun createTestProduct(
        code: String = "TEST-001",
        name: String = "Test Product",
        description: String = "Test Product Description",
        category: String = "Test Category",
        price: BigDecimal = BigDecimal("10.00")
    ): Product {
        return Product(
            code = code,
            name = name,
            description = description,
            category = category,
            price = price
        )
    }

    /**
     * Creates a test warehouse with default values
     */
    fun createTestWarehouse(
        name: String = "Test Warehouse",
        location: String = "Test Location",
        capacity: Double = 1000.0
    ): Warehouse {
        return Warehouse(
            name = name,
            location = location,
            capacity = capacity
        )
    }

    /**
     * Creates a test inventory item
     */
    fun createTestInventoryItem(
        product: Product,
        warehouse: Warehouse,
        quantity: BigDecimal = BigDecimal("100.00"),
        minimumLevel: BigDecimal? = null
    ): InventoryItem {
        return InventoryItem(
            product = product,
            warehouse = warehouse,
            quantity = quantity,
            minimumLevel = minimumLevel
        )
    }
}