package com.warehouse.api.service

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class MetricsService(private val registry: MeterRegistry) {

    fun recordTransaction(type: String) {
        registry.counter("warehouse.transactions", "type", type).increment()
    }

    fun recordProductOperation(operation: String) {
        registry.counter("warehouse.products", "operation", operation).increment()
    }

    fun updateInventoryLevel(warehouseId: Long, level: Double) {
        registry.gauge("warehouse.inventory.level", level)
    }

    fun recordInventoryCheck(warehouseId: Long) {
        registry.counter("warehouse.inventory.checks", "warehouseId", warehouseId.toString()).increment()
    }

    fun recordLowStockAlert(productId: Long) {
        registry.counter("warehouse.inventory.lowstock", "productId", productId.toString()).increment()
    }

    fun recordInventoryUpdate(warehouseId: Long, productId: Long) {
        registry.counter(
            "warehouse.inventory.updates",
            "warehouseId", warehouseId.toString(),
            "productId", productId.toString()
        ).increment()
    }
}