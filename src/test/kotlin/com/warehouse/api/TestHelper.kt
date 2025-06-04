package com.warehouse.api.test

import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.model.InventoryItem
import java.math.BigDecimal

object TestHelper {
    fun createTestProduct(
        id: Long? = null,
        code: String = "TEST001",
        description: String = "Test Product",
        barcode: String = "123456789",
        category: String = "Test Category",
        name: String = "Test Product Name",
        price: BigDecimal = BigDecimal("9.99")
    ) = Product(
        id = id,
        code = code,
        description = description,
        barcode = barcode,
        category = category,
        name = name,
        price = price
    )

    fun createTestWarehouse(
        id: Long? = null,
        name: String = "Test Warehouse",
        location: String = "Test Location",
        capacity: Double = 1000.0
    ) = Warehouse(
        id = id,
        name = name,
        location = location,
        capacity = capacity
    )

    fun createTestInventoryItem(
        id: Long? = null,
        product: Product = createTestProduct(),
        warehouse: Warehouse = createTestWarehouse(),
        quantity: BigDecimal = BigDecimal.TEN
    ) = InventoryItem(
        id = id,
        product = product,
        warehouse = warehouse,
        quantity = quantity
    )
}
