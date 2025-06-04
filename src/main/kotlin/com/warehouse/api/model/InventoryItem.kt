package com.warehouse.api.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "inventory_items")
data class InventoryItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    val product: Product,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id")
    val warehouse: Warehouse,

    @Column(precision = 10, scale = 2)
    val quantity: BigDecimal,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
