package com.warehouse.api.controller

import com.warehouse.api.dto.WarehouseDto
import com.warehouse.api.service.WarehouseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/warehouses")
@Tag(name = "Noliktavu Pārvaldība", description = "Noliktavu CRUD operācijas")
class WarehouseController(
    private val warehouseService: WarehouseService
) {

    @PostMapping
    @Operation(summary = "Izveido jaunu noliktavu")
    fun createWarehouse(@Valid @RequestBody warehouseDto: WarehouseDto): ResponseEntity<WarehouseDto> {
        return try {
            val createdWarehouse = warehouseService.createWarehouse(warehouseDto)
            ResponseEntity.status(HttpStatus.CREATED).body(createdWarehouse)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Iegūst noliktavu pēc ID")
    fun getWarehouse(@PathVariable id: Long): ResponseEntity<WarehouseDto> {
        return try {
            val warehouse = warehouseService.getWarehouse(id)
            ResponseEntity.ok(warehouse)
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    @Operation(summary = "Iegūst visas noliktavas")
    fun getAllWarehouses(): ResponseEntity<List<WarehouseDto>> {
        val warehouses = warehouseService.getAllWarehouses()
        return ResponseEntity.ok(warehouses)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atjaunina noliktavu")
    fun updateWarehouse(
        @PathVariable id: Long,
        @Valid @RequestBody warehouseDto: WarehouseDto
    ): ResponseEntity<WarehouseDto> {
        return try {
            val updatedWarehouse = warehouseService.updateWarehouse(id, warehouseDto)
            ResponseEntity.ok(updatedWarehouse)
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dzēš noliktavu")
    fun deleteWarehouse(@PathVariable id: Long): ResponseEntity<Unit> {
        return try {
            warehouseService.deleteWarehouse(id)
            ResponseEntity.noContent().build()
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }
}