package com.warehouse.api.service

import com.warehouse.api.model.InventoryItem
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.ProductRepository
import com.warehouse.api.repository.WarehouseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class InventoryService(
    private val inventoryItemRepository: InventoryItemRepository,
    private val productRepository: ProductRepository,
    private val warehouseRepository: WarehouseRepository
) {

    fun getAllInventoryItems(): List<InventoryItem> =
        inventoryItemRepository.findAll()

    fun getInventoryItemById(id: Long): InventoryItem =
        inventoryItemRepository.findById(id)
            .orElseThrow { NoSuchElementException("Inventory item not found with id: $id") }

    fun getInventoryByWarehouse(warehouseId: Long): List<InventoryItem> =
        inventoryItemRepository.findByWarehouseId(warehouseId)

    fun getInventoryByProduct(productId: Long): List<InventoryItem> =
        inventoryItemRepository.findByProductId(productId)

    @Transactional
    fun updateInventoryQuantity(
        productId: Long,
        warehouseId: Long,
        quantity: BigDecimal
    ): InventoryItem {
        try {
            println("Updating inventory for product $productId in warehouse $warehouseId with quantity $quantity")

            // Verify product exists
            val product = productRepository.findById(productId)
                .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
            println("Found product: ${product.id}")

            // Verify warehouse exists
            val warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow { NoSuchElementException("Warehouse not found with id: $warehouseId") }
            println("Found warehouse: ${warehouse.id}")

            // Check if inventory item already exists
            val existingItem = inventoryItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)

            if (existingItem != null) {
                println("Found existing inventory item with id: ${existingItem.id}")
                // Create a copy with updated quantity and updatedAt
                val updatedItem = existingItem.copy(
                    quantity = quantity,
                    updatedAt = LocalDateTime.now()
                )
                println("Saving updated inventory item")
                return inventoryItemRepository.save(updatedItem)
            } else {
                println("No existing inventory item found, creating new one")
                // Create a new item with the specified quantity
                val newItem = InventoryItem(
                    product = product,
                    warehouse = warehouse,
                    quantity = quantity
                )
                println("Saving new inventory item")
                return inventoryItemRepository.save(newItem)
            }
        } catch (e: Exception) {
            println("ERROR updating inventory: ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Transactional
    fun adjustInventory(
        productId: Long,
        warehouseId: Long,
        adjustment: BigDecimal
    ): InventoryItem {
        val inventoryItem = inventoryItemRepository
            .findByProductIdAndWarehouseId(productId, warehouseId)
            ?: throw NoSuchElementException("Inventory item not found")

        val newQuantity = inventoryItem.quantity.add(adjustment)
        if (newQuantity < BigDecimal.ZERO) {
            throw IllegalStateException("Inventory cannot be negative")
        }

        val updatedItem = inventoryItem.copy(
            quantity = newQuantity,
            updatedAt = LocalDateTime.now()
        )

        return inventoryItemRepository.save(updatedItem)
    }

    @Transactional
    fun createInventoryItem(
        productId: Long,
        warehouseId: Long,
        quantity: BigDecimal
    ): InventoryItem {
        try {
            println("Creating inventory item for product $productId in warehouse $warehouseId with quantity $quantity")

            val product = productRepository.findById(productId)
                .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
            println("Found product: ${product.id}")

            val warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow { NoSuchElementException("Warehouse not found with id: $warehouseId") }
            println("Found warehouse: ${warehouse.id}")

            // Check if an item already exists
            val existingItem = inventoryItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            if (existingItem != null) {
                println("Item already exists with id: ${existingItem.id}, updating instead")
                val updatedItem = existingItem.copy(
                    quantity = quantity,
                    updatedAt = LocalDateTime.now()
                )
                return inventoryItemRepository.save(updatedItem)
            }

            val newItem = InventoryItem(
                product = product,
                warehouse = warehouse,
                quantity = quantity
            )

            println("Saving new inventory item")
            return inventoryItemRepository.save(newItem)
        } catch (e: Exception) {
            println("ERROR creating inventory: ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}