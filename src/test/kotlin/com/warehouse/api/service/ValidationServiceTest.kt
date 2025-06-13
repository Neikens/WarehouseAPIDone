package com.warehouse.api.service

import com.warehouse.api.exception.ValidationException
import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import com.warehouse.api.model.Transaction
import com.warehouse.api.model.TransactionType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ValidationServiceTest {

    private lateinit var validationService: ValidationService

    @BeforeEach
    fun setUp() {
        validationService = ValidationService()
    }

    @Test
    fun `should validate valid warehouse`() {
        // Given
        val warehouse = Warehouse(
            name = "Valid Warehouse",
            location = "Valid Location",
            capacity = 1000.0
        )

        // When & Then
        assertDoesNotThrow {
            validationService.validateWarehouse(warehouse)
        }
    }

    @Test
    fun `should throw exception for warehouse with empty name`() {
        // Given
        val warehouse = Warehouse(
            name = "",
            location = "Valid Location",
            capacity = 1000.0
        )

        // When & Then
        assertThrows<ValidationException> {
            validationService.validateWarehouse(warehouse)
        }
    }

    @Test
    fun `should validate valid product`() {
        // Given
        val product = Product(
            code = "VALID-001",
            name = "Valid Product",
            description = "Valid Description",
            category = "Valid Category",
            price = BigDecimal("10.00")
        )

        // When & Then
        assertDoesNotThrow {
            validationService.validateProduct(product)
        }
    }

    @Test
    fun `should throw exception for product with empty code`() {
        // Given
        val product = Product(
            code = "",
            name = "Valid Product",
            description = "Valid Description",
            category = "Valid Category",
            price = BigDecimal("10.00")
        )

        // When & Then
        assertThrows<ValidationException> {
            validationService.validateProduct(product)
        }
    }

    @Test
    fun `should throw exception for product with negative price`() {
        // Given
        val product = Product(
            code = "VALID-001",
            name = "Valid Product",
            description = "Valid Description",
            category = "Valid Category",
            price = BigDecimal("-10.00")
        )

        // When & Then
        assertThrows<ValidationException> {
            validationService.validateProduct(product)
        }
    }

    @Test
    fun `should validate valid transaction`() {
        // Given
        val warehouse = Warehouse(
            name = "Test Warehouse",
            location = "Test Location",
            capacity = 1000.0
        )
        val product = Product(
            code = "TEST-001",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = BigDecimal("10.00")
        )
        val transaction = Transaction(
            transactionType = TransactionType.RECEIPT,
            product = product,
            sourceWarehouse = null,
            destinationWarehouse = warehouse,
            quantity = BigDecimal("10.00")
        )

        // When & Then
        assertDoesNotThrow {
            validationService.validateTransaction(transaction)
        }
    }

    @Test
    fun `should throw exception for transaction with negative quantity`() {
        // Given
        val product = Product(
            code = "TEST-001",
            name = "Test Product",
            description = "Test Description",
            category = "Test Category",
            price = BigDecimal("10.00")
        )
        val transaction = Transaction(
            transactionType = TransactionType.RECEIPT,
            product = product,
            sourceWarehouse = null,
            destinationWarehouse = null,
            quantity = BigDecimal("-10.00")
        )

        // When & Then
        assertThrows<ValidationException> {
            validationService.validateTransaction(transaction)
        }
    }

    @Test
    fun `should validate positive quantity`() {
        // When & Then
        assertDoesNotThrow {
            validationService.validateQuantity(BigDecimal("10.00"), "TEST_OPERATION")
        }
    }

    @Test
    fun `should throw exception for negative quantity`() {
        // When & Then
        assertThrows<ValidationException> {
            validationService.validateQuantity(BigDecimal("-10.00"), "TEST_OPERATION")
        }
    }

    @Test
    fun `should throw exception for zero quantity`() {
        // When & Then
        assertThrows<ValidationException> {
            validationService.validateQuantity(BigDecimal.ZERO, "TEST_OPERATION")
        }
    }

    @Test
    fun `should validate valid ID`() {
        // When & Then
        assertDoesNotThrow {
            validationService.validateId(1L, "Product")
        }
    }

    @Test
    fun `should throw exception for null ID`() {
        // When & Then
        assertThrows<ValidationException> {
            validationService.validateId(null, "Product")
        }
    }

    @Test
    fun `should throw exception for negative ID`() {
        // When & Then
        assertThrows<ValidationException> {
            validationService.validateId(-1L, "Product")
        }
    }
}