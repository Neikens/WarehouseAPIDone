package com.warehouse.api.controller

import com.warehouse.api.dto.WarehouseDto
import com.warehouse.api.service.WarehouseService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/warehouses")
class WarehouseController(
    private val warehouseService: WarehouseService
) {

    @PostMapping
    fun createWarehouse(@Valid @RequestBody warehouseDto: WarehouseDto): ResponseEntity<WarehouseDto> {
        val createdWarehouse = warehouseService.createWarehouse(warehouseDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWarehouse)
    }

    @GetMapping("/{id}")
    fun getWarehouse(@PathVariable id: Long): ResponseEntity<WarehouseDto> {
        val warehouse = warehouseService.getWarehouse(id)
        return ResponseEntity.ok(warehouse)
    }

    @GetMapping
    fun getAllWarehouses(): ResponseEntity<List<WarehouseDto>> {
        val warehouses = warehouseService.getAllWarehouses()
        return ResponseEntity.ok(warehouses)
    }
}
