package com.warehouse.api.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Noliktavas entītija
 * Reprezentē fizisko noliktavu ar tās īpašībām un saistītajām transakcijām
 */
@Entity
@Table(
    name = "warehouses",
    indexes = [
        Index(name = "idx_warehouse_name", columnList = "name"),
        Index(name = "idx_warehouse_location", columnList = "location")
    ]
)
data class Warehouse(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /**
     * Noliktavas nosaukums
     */
    @Column(name = "name", length = 100, nullable = false)
    @field:NotBlank(message = "Noliktavas nosaukums ir obligāts")
    @field:Size(max = 100, message = "Nosaukums nevar pārsniegt 100 rakstzīmes")
    val name: String,

    /**
     * Noliktavas atrašanās vieta
     */
    @Column(name = "location", length = 255, nullable = false)
    @field:NotBlank(message = "Noliktavas atrašanās vieta ir obligāta")
    @field:Size(max = 255, message = "Atrašanās vieta nevar pārsniegt 255 rakstzīmes")
    val location: String,

    /**
     * Noliktavas kapacitāte (kubikmetros vai citās mērvienībās)
     */
    @Column(name = "capacity", nullable = false)
    @field:DecimalMin(value = "0.1", message = "Kapacitātei jābūt pozitīvai")
    val capacity: Double,

    /**
     * Noliktavas apraksts
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @field:Size(max = 1000, message = "Apraksts nevar pārsniegt 1000 rakstzīmes")
    val description: String? = null,

    /**
     * Noliktavas statuss (aktīva/neaktīva)
     */
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    /**
     * Ieraksta izveides laiks
     */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Pēdējās atjaunināšanas laiks
     */
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    /**
     * Izejošās transakcijas (no šīs noliktavas)
     * Lazy loading, lai izvairītos no nevajadzīgas datu ielādes
     */
    @OneToMany(mappedBy = "sourceWarehouse", fetch = FetchType.LAZY)
    val outgoingTransactions: List<Transaction> = emptyList(),

    /**
     * Ienākošās transakcijas (uz šo noliktavu)
     */
    @OneToMany(mappedBy = "destinationWarehouse", fetch = FetchType.LAZY)
    val incomingTransactions: List<Transaction> = emptyList(),

    /**
     * Krājumu vienības šajā noliktavā
     */
    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @JsonManagedReference("inventory-warehouse")
    val inventoryItems: List<InventoryItem> = emptyList()
) {
    /**
     * Atgriež noliktavas pilno nosaukumu ar atrašanās vietu
     */
    fun getFullName(): String = "$name ($location)"

    /**
     * Aprēķina kopējo transakciju skaitu
     */
    fun getTotalTransactionCount(): Int =
        outgoingTransactions.size + incomingTransactions.size
}