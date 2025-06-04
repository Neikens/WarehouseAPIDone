package com.warehouse.api.service

import com.warehouse.api.repository.TransactionRepository
import com.warehouse.api.repository.ProductRepository
import com.warehouse.api.repository.WarehouseRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReportService(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
    private val warehouseRepository: WarehouseRepository
) {
    fun generateInventoryReport(warehouseId: Long): Map<String, Any> {
        val warehouse = warehouseRepository.findById(warehouseId).orElseThrow()
        val products = productRepository.findAll()
        val transactions = transactionRepository.findAll()
        
        return mapOf(
            "warehouseId" to warehouseId,
            "warehouseName" to warehouse.name,
            "totalProducts" to products.size,
            "totalTransactions" to transactions.size,
            "generatedAt" to LocalDateTime.now()
        )
    }

    fun generateTransactionReport(startDate: LocalDateTime, endDate: LocalDateTime): Map<String, Any> {
        val transactions = transactionRepository.findAll()
        
        return mapOf(
            "startDate" to startDate,
            "endDate" to endDate,
            "totalTransactions" to transactions.size,
            "generatedAt" to LocalDateTime.now()
        )
    }
}
