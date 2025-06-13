package com.warehouse.api.repository

import com.warehouse.api.model.InventoryItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

/**
 * Krājumu vienību repozitorijs
 * Nodrošina datu piekļuves operācijas krājumu vienībām
 */
@Repository
interface InventoryItemRepository : JpaRepository<InventoryItem, Long> {

    /**
     * Atrod krājumu vienību pēc produkta un noliktavas ID
     * Izmanto standarta Spring Data JPA metodi
     */
    fun findByProductIdAndWarehouseId(productId: Long, warehouseId: Long): InventoryItem?

    /**
     * Alternatīva metode krājumu vienības atrašanai
     * Izmanto eksplicītu JPQL vaicājumu lielākai uzticamībai
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.product.id = :productId AND i.warehouse.id = :warehouseId")
    fun findByProductAndWarehouseIds(
        @Param("productId") productId: Long,
        @Param("warehouseId") warehouseId: Long
    ): InventoryItem?

    /**
     * Atrod visas krājumu vienības konkrētā noliktavā
     */
    fun findByWarehouseId(warehouseId: Long): List<InventoryItem>

    /**
     * Atrod visas krājumu vienības konkrētam produktam visos noliktavās
     */
    fun findByProductId(productId: Long): List<InventoryItem>

    /**
     * Atrod krājumu vienības, kuru daudzums ir zem norādītā sliekšņa
     * Izmanto, lai identificētu zemu krājumu līmeni
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= :threshold")
    fun findItemsBelowThreshold(@Param("threshold") threshold: BigDecimal): List<InventoryItem>

    /**
     * Atrod krājumu vienības, kuru daudzums ir zem to minimālā līmeņa
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.minimumLevel IS NOT NULL AND i.quantity < i.minimumLevel")
    fun findItemsBelowMinimumLevel(): List<InventoryItem>

    /**
     * Atrod krājumu vienības, kuru daudzums pārsniedz maksimālo līmeni
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.maximumLevel IS NOT NULL AND i.quantity > i.maximumLevel")
    fun findItemsAboveMaximumLevel(): List<InventoryItem>

    /**
     * Aprēķina kopējo krājumu daudzumu konkrētam produktam visos noliktavās
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InventoryItem i WHERE i.product.id = :productId")
    fun getTotalQuantityByProductId(@Param("productId") productId: Long): BigDecimal

    /**
     * Skaita krājumu vienības konkrētā noliktavā
     */
    fun countByWarehouseId(warehouseId: Long): Long

    /**
     * Aprēķina kopējo krājumu daudzumu konkrētā noliktavā
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InventoryItem i WHERE i.warehouse.id = :warehouseId")
    fun getTotalQuantityByWarehouseId(@Param("warehouseId") warehouseId: Long): BigDecimal
}