package com.warehouse.api.model

import jakarta.persistence.*

@Entity
@Table(name = "warehouses")
data class Warehouse(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val location: String,

    @Column(nullable = false)
    val capacity: Double,

    @OneToMany(mappedBy = "sourceWarehouse")
    val outgoingTransactions: List<Transaction> = emptyList(),

    @OneToMany(mappedBy = "destinationWarehouse")
    val incomingTransactions: List<Transaction> = emptyList()
)
