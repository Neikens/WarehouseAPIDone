package com.warehouse.api.service

import com.warehouse.api.model.InventoryItem
import com.warehouse.api.model.StockStatus
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.WarehouseRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

/**
 * Krājumu izsekošanas serviss
 * Nodrošina krājumu monitoringu, brīdinājumus un atskaites
 */
@Service
class InventoryTrackingService(
    private val inventoryItemRepository: InventoryItemRepository,
    private val warehouseRepository: WarehouseRepository,
    private val metricsService: MetricsService,
    private val auditService: AuditService
) {
    private val logger = LoggerFactory.getLogger(InventoryTrackingService::class.java)

    /**
     * Pārbauda krājumus, kas ir zem norādītā sliekšņa
     * @param threshold Minimālais daudzums, zem kura krājumi tiek uzskatīti par zemiem
     * @return Saraksts ar krājumu vienībām zem sliekšņa
     */
    fun checkLowStock(threshold: BigDecimal): List<InventoryItem> {
        logger.info("Pārbauda zemus krājumus ar slieksni: $threshold")

        val lowStockItems = inventoryItemRepository.findItemsBelowThreshold(threshold)

        logger.info("Atrasti ${lowStockItems.size} krājumi zem sliekšņa")

        // Reģistrē brīdinājumus katram zemam krājumam
        lowStockItems.forEach { item ->
            item.product.id?.let { productId ->
                metricsService.recordLowStockAlert(productId)

                auditService.logAction(
                    action = "LOW_STOCK_DETECTED",
                    userId = "SYSTEM",
                    details = "Zemi krājumi: produkts=${item.product.code}, noliktava=${item.warehouse.name}, daudzums=${item.quantity}, slieksnis=$threshold",
                    entityId = item.id
                )
            }
        }

        return lowStockItems
    }

    /**
     * Pārbauda krājumus, kas ir zem to individuālā minimālā līmeņa
     * @return Saraksts ar krājumu vienībām zem minimālā līmeņa
     */
    fun checkItemsBelowMinimumLevel(): List<InventoryItem> {
        logger.info("Pārbauda krājumus zem individuālā minimālā līmeņa")

        val lowStockItems = inventoryItemRepository.findItemsBelowMinimumLevel()

        logger.info("Atrasti ${lowStockItems.size} krājumi zem minimālā līmeņa")

        lowStockItems.forEach { item ->
            item.product.id?.let { productId ->
                metricsService.recordLowStockAlert(productId)

                auditService.logAction(
                    action = "BELOW_MINIMUM_LEVEL",
                    userId = "SYSTEM",
                    details = "Krājumi zem minimālā līmeņa: produkts=${item.product.code}, daudzums=${item.quantity}, minimums=${item.minimumLevel}",
                    entityId = item.id
                )
            }
        }

        return lowStockItems
    }

    /**
     * Pārbauda krājumus, kas pārsniedz maksimālo līmeni
     * @return Saraksts ar krājumu vienībām virs maksimālā līmeņa
     */
    fun checkItemsAboveMaximumLevel(): List<InventoryItem> {
        logger.info("Pārbauda krājumus virs maksimālā līmeņa")

        val excessStockItems = inventoryItemRepository.findItemsAboveMaximumLevel()

        logger.info("Atrasti ${excessStockItems.size} krājumi virs maksimālā līmeņa")

        excessStockItems.forEach { item ->
            auditService.logAction(
                action = "ABOVE_MAXIMUM_LEVEL",
                userId = "SYSTEM",
                details = "Krājumi virs maksimālā līmeņa: produkts=${item.product.code}, daudzums=${item.quantity}, maksimums=${item.maximumLevel}",
                entityId = item.id
            )
        }

        return excessStockItems
    }

    /**
     * Ģenerē detalizētu krājumu atskaiti konkrētai noliktavai
     * @param warehouseId Noliktavas identifikators
     * @return Atskaites dati
     */
    fun generateInventoryReport(warehouseId: Long): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        logger.info("Ģenerē krājumu atskaiti noliktavai: $warehouseId")

        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow {
                logger.error("Noliktava ar ID $warehouseId nav atrasta")
                NoSuchElementException("Noliktava ar ID $warehouseId nav atrasta")
            }

        val inventoryItems = inventoryItemRepository.findByWarehouseId(warehouseId)

        // Aprēķina statistiku
        val totalQuantity = inventoryItems.sumOf { it.quantity }
        val totalItems = inventoryItems.size
        val lowStockItems = inventoryItems.filter { it.isBelowMinimumLevel() }
        val excessStockItems = inventoryItems.filter { it.isAboveMaximumLevel() }
        val normalStockItems = inventoryItems.filter { it.getStockStatus() == StockStatus.NORMAL }

        // Grupē pēc kategorijām
        val itemsByCategory = inventoryItems.groupBy { it.product.category }
        val categoryStats = itemsByCategory.mapValues { (_, items) ->
            mapOf(
                "itemCount" to items.size,
                "totalQuantity" to items.sumOf { it.quantity },
                "lowStockCount" to items.count { it.isBelowMinimumLevel() }
            )
        }

        // Aprēķina vērtību (ja ir cenas)
        val totalValue = inventoryItems.sumOf { item ->
            item.quantity.multiply(item.product.price)
        }

        // Reģistrē metriku - LABOJUMS: noņemts !!
        warehouse.id?.let { id ->
            metricsService.recordInventoryCheck(id)
            metricsService.updateInventoryLevel(id, warehouse.capacity)
        }

        val report = mapOf(
            "timestamp" to LocalDateTime.now(),
            "warehouse" to mapOf(
                "id" to warehouse.id,
                "name" to warehouse.name,
                "location" to warehouse.location,
                "capacity" to warehouse.capacity,
                "description" to warehouse.description
            ),
            "summary" to mapOf(
                "totalItems" to totalItems,
                "totalQuantity" to totalQuantity,
                "totalValue" to totalValue,
                "lowStockItems" to lowStockItems.size,
                "excessStockItems" to excessStockItems.size,
                "normalStockItems" to normalStockItems.size
            ),
            "stockStatus" to mapOf(
                "low" to lowStockItems.map { createItemSummary(it) },
                "excess" to excessStockItems.map { createItemSummary(it) },
                "normal" to normalStockItems.map { createItemSummary(it) }
            ),
            "categoryBreakdown" to categoryStats,
            "inventoryItems" to inventoryItems.map { createDetailedItemInfo(it) }
        )

        val duration = System.currentTimeMillis() - startTime
        auditService.logPerformanceMetric("GENERATE_INVENTORY_REPORT", duration)
        auditService.logAction(
            action = "INVENTORY_REPORT_GENERATED",
            userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
            details = "Ģenerēta krājumu atskaite noliktavai: ${warehouse.name}"
        )

        logger.info("Krājumu atskaite ģenerēta: ${inventoryItems.size} vienības")
        return report
    }

    /**
     * Ģenerē kopējo krājumu atskaiti visām noliktavām
     * @return Kopējā atskaite
     */
    fun generateOverallInventoryReport(): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        logger.info("Ģenerē kopējo krājumu atskaiti")

        val allInventoryItems = inventoryItemRepository.findAll()
        val allWarehouses = warehouseRepository.findAll()

        val totalQuantity = allInventoryItems.sumOf { it.quantity }
        val totalValue = allInventoryItems.sumOf { item ->
            item.quantity.multiply(item.product.price)
        }

        // LABOJUMS: noņemts !! un pievienota null pārbaude
        val warehouseReports = allWarehouses.mapNotNull { warehouse ->
            warehouse.id?.let { id ->
                id to generateInventoryReport(id)
            }
        }.toMap()

        val report = mapOf(
            "timestamp" to LocalDateTime.now(),
            "summary" to mapOf(
                "totalWarehouses" to allWarehouses.size,
                "totalInventoryItems" to allInventoryItems.size,
                "totalQuantity" to totalQuantity,
                "totalValue" to totalValue,
                "lowStockItemsCount" to allInventoryItems.count { it.isBelowMinimumLevel() },
                "excessStockItemsCount" to allInventoryItems.count { it.isAboveMaximumLevel() }
            ),
            "warehouseReports" to warehouseReports
        )

        val duration = System.currentTimeMillis() - startTime
        auditService.logPerformanceMetric("GENERATE_OVERALL_INVENTORY_REPORT", duration)

        logger.info("Kopējā krājumu atskaite ģenerēta")
        return report
    }

    /**
     * Izveido krājumu vienības kopsavilkumu
     */
    private fun createItemSummary(item: InventoryItem): Map<String, Any?> {
        return mapOf(
            "id" to item.id,
            "productCode" to item.product.code,
            "productName" to item.product.name,
            "quantity" to item.quantity,
            "minimumLevel" to item.minimumLevel,
            "maximumLevel" to item.maximumLevel,
            "status" to item.getStockStatus().name
        )
    }

    /**
     * Izveido detalizētu krājumu vienības informāciju
     */
    private fun createDetailedItemInfo(item: InventoryItem): Map<String, Any?> {
        return mapOf(
            "id" to item.id,
            "product" to mapOf(
                "id" to item.product.id,
                "code" to item.product.code,
                "name" to item.product.name,
                "description" to item.product.description,
                "category" to item.product.category,
                "price" to item.product.price
            ),
            "quantity" to item.quantity,
            "minimumLevel" to item.minimumLevel,
            "maximumLevel" to item.maximumLevel,
            "status" to item.getStockStatus().name,
            "value" to item.quantity.multiply(item.product.price),
            "createdAt" to item.createdAt,
            "updatedAt" to item.updatedAt
        )
    }
}