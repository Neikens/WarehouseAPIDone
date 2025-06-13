package com.warehouse.api.service

import com.warehouse.api.dto.WarehouseDto
import com.warehouse.api.model.Warehouse
import com.warehouse.api.repository.WarehouseRepository
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.exception.ValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Noliktavu pārvaldības serviss
 * Nodrošina noliktavu CRUD operācijas un saistīto funkcionalitāti
 */
@Service
class WarehouseService(
    private val warehouseRepository: WarehouseRepository,
    private val inventoryItemRepository: InventoryItemRepository,
    private val validationService: ValidationService
) {

    /**
     * Izveido jaunu noliktavu
     * @param warehouseDto noliktavas dati
     * @return izveidotās noliktavas DTO
     */
    @Transactional
    fun createWarehouse(warehouseDto: WarehouseDto): WarehouseDto {
        // Pārbauda, vai noliktava ar tādu nosaukumu jau eksistē
        if (warehouseRepository.existsByName(warehouseDto.name)) {
            throw ValidationException("Noliktava ar nosaukumu '${warehouseDto.name}' jau eksistē")
        }

        val warehouse = Warehouse(
            name = warehouseDto.name,
            location = warehouseDto.location,
            capacity = warehouseDto.capacity,
            description = warehouseDto.description
        )

        // Validē noliktavas datus
        validationService.validateWarehouse(warehouse)

        val savedWarehouse = warehouseRepository.save(warehouse)

        return convertToDto(savedWarehouse)
    }

    /**
     * Atgriež noliktavu pēc ID
     * @param id noliktavas ID
     * @return noliktavas DTO
     */
    fun getWarehouse(id: Long): WarehouseDto {
        validationService.validateId(id, "Noliktavas")

        val warehouse = warehouseRepository.findById(id)
            .orElseThrow { NoSuchElementException("Noliktava ar ID $id nav atrasta") }

        return convertToDto(warehouse)
    }

    /**
     * Atgriež visas noliktavas
     * @return noliktavu saraksts
     */
    fun getAllWarehouses(): List<WarehouseDto> {
        return warehouseRepository.findAll().map { warehouse ->
            convertToDto(warehouse)
        }
    }

    /**
     * Atgriež aktīvās noliktavas (ar kapacitāti > 0)
     */
    fun getActiveWarehouses(): List<WarehouseDto> {
        return warehouseRepository.findByCapacityGreaterThan(0.0).map { warehouse ->
            convertToDto(warehouse)
        }
    }

    /**
     * Atjaunina noliktavas datus
     * @param id noliktavas ID
     * @param warehouseDto jaunie noliktavas dati
     * @return atjauninātās noliktavas DTO
     */
    @Transactional
    fun updateWarehouse(id: Long, warehouseDto: WarehouseDto): WarehouseDto {
        validationService.validateId(id, "Noliktavas")

        val existingWarehouse = warehouseRepository.findById(id)
            .orElseThrow { NoSuchElementException("Noliktava ar ID $id nav atrasta") }

        // Pārbauda, vai nosaukums nav aizņemts (izņemot pašreizējo noliktavu)
        if (warehouseDto.name != existingWarehouse.name &&
            warehouseRepository.existsByName(warehouseDto.name)) {
            throw ValidationException("Noliktava ar nosaukumu '${warehouseDto.name}' jau eksistē")
        }

        val updatedWarehouse = existingWarehouse.copy(
            name = warehouseDto.name,
            location = warehouseDto.location,
            capacity = warehouseDto.capacity,
            description = warehouseDto.description
        )

        // Validē atjauninātās noliktavas datus
        validationService.validateWarehouse(updatedWarehouse)

        val savedWarehouse = warehouseRepository.save(updatedWarehouse)

        return convertToDto(savedWarehouse)
    }

    /**
     * Dzēš noliktavu (tikai ja tajā nav krājumu)
     * @param id noliktavas ID
     */
    @Transactional
    fun deleteWarehouse(id: Long) {
        validationService.validateId(id, "Noliktavas")

        val warehouse = warehouseRepository.findById(id)
            .orElseThrow { NoSuchElementException("Noliktava ar ID $id nav atrasta") }

        // Pārbauda, vai noliktavā ir krājumi
        val inventoryCount = inventoryItemRepository.countByWarehouseId(id)
        if (inventoryCount > 0) {
            throw ValidationException("Nevar dzēst noliktavu, kurā ir krājumi. Krājumu skaits: $inventoryCount")
        }

        warehouseRepository.delete(warehouse)
    }

    /**
     * Atgriež noliktavas krājumu kopsavilkumu
     * @param id noliktavas ID
     * @return krājumu statistika
     */
    fun getWarehouseInventorySummary(id: Long): Map<String, Any> {
        validationService.validateId(id, "Noliktavas")

        val warehouse = warehouseRepository.findById(id)
            .orElseThrow { NoSuchElementException("Noliktava ar ID $id nav atrasta") }

        val inventoryItems = inventoryItemRepository.findByWarehouseId(id)

        val totalValue = inventoryItems.sumOf { item ->
            item.quantity * item.product.price
        }

        val lowStockItems = inventoryItems.filter { it.isBelowMinimumLevel() }
        val excessStockItems = inventoryItems.filter { it.isAboveMaximumLevel() }

        return mapOf(
            "warehouseId" to id,
            "warehouseName" to warehouse.name,
            "totalProducts" to inventoryItems.size,
            "totalValue" to totalValue,
            "lowStockCount" to lowStockItems.size,
            "excessStockCount" to excessStockItems.size,
            "utilizationPercentage" to calculateUtilization(warehouse, inventoryItems.size)
        )
    }

    /**
     * Meklē noliktavas pēc nosaukuma vai atrašanās vietas
     */
    fun searchWarehouses(searchTerm: String): List<WarehouseDto> {
        if (searchTerm.isBlank()) {
            return getAllWarehouses()
        }

        // Izsauc divas atsevišķas repozitorija metodes
        val byName = warehouseRepository.findByNameContainingIgnoreCase(searchTerm)
        val byLocation = warehouseRepository.findByLocationContainingIgnoreCase(searchTerm)

        // Apvieno rezultātus un noņemam duplikātus
        val combinedResults = (byName + byLocation).distinctBy { it.id }

        return combinedResults.map { convertToDto(it) }
    }

    /**
     * Konvertē Warehouse entītiju uz WarehouseDto
     */
    private fun convertToDto(warehouse: Warehouse): WarehouseDto {
        return WarehouseDto(
            id = warehouse.id,
            name = warehouse.name,
            location = warehouse.location,
            capacity = warehouse.capacity,
            description = warehouse.description,
            createdAt = warehouse.createdAt
        )
    }

    /**
     * Aprēķina noliktavas izmantošanas procentu
     */
    private fun calculateUtilization(warehouse: Warehouse, productCount: Int): Double {
        // Vienkāršots aprēķins - produktu skaits pret kapacitāti
        // Reālā sistēmā būtu jāņem vērā produktu tilpums
        return if (warehouse.capacity > 0) {
            (productCount / warehouse.capacity * 100).coerceAtMost(100.0)
        } else {
            0.0
        }
    }
}