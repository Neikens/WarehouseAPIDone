package com.warehouse.api.controller

import com.warehouse.api.model.Product
import com.warehouse.api.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {

    @GetMapping
    fun getAllProducts(): ResponseEntity<List<Product>> =
        ResponseEntity.ok(productService.getAllProducts())

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<Product> =
        ResponseEntity.ok(productService.getProductById(id))

    @PostMapping
    fun createProduct(@RequestBody product: Product): ResponseEntity<Product> =
        ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(product))

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody product: Product
    ): ResponseEntity<Product> =
        ResponseEntity.ok(productService.updateProduct(id, product))

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Unit> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }
}