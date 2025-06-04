package com.warehouse.api.service

import com.warehouse.api.exception.ValidationException
import com.warehouse.api.model.Product
import com.warehouse.api.model.Warehouse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ValidationServiceTest {

    private val validationService = ValidationService()

    @Test
    fun `should validate valid warehouse`() {
        val warehouse = Warehouse(
            name = "Valid Warehouse",
            location = "Valid Location",
            capacity = 1000.0
        )
        
        validationService.validateWarehouse(warehouse)
    }

    @Test
    fun `should throw exception for invalid warehouse`() {
        val warehouse = Warehouse(
            name = "",
            location = "",
            capacity = -1.0
        )
        
        assertThrows<ValidationException> {
            validationService.validateWarehouse(warehouse)
        }
    }
}
