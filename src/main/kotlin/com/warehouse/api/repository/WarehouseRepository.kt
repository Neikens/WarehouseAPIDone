package com.warehouse.api.repository

import com.warehouse.api.model.Warehouse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Noliktavu repozitorijs
 * Nodrošina datu piekļuves operācijas noliktavām
 */
@Repository
interface WarehouseRepository : JpaRepository<Warehouse, Long> {

    /**
     * Atrod noliktavu pēc nosaukuma
     */
    fun findByName(name: String): Warehouse?

    /**
     * Atrod noliktavas pēc atrašanās vietas
     */
    fun findByLocation(location: String): List<Warehouse>

    /**
     * Meklē noliktavas pēc nosaukuma (daļēja atbilstība, reģistrnejutīga)
     */
    @Query("SELECT w FROM Warehouse w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameContainingIgnoreCase(@Param("name") name: String): List<Warehouse>

    /**
     * Meklē noliktavas pēc atrašanās vietas (daļēja atbilstība, reģistrnejutīga)
     */
    @Query("SELECT w FROM Warehouse w WHERE LOWER(w.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    fun findByLocationContainingIgnoreCase(@Param("location") location: String): List<Warehouse>

    /**
     * Atrod noliktavas ar kapacitāti lielāku par norādīto vērtību
     */
    fun findByCapacityGreaterThan(capacity: Double): List<Warehouse>

    /**
     * Atrod noliktavas ar kapacitāti mazāku par norādīto vērtību
     */
    fun findByCapacityLessThan(capacity: Double): List<Warehouse>

    /**
     * Atrod noliktavas ar kapacitāti noteiktā diapazonā
     */
    fun findByCapacityBetween(minCapacity: Double, maxCapacity: Double): List<Warehouse>

    /**
     * Pārbauda, vai noliktava ar konkrētu nosaukumu jau eksistē
     */
    fun existsByName(name: String): Boolean

    /**
     * Atrod visas unikālās atrašanās vietas
     */
    @Query("SELECT DISTINCT w.location FROM Warehouse w ORDER BY w.location")
    fun findAllLocations(): List<String>

    /**
     * Aprēķina kopējo noliktavu kapacitāti
     */
    @Query("SELECT COALESCE(SUM(w.capacity), 0) FROM Warehouse w")
    fun getTotalCapacity(): Double

    /**
     * Atrod noliktavas, sakārtotas pēc kapacitātes dilstošā secībā
     */
    fun findAllByOrderByCapacityDesc(): List<Warehouse>

    /**
     * Atrod noliktavas, sakārtotas pēc nosaukuma
     */
    fun findAllByOrderByNameAsc(): List<Warehouse>


    fun findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(
        name: String,
        location: String
    ): List<Warehouse>
}