package com.warehouse.api.service

import com.warehouse.api.exception.ValidationException
import com.warehouse.api.model.Product
import com.warehouse.api.model.Transaction
import com.warehouse.api.model.Warehouse
import org.springframework.stereotype.Service

@Service
class ValidationService {
    
    fun validateProduct(product: Product) {
        if (product.code.isBlank()) {
            throw ValidationException("Product code cannot be empty")
        }
        if (product.description.isBlank()) {
            throw ValidationException("Product description cannot be empty")
        }
        if (product.category.isBlank()) {
            throw ValidationException("Product category cannot be empty")
        }
    }

    fun validateWarehouse(warehouse: Warehouse) {
        if (warehouse.name.isBlank()) {
            throw ValidationException("Warehouse name cannot be empty")
        }
        if (warehouse.location.isBlank()) {
            throw ValidationException("Warehouse location cannot be empty")
        }
        if (warehouse.capacity <= 0) {
            throw ValidationException("Warehouse capacity must be greater than 0")
        }
    }

    fun validateTransaction(transaction: Transaction) {
        if (transaction.quantity.signum() <= 0) {
            throw ValidationException("Transaction quantity must be greater than 0")
        }
        // Add additional transaction validations as needed
    }
}
