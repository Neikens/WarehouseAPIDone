package com.warehouse.api.service

import com.warehouse.api.model.TransactionType
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.TransactionRepository
import com.warehouse.api.repository.WarehouseRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ReportService(
    private val warehouseRepository: WarehouseRepository,
    private val inventoryItemRepository: InventoryItemRepository,
    private val transactionRepository: TransactionRepository
) {

    fun generateInventoryReport(warehouseId: Long): Map<String, Any> {
        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { NoSuchElementException("Warehouse not found with id: $warehouseId") }

        val inventoryItems = inventoryItemRepository.findByWarehouseId(warehouseId)

        val totalItems = inventoryItems.size
        val totalQuantity = inventoryItems.sumOf { it.quantity }
        val lowStockItems = inventoryItems.filter { it.isBelowMinimumLevel() }
        val excessStockItems = inventoryItems.filter { it.isAboveMaximumLevel() }

        return mapOf(
            "warehouseId" to warehouseId,
            "warehouseName" to warehouse.name,
            "warehouseLocation" to warehouse.location,
            "reportGeneratedAt" to LocalDateTime.now(),
            "totalUniqueItems" to totalItems,
            "totalQuantity" to totalQuantity,
            "lowStockItemsCount" to lowStockItems.size,
            "excessStockItemsCount" to excessStockItems.size,
            "inventoryItems" to inventoryItems.map { item ->
                mapOf(
                    "productId" to item.product.id,
                    "productCode" to item.product.code,
                    "productName" to item.product.name,
                    "productCategory" to item.product.category,
                    "quantity" to item.quantity,
                    "minimumLevel" to item.minimumLevel,
                    "maximumLevel" to item.maximumLevel,
                    "stockStatus" to item.getStockStatus().name,
                    "createdAt" to item.createdAt,
                    "updatedAt" to item.updatedAt
                )
            },
            "lowStockItems" to lowStockItems.map { item ->
                mapOf(
                    "productName" to item.product.name,
                    "productCode" to item.product.code,
                    "currentQuantity" to item.quantity,
                    "minimumLevel" to item.minimumLevel
                )
            }
        )
    }

    fun generateTransactionReport(startDate: LocalDateTime, endDate: LocalDateTime): Map<String, Any> {
        val transactions = transactionRepository.findByTimestampBetween(startDate, endDate)

        val totalTransactions = transactions.size
        val receiptTransactions = transactions.filter { it.transactionType == TransactionType.RECEIPT }
        val issueTransactions = transactions.filter { it.transactionType == TransactionType.ISSUE }
        val transferTransactions = transactions.filter { it.transactionType == TransactionType.TRANSFER }

        return mapOf(
            "reportPeriod" to mapOf(
                "startDate" to startDate,
                "endDate" to endDate
            ),
            "reportGeneratedAt" to LocalDateTime.now(),
            "totalTransactions" to totalTransactions,
            "receiptCount" to receiptTransactions.size,
            "issueCount" to issueTransactions.size,
            "transferCount" to transferTransactions.size,
            "totalReceiptQuantity" to receiptTransactions.sumOf { it.quantity },
            "totalIssueQuantity" to issueTransactions.sumOf { it.quantity },
            "totalTransferQuantity" to transferTransactions.sumOf { it.quantity },
            "transactionsByType" to mapOf(
                "RECEIPT" to receiptTransactions.size,
                "ISSUE" to issueTransactions.size,
                "TRANSFER" to transferTransactions.size
            ),
            "transactions" to transactions.map { transaction ->
                mapOf(
                    "id" to transaction.id,
                    "transactionType" to transaction.transactionType.name,
                    "productCode" to transaction.product.code,
                    "productName" to transaction.product.name,
                    "quantity" to transaction.quantity,
                    "sourceWarehouse" to transaction.sourceWarehouse?.name,
                    "destinationWarehouse" to transaction.destinationWarehouse?.name,
                    "timestamp" to transaction.timestamp,
                    "description" to transaction.description,
                    "userId" to transaction.userId,
                    "referenceNumber" to transaction.referenceNumber,
                    "directionDescription" to transaction.getDirectionDescription()
                )
            }
        )
    }

    fun generateSystemSummaryReport(): Map<String, Any> {
        val totalWarehouses = warehouseRepository.count()
        val totalInventoryItems = inventoryItemRepository.count()
        val totalTransactions = transactionRepository.count()
        val totalCapacity = warehouseRepository.getTotalCapacity()

        val recentTransactions = transactionRepository.findRecentTransactions(10)
        val lowStockItems = inventoryItemRepository.findItemsBelowMinimumLevel()
        val excessStockItems = inventoryItemRepository.findItemsAboveMaximumLevel()

        // Calculate total quantities by transaction type
        val totalReceiptQuantity = transactionRepository.getTotalQuantityByTransactionType(TransactionType.RECEIPT)
        val totalIssueQuantity = transactionRepository.getTotalQuantityByTransactionType(TransactionType.ISSUE)
        val totalTransferQuantity = transactionRepository.getTotalQuantityByTransactionType(TransactionType.TRANSFER)

        return mapOf(
            "reportGeneratedAt" to LocalDateTime.now(),
            "systemOverview" to mapOf(
                "totalWarehouses" to totalWarehouses,
                "totalInventoryItems" to totalInventoryItems,
                "totalTransactions" to totalTransactions,
                "totalWarehouseCapacity" to totalCapacity,
                "lowStockItemsCount" to lowStockItems.size,
                "excessStockItemsCount" to excessStockItems.size
            ),
            "transactionSummary" to mapOf(
                "totalReceiptQuantity" to totalReceiptQuantity,
                "totalIssueQuantity" to totalIssueQuantity,
                "totalTransferQuantity" to totalTransferQuantity
            ),
            "recentActivity" to recentTransactions.map { transaction ->
                mapOf(
                    "id" to transaction.id,
                    "transactionType" to transaction.transactionType.name,
                    "productCode" to transaction.product.code,
                    "productName" to transaction.product.name,
                    "quantity" to transaction.quantity,
                    "sourceWarehouse" to transaction.sourceWarehouse?.name,
                    "destinationWarehouse" to transaction.destinationWarehouse?.name,
                    "timestamp" to transaction.timestamp,
                    "directionDescription" to transaction.getDirectionDescription()
                )
            },
            "lowStockAlerts" to lowStockItems.map { item ->
                mapOf(
                    "productCode" to item.product.code,
                    "productName" to item.product.name,
                    "currentQuantity" to item.quantity,
                    "minimumLevel" to item.minimumLevel,
                    "warehouseName" to item.warehouse.name,
                    "warehouseLocation" to item.warehouse.location
                )
            },
            "excessStockAlerts" to excessStockItems.map { item ->
                mapOf(
                    "productCode" to item.product.code,
                    "productName" to item.product.name,
                    "currentQuantity" to item.quantity,
                    "maximumLevel" to item.maximumLevel,
                    "warehouseName" to item.warehouse.name,
                    "warehouseLocation" to item.warehouse.location
                )
            }
        )
    }
}