package com.warehouse.api.service

import com.warehouse.api.model.Transaction
import com.warehouse.api.model.TransactionType
import com.warehouse.api.repository.TransactionRepository
import com.warehouse.api.repository.ProductRepository
import com.warehouse.api.repository.WarehouseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
    private val warehouseRepository: WarehouseRepository
) {
    @Autowired
    @Lazy
    private lateinit var inventoryService: InventoryService

    fun getAllTransactions(): List<Transaction> = transactionRepository.findAll()

    @Transactional
    fun createTransaction(
        productId: Long,
        sourceWarehouseId: Long?,
        destinationWarehouseId: Long?,
        quantity: BigDecimal,
        type: TransactionType
    ): Transaction {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }

        val sourceWarehouse = sourceWarehouseId?.let {
            warehouseRepository.findById(it)
                .orElseThrow { NoSuchElementException("Source warehouse not found with id: $it") }
        }

        val destinationWarehouse = destinationWarehouseId?.let {
            warehouseRepository.findById(it)
                .orElseThrow { NoSuchElementException("Destination warehouse not found with id: $it") }
        }

        val transaction = Transaction(
            product = product,
            sourceWarehouse = sourceWarehouse,
            destinationWarehouse = destinationWarehouse,
            quantity = quantity,
            transactionType = type
        )

        return transactionRepository.save(transaction)
    }
}
