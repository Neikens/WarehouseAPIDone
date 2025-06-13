package com.warehouse.api.controller

import com.warehouse.api.model.Product
import com.warehouse.api.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Produktu pārvaldības kontrolleris
 * Nodrošina CRUD operācijas produktiem
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Produktu Pārvaldība", description = "Produktu CRUD operācijas")
class ProductController(private val productService: ProductService) {

    /**
     * Atgriež visus produktus sistēmā
     */
    @GetMapping
    @Operation(summary = "Iegūst visus produktus")
    fun getAllProducts(): ResponseEntity<List<Product>> =
        ResponseEntity.ok(productService.getAllProducts())

    /**
     * Atgriež konkrētu produktu pēc ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Iegūst produktu pēc ID")
    fun getProductById(@PathVariable id: Long): ResponseEntity<Product> {
        return try {
            ResponseEntity.ok(productService.getProductById(id))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Meklē produktu pēc koda
     * Produkta kods ir unikāls identifikators
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Iegūst produktu pēc koda")
    fun getProductByCode(@PathVariable code: String): ResponseEntity<Product> {
        val product = productService.getProductByCode(code)
        return if (product != null) {
            ResponseEntity.ok(product)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Izveido jaunu produktu
     * @Valid anotācija nodrošina automātisku validāciju
     */
    @PostMapping
    @Operation(summary = "Izveido jaunu produktu")
    fun createProduct(@Valid @RequestBody product: Product): ResponseEntity<Product> {
        return try {
            val createdProduct = productService.createProduct(product)
            ResponseEntity.status(HttpStatus.CREATED).body(createdProduct)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Atjaunina esošu produktu
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atjaunina produktu")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody product: Product
    ): ResponseEntity<Product> {
        return try {
            val updatedProduct = productService.updateProduct(id, product)
            ResponseEntity.ok(updatedProduct)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Dzēš produktu pēc ID
     * UZMANĪBU: Dzēšana var ietekmēt saistītos krājumu ierakstus
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Dzēš produktu")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Unit> {
        return try {
            productService.deleteProduct(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}