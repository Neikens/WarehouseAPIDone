package com.warehouse.api.model

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Transakcijas entītija
 * Reprezentē krājumu kustību starp noliktavām
 */
@Entity
@Table(
    name = "transactions",
    indexes = [
        Index(name = "idx_transaction_timestamp", columnList = "transaction_timestamp"),
        Index(name = "idx_transaction_product", columnList = "product_id"),
        Index(name = "idx_transaction_type", columnList = "transaction_type")
    ]
)
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /**
     * Transakcijas tips
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @field:NotNull(message = "Transakcijas tips ir obligāts")
    val transactionType: TransactionType,

    /**
     * Produkts, ar kuru veic transakciju
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @field:NotNull(message = "Produkts ir obligāts")
    val product: Product,

    /**
     * Avota noliktava (no kurienes produkts tiek ņemts)
     * Var būt null RECEIPT transakcijām
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_warehouse_id")
    val sourceWarehouse: Warehouse?,

    /**
     * Galamērķa noliktava (kurp produkts tiek nogādāts)
     * Var būt null ISSUE transakcijām
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_warehouse_id")
    val destinationWarehouse: Warehouse?,

    /**
     * Transakcijas daudzums
     */
    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    @field:DecimalMin(value = "0.01", message = "Daudzumam jābūt pozitīvam")
    val quantity: BigDecimal,

    /**
     * Transakcijas laiks
     */
    @Column(name = "transaction_timestamp", nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),

    /**
     * Transakcijas apraksts/komentārs
     */
    @Column(name = "description", length = 500)
    val description: String? = null,

    /**
     * Lietotājs, kurš veica transakciju
     */
    @Column(name = "user_id", length = 50)
    val userId: String? = null,

    /**
     * Atsauces numurs (piemēram, pasūtījuma numurs)
     */
    @Column(name = "reference_number", length = 100)
    val referenceNumber: String? = null
) {
    /**
     * Pārbauda, vai transakcija ir derīga
     */
    fun isValid(): Boolean {
        return when (transactionType) {
            TransactionType.RECEIPT -> destinationWarehouse != null
            TransactionType.ISSUE -> sourceWarehouse != null
            TransactionType.TRANSFER -> sourceWarehouse != null &&
                    destinationWarehouse != null &&
                    sourceWarehouse.id != destinationWarehouse.id
        }
    }

    /**
     * Atgriež transakcijas virzienu kā tekstu
     */
    fun getDirectionDescription(): String {
        return when (transactionType) {
            TransactionType.RECEIPT -> "Saņemšana → ${destinationWarehouse?.name ?: "Nav norādīta"}"
            TransactionType.ISSUE -> "${sourceWarehouse?.name ?: "Nav norādīta"} → Izdošana"
            TransactionType.TRANSFER -> "${sourceWarehouse?.name ?: "Nav norādīta"} → ${destinationWarehouse?.name ?: "Nav norādīta"}"
        }
    }
}

/**
 * Transakciju tipu enumerācija
 */
enum class TransactionType(val description: String) {
    RECEIPT("Saņemšana"),     // Produktu saņemšana noliktavā
    TRANSFER("Pārvietošana"), // Produktu pārvietošana starp noliktavām
    ISSUE("Izdošana")         // Produktu izdošana no noliktavas
}