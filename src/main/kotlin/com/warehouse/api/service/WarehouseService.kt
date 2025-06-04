package com.warehouse.api.service

import com.warehouse.api.dto.WarehouseDto
import com.warehouse.api.model.Warehouse
import com.warehouse.api.repository.WarehouseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WarehouseService(
    private val warehouseRepository: WarehouseRepository
) {
    @Transactional
    fun createWarehouse(warehouseDto: WarehouseDto): WarehouseDto {
        val warehouse = Warehouse(
            name = warehouseDto.name,
            location = warehouseDto.location,
            capacity = warehouseDto.capacity
        )
        
        val savedWarehouse = warehouseRepository.save(warehouse)
        
        return WarehouseDto(
            id = savedWarehouse.id,
            name = savedWarehouse.name,
            location = savedWarehouse.location,
            capacity = savedWarehouse.capacity
        )
    }

    fun getWarehouse(id: Long): WarehouseDto {
        val warehouse = warehouseRepository.findById(id)
            .orElseThrow { RuntimeException("Warehouse not found") }
            
        return WarehouseDto(
            id = warehouse.id,
            name = warehouse.name,
            location = warehouse.location,
            capacity = warehouse.capacity
        )
    }

    fun getAllWarehouses(): List<WarehouseDto> {
        return warehouseRepository.findAll().map { warehouse ->
            WarehouseDto(
                id = warehouse.id,
                name = warehouse.name,
                location = warehouse.location,
                capacity = warehouse.capacity
            )
        }
    }
}
