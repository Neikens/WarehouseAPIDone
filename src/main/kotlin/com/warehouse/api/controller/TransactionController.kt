package com.warehouse.api.controller

import com.warehouse.api.model.Transaction
import com.warehouse.api.model.TransactionType
import com.warehouse.api.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/transactions")
class TransactionController(private val transactionService: TransactionService) {
    
    @GetMapping
    fun getAllTransactions(): List<Transaction> = transactionService.getAllTransactions()
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTransaction(
        @RequestParam productId: Long,
        @RequestParam(required = false) sourceWarehouseId: Long?,
        @RequestParam(required = false) destinationWarehouseId: Long?,
        @RequestParam quantity: BigDecimal,
        @RequestParam type: TransactionType
    ): Transaction = transactionService.createTransaction(
        productId, sourceWarehouseId, destinationWarehouseId, quantity, type
    )
}
