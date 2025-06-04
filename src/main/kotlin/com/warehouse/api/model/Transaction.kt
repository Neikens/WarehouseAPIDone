package com.warehouse.api.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Enumerated(EnumType.STRING)
    val transactionType: TransactionType,
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product,
    
    @ManyToOne
    @JoinColumn(name = "source_warehouse_id")
    val sourceWarehouse: Warehouse?,
    
    @ManyToOne
    @JoinColumn(name = "destination_warehouse_id")
    val destinationWarehouse: Warehouse?,
    
    val quantity: BigDecimal,
    
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType {
    RECEIPT, TRANSFER, ISSUE
}
