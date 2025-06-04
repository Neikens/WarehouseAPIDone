package com.warehouse.api.repository

import com.warehouse.api.model.Warehouse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WarehouseRepository : JpaRepository<Warehouse, Long>
