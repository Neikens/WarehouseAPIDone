package com.warehouse.api.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Produkta entītija
 * Reprezentē produktu ar visām tā īpašībām
 */
@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_product_code", columnList = "code"),
        Index(name = "idx_product_barcode", columnList = "barcode"),
        Index(name = "idx_product_category", columnList = "category")
    ]
)
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /**
     * Unikāls produkta kods
     * Izmanto biznesa loģikā produkta identifikācijai
     */
    @Column(name = "code", length = 50, nullable = false, unique = true)
    @field:NotBlank(message = "Produkta kods ir obligāts")
    @field:Size(min = 1, max = 50, message = "Produkta kodam jābūt no 1 līdz 50 rakstzīmēm")
    val code: String,

    /**
     * Produkta apraksts
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @field:NotBlank(message = "Produkta apraksts ir obligāts")
    @field:Size(max = 1000, message = "Apraksts nevar pārsniegt 1000 rakstzīmes")
    val description: String,

    /**
     * Produkta svītrkods
     */
    @Column(name = "barcode", length = 100)
    @field:Size(max = 100, message = "Svītrkods nevar pārsniegt 100 rakstzīmes")
    val barcode: String? = null,

    /**
     * Produkta kategorija
     */
    @Column(name = "category", length = 100, nullable = false)
    @field:NotBlank(message = "Produkta kategorija ir obligāta")
    @field:Size(max = 100, message = "Kategorija nevar pārsniegt 100 rakstzīmes")
    val category: String,

    /**
     * Produkta nosaukums (var atšķirties no apraksta)
     */
    @Column(name = "name", length = 255, nullable = false)
    @field:NotBlank(message = "Produkta nosaukums ir obligāts")
    @field:Size(max = 255, message = "Nosaukums nevar pārsniegt 255 rakstzīmes")
    val name: String,

    /**
     * Produkta cena
     * Izmanto BigDecimal precīziem naudas aprēķiniem
     */
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    @field:DecimalMin(value = "0.0", message = "Cenai jābūt pozitīvai vai nullei")
    @field:Digits(integer = 8, fraction = 2, message = "Cena var būt maksimums 8 cipari pirms komata un 2 pēc")
    val price: BigDecimal,

    /**
     * Produkta svars (kilogramos)
     */
    @Column(name = "weight", precision = 8, scale = 3)
    @field:DecimalMin(value = "0.0", message = "Svaram jābūt pozitīvam")
    val weight: BigDecimal? = null,

    /**
     * Produkta izmēri (garums x platums x augstums cm)
     */
    @Column(name = "dimensions", length = 50)
    val dimensions: String? = null,

    /**
     * Produkta statuss (aktīvs/neaktīvs)
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
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Krājumu vienības šim produktam
     * Lazy loading ar JSON managed reference
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonManagedReference("inventory-product")
    val inventoryItems: List<InventoryItem> = emptyList()
) {
    /**
     * Pārbauda, vai produkts ir aktīvs un pieejams
     */
    fun isAvailable(): Boolean = isActive

    /**
     * Atgriež produkta pilno nosaukumu (kods + nosaukums)
     */
    fun getFullName(): String = "[$code] $name"

    /**
     * Aprēķina produkta tilpumu (ja ir izmēri)
     */
    fun calculateVolume(): BigDecimal? {
        return dimensions?.let { dim ->
            try {
                val parts = dim.split("x").map { it.trim().toBigDecimal() }
                if (parts.size == 3) {
                    parts[0] * parts[1] * parts[2]
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}