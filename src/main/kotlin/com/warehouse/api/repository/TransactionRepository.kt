package com.warehouse.api.repository

import com.warehouse.api.model.Transaction
import com.warehouse.api.model.TransactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Transakciju repozitorijs
 * Nodrošina datu piekļuves operācijas transakcijām
 */
@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {

    /**
     * Atrod transakcijas pēc produkta ID
     */
    fun findByProductId(productId: Long): List<Transaction>

    /**
     * Atrod transakcijas pēc avota noliktavas ID
     */
    fun findBySourceWarehouseId(sourceWarehouseId: Long): List<Transaction>

    /**
     * Atrod transakcijas pēc galamērķa noliktavas ID
     */
    fun findByDestinationWarehouseId(destinationWarehouseId: Long): List<Transaction>

    /**
     * Atrod transakcijas pēc tipa
     */
    fun findByTransactionType(transactionType: TransactionType): List<Transaction>

    /**
     * Atrod transakcijas noteiktā laika periodā
     */
    fun findByTimestampBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Transaction>

    /**
     * Atrod transakcijas pēc lietotāja ID
     */
    fun findByUserId(userId: String): List<Transaction>

    /**
     * Atrod transakcijas pēc atsauces numura
     */
    fun findByReferenceNumber(referenceNumber: String): List<Transaction>

    /**
     * Atrod visas transakcijas konkrētai noliktavai (gan ienākošās, gan izejošās)
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceWarehouse.id = :warehouseId OR t.destinationWarehouse.id = :warehouseId")
    fun findAllByWarehouseId(@Param("warehouseId") warehouseId: Long): List<Transaction>

    /**
     * Atrod transakcijas konkrētam produktam noteiktā laika periodā
     */
    @Query("SELECT t FROM Transaction t WHERE t.product.id = :productId AND t.timestamp BETWEEN :startDate AND :endDate")
    fun findByProductIdAndTimestampBetween(
        @Param("productId") productId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Transaction>

    /**
     * Aprēķina kopējo transakciju daudzumu pēc tipa
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transaction t WHERE t.transactionType = :transactionType")
    fun getTotalQuantityByTransactionType(@Param("transactionType") transactionType: TransactionType): java.math.BigDecimal

    /**
     * Atrod pēdējās N transakcijas, sakārtotas pēc laika
     */
    @Query("SELECT t FROM Transaction t ORDER BY t.timestamp DESC LIMIT :limit")
    fun findRecentTransactions(@Param("limit") limit: Int = 10): List<Transaction>

    /**
     * Aprēķina transakciju skaitu konkrētā dienā
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE DATE(t.timestamp) = DATE(:date)")
    fun countTransactionsByDate(@Param("date") date: LocalDateTime): Long
}