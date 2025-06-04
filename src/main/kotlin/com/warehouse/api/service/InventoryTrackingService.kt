package com.warehouse.api.service

import com.warehouse.api.model.InventoryItem
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.WarehouseRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class InventoryTrackingService(
    private val inventoryItemRepository: InventoryItemRepository,
    private val warehouseRepository: WarehouseRepository,
    private val metricsService: MetricsService
) {
    fun checkLowStock(threshold: BigDecimal): List<InventoryItem> {
        val lowStockItems = inventoryItemRepository.findItemsBelowThreshold(threshold)
        lowStockItems.forEach { item ->
            item.product.id?.let { productId ->
                metricsService.recordLowStockAlert(productId)
            }
        }
        return lowStockItems
    }

    fun generateInventoryReport(warehouseId: Long): Map<String, Any> {
        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { NoSuchElementException("Warehouse not found") }

        val inventoryItems = inventoryItemRepository.findByWarehouseId(warehouseId)
        val totalQuantity = inventoryItems.sumOf { it.quantity }

        // Record the inventory check in metrics
        warehouse.id?.let { id ->
            metricsService.recordInventoryCheck(id)
            metricsService.updateInventoryLevel(id, warehouse.capacity)
        }

        return mapOf(
            "timestamp" to LocalDateTime.now(),
            "warehouse" to mapOf(
                "id" to warehouse.id,
                "name" to warehouse.name,
                "location" to warehouse.location,
                "capacity" to warehouse.capacity
            ),
            "inventoryItems" to inventoryItems,
            "totalQuantity" to totalQuantity
        )
    }
}