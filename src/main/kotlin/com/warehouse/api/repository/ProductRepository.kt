package com.warehouse.api.repository

import com.warehouse.api.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Produktu repozitorijs
 * Nodrošina datu piekļuves operācijas produktiem
 */
@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    /**
     * Atrod produktu pēc unikālā koda
     */
    fun findByCode(code: String): Product?

    /**
     * Atrod produktu pēc svītrkoda
     */
    fun findByBarcode(barcode: String): Product?

    /**
     * Atrod visus produktus konkrētā kategorijā
     */
    fun findByCategory(category: String): List<Product>

    /**
     * Atrod aktīvos produktus
     */
    fun findByIsActiveTrue(): List<Product>

    /**
     * Atrod neaktīvos produktus
     */
    fun findByIsActiveFalse(): List<Product>

    /**
     * Meklē produktus pēc nosaukuma (daļēja atbilstība, reģistrnejutīga)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameContainingIgnoreCase(@Param("name") name: String): List<Product>

    /**
     * Meklē produktus pēc apraksta (daļēja atbilstība, reģistrnejutīga)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    fun findByDescriptionContainingIgnoreCase(@Param("description") description: String): List<Product>

    /**
     * Atrod produktus noteiktā cenu diapazonā
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    fun findByPriceBetween(
        @Param("minPrice") minPrice: java.math.BigDecimal,
        @Param("maxPrice") maxPrice: java.math.BigDecimal
    ): List<Product>

    /**
     * Pārbauda, vai produkts ar konkrētu kodu jau eksistē
     */
    fun existsByCode(code: String): Boolean

    /**
     * Pārbauda, vai produkts ar konkrētu svītrkodu jau eksistē
     */
    fun existsByBarcode(barcode: String): Boolean

    /**
     * Atrod visas unikālās kategorijas
     */
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    fun findAllCategories(): List<String>
}