package com.warehouse.api.controller

import com.warehouse.api.model.InventoryItem
import com.warehouse.api.service.InventoryService
import com.warehouse.api.service.InventoryTrackingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Datu klases pieprasījumu apstrādei
 */
data class QuantityRequest(val quantity: BigDecimal)

data class CreateInventoryRequest(
    val productId: Long,
    val warehouseId: Long,
    val quantity: BigDecimal
)

/**
 * Krājumu pārvaldības kontrolleris
 * Nodrošina CRUD operācijas krājumu vienībām un to sekošanu
 */
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Krājumu Pārvaldība", description = "Krājumu uzskaites un pārvaldības endpoint'i")
class InventoryController(
    private val inventoryService: InventoryService,
    private val inventoryTrackingService: InventoryTrackingService
) {

    /**
     * Atgriež visus krājumu ierakstus sistēmā
     */
    @GetMapping
    @Operation(summary = "Iegūst visus krājumu ierakstus")
    fun getAllInventory(): ResponseEntity<List<InventoryItem>> =
        ResponseEntity.ok(inventoryService.getAllInventoryItems())

    /**
     * Atgriež konkrētu krājumu ierakstu pēc ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Iegūst krājumu ierakstu pēc ID")
    fun getInventoryItem(@PathVariable id: Long): ResponseEntity<InventoryItem> =
        ResponseEntity.ok(inventoryService.getInventoryItemById(id))

    /**
     * Atjaunina krājumu daudzumu konkrētam produktam konkrētā noliktavā
     * Šī ir galvenā metode krājumu daudzuma maiņai
     */
    @PutMapping("/{productId}/{warehouseId}")
    @Operation(summary = "Atjaunina krājumu daudzumu")
    fun updateInventoryQuantity(
        @PathVariable productId: Long,
        @PathVariable warehouseId: Long,
        @RequestBody request: QuantityRequest
    ): ResponseEntity<InventoryItem> {
        // Ļaujam GlobalExceptionHandler apstrādāt visas kļūdas
        val result = inventoryService.updateInventoryQuantity(productId, warehouseId, request.quantity)
        return ResponseEntity.ok(result)
    }

    /**
     * Pielāgo krājumu daudzumu (pievieno vai atņem no esošā daudzuma)
     */
    @PostMapping("/{productId}/{warehouseId}/adjust")
    @Operation(summary = "Pielāgo krājumu daudzumu (relatīva izmaiņa)")
    fun adjustInventory(
        @PathVariable productId: Long,
        @PathVariable warehouseId: Long,
        @RequestParam adjustment: BigDecimal
    ): ResponseEntity<InventoryItem> {
        // Ļaujam GlobalExceptionHandler apstrādāt visas kļūdas
        val result = inventoryService.adjustInventory(productId, warehouseId, adjustment)
        return ResponseEntity.ok(result)
    }

    /**
     * Atgriež visus krājumu ierakstus konkrētai noliktavai
     */
    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Iegūst krājumus pēc noliktavas")
    fun getInventoryByWarehouse(@PathVariable warehouseId: Long): ResponseEntity<List<InventoryItem>> =
        ResponseEntity.ok(inventoryService.getInventoryByWarehouse(warehouseId))

    /**
     * Atgriež visus krājumu ierakstus konkrētam produktam
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Iegūst krājumus pēc produkta")
    fun getInventoryByProduct(@PathVariable productId: Long): ResponseEntity<List<InventoryItem>> =
        ResponseEntity.ok(inventoryService.getInventoryByProduct(productId))

    /**
     * Atgriež produktus ar zemu krājumu līmeni
     * Svarīga funkcija krājumu papildināšanas plānošanai
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Iegūst produktus ar zemu krājumu līmeni")
    fun getLowStockItems(@RequestParam threshold: BigDecimal): ResponseEntity<List<InventoryItem>> =
        ResponseEntity.ok(inventoryTrackingService.checkLowStock(threshold))

    /**
     * Ģenerē detalizētu krājumu atskaiti konkrētai noliktavai
     */
    @GetMapping("/report/{warehouseId}")
    @Operation(summary = "Ģenerē krājumu atskaiti noliktavai")
    fun generateWarehouseReport(@PathVariable warehouseId: Long): ResponseEntity<Map<String, Any>> =
        ResponseEntity.ok(inventoryTrackingService.generateInventoryReport(warehouseId))

    /**
     * Izveido jaunu krājumu ierakstu
     */
    @PostMapping
    @Operation(summary = "Izveido jaunu krājumu ierakstu")
    fun createInventoryItem(@RequestBody request: CreateInventoryRequest): ResponseEntity<InventoryItem> {
        // Ļaujam GlobalExceptionHandler apstrādāt visas kļūdas
        val item = inventoryService.createInventoryItem(
            request.productId,
            request.warehouseId,
            request.quantity
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(item)
    }
}