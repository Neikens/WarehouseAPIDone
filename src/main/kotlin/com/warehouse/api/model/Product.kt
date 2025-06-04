package com.warehouse.api.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(length = 255)
    val code: String,

    @Column(length = 255)
    val description: String,

    @Column(length = 255)
    val barcode: String,

    @Column(length = 255)
    val category: String,

    @Column(length = 255)
    val name: String,

    @Column(precision = 38, scale = 2)
    val price: BigDecimal
)