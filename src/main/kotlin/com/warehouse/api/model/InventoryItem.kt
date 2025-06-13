package com.warehouse.api.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Krājumu vienības entītija
 * Reprezentē konkrēta produkta daudzumu konkrētā noliktavā
 */
@Entity
@Table(
    name = "inventory_items",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_product_warehouse",
            columnNames = ["product_id", "warehouse_id"]
        )
    ]
)
data class InventoryItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /**
     * Saite uz produktu
     * EAGER loading nodrošina, ka produkta dati vienmēr ir pieejami
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference("inventory-product")
    val product: Product,

    /**
     * Saite uz noliktavu
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference("inventory-warehouse")
    val warehouse: Warehouse,

    /**
     * Krājumu daudzums
     * Izmanto BigDecimal precīziem aprēķiniem
     */
    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    val quantity: BigDecimal,

    /**
     * Ieraksta izveides laiks
     */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Pēdējās atjaunināšanas laiks
     */
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Minimālais krājumu līmenis (brīdinājumiem)
     */
    @Column(name = "minimum_level", precision = 10, scale = 2)
    val minimumLevel: BigDecimal? = null,

    /**
     * Maksimālais krājumu līmenis
     */
    @Column(name = "maximum_level", precision = 10, scale = 2)
    val maximumLevel: BigDecimal? = null
) {
    /**
     * Pārbauda, vai krājumi ir zem minimālā līmeņa
     */
    fun isBelowMinimumLevel(): Boolean {
        return minimumLevel?.let { quantity < it } ?: false
    }

    /**
     * Pārbauda, vai krājumi pārsniedz maksimālo līmeni
     */
    fun isAboveMaximumLevel(): Boolean {
        return maximumLevel?.let { quantity > it } ?: false
    }

    /**
     * Aprēķina krājumu statusu
     */
    fun getStockStatus(): StockStatus {
        return when {
            isBelowMinimumLevel() -> StockStatus.LOW
            isAboveMaximumLevel() -> StockStatus.EXCESS
            else -> StockStatus.NORMAL
        }
    }
}

/**
 * Krājumu statusa enumerācija
 */
enum class StockStatus {
    LOW,      // Zemi krājumi
    NORMAL,   // Normāli krājumi
    EXCESS    // Pārmērīgi krājumi
}