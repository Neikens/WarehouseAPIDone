package com.warehouse.api.repository

import com.warehouse.api.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByCode(code: String): Product?
}
