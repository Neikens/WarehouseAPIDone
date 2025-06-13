package com.warehouse.api.service

import com.warehouse.api.model.Product
import com.warehouse.api.repository.ProductRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Produktu pārvaldības serviss
 * Nodrošina produktu CRUD operācijas un biznesa loģiku
 */
@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val auditService: AuditService,
    private val metricsService: MetricsService,
    private val validationService: ValidationService
) {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    /**
     * Atgriež visus produktus sistēmā
     * @return Visu produktu saraksts
     */
    fun getAllProducts(): List<Product> {
        logger.debug("Iegūst visus produktus")
        val timer = metricsService.startTimer("GET_ALL_PRODUCTS")

        try {
            val products = productRepository.findAll()
            metricsService.recordProductOperation("VIEW_ALL")
            logger.info("Iegūti ${products.size} produkti")
            return products
        } finally {
            metricsService.stopTimer(timer, "GET_ALL_PRODUCTS")
        }
    }

    /**
     * Atgriež produktu pēc ID
     * @param id Produkta identifikators
     * @return Produkts
     * @throws NoSuchElementException ja produkts nav atrasts
     */
    fun getProductById(id: Long): Product {
        logger.debug("Meklē produktu ar ID: $id")
        val timer = metricsService.startTimer("GET_PRODUCT_BY_ID")

        try {
            val product = productRepository.findById(id)
                .orElseThrow {
                    logger.warn("Produkts ar ID $id nav atrasts")
                    metricsService.recordError("PRODUCT_NOT_FOUND", "ProductService")
                    NoSuchElementException("Produkts ar ID $id nav atrasts")
                }

            metricsService.recordProductOperation("VIEW")
            logger.debug("Atrasts produkts: ${product.code}")
            return product
        } finally {
            metricsService.stopTimer(timer, "GET_PRODUCT_BY_ID")
        }
    }

    /**
     * Atgriež produktu pēc koda
     * @param code Produkta kods
     * @return Produkts vai null, ja nav atrasts
     */
    fun getProductByCode(code: String): Product? {
        logger.debug("Meklē produktu ar kodu: $code")
        val timer = metricsService.startTimer("GET_PRODUCT_BY_CODE")

        try {
            val product = productRepository.findByCode(code)
            if (product != null) {
                metricsService.recordProductOperation("VIEW")
                logger.debug("Atrasts produkts ar kodu: $code")
            } else {
                logger.debug("Produkts ar kodu $code nav atrasts")
            }
            return product
        } finally {
            metricsService.stopTimer(timer, "GET_PRODUCT_BY_CODE")
        }
    }

    /**
     * Atgriež aktīvos produktus
     * @return Aktīvo produktu saraksts
     */
    fun getActiveProducts(): List<Product> {
        logger.debug("Iegūst aktīvos produktus")
        val timer = metricsService.startTimer("GET_ACTIVE_PRODUCTS")

        try {
            val products = productRepository.findByIsActiveTrue()
            metricsService.recordProductOperation("VIEW_ACTIVE")
            logger.info("Iegūti ${products.size} aktīvi produkti")
            return products
        } finally {
            metricsService.stopTimer(timer, "GET_ACTIVE_PRODUCTS")
        }
    }

    /**
     * Meklē produktus pēc nosaukuma
     * @param name Meklējamais nosaukums (daļēja atbilstība)
     * @return Atrastie produkti
     */
    fun searchProductsByName(name: String): List<Product> {
        logger.debug("Meklē produktus pēc nosaukuma: $name")
        val timer = metricsService.startTimer("SEARCH_PRODUCTS_BY_NAME")

        try {
            val products = productRepository.findByNameContainingIgnoreCase(name)
            metricsService.recordProductOperation("SEARCH")
            logger.info("Atrasti ${products.size} produkti ar nosaukumu '$name'")
            return products
        } finally {
            metricsService.stopTimer(timer, "SEARCH_PRODUCTS_BY_NAME")
        }
    }

    /**
     * Atgriež produktus pēc kategorijas
     * @param category Kategorijas nosaukums
     * @return Produkti kategorijā
     */
    fun getProductsByCategory(category: String): List<Product> {
        logger.debug("Iegūst produktus kategorijā: $category")
        val timer = metricsService.startTimer("GET_PRODUCTS_BY_CATEGORY")

        try {
            val products = productRepository.findByCategory(category)
            metricsService.recordProductOperation("VIEW_BY_CATEGORY")
            logger.info("Atrasti ${products.size} produkti kategorijā '$category'")
            return products
        } finally {
            metricsService.stopTimer(timer, "GET_PRODUCTS_BY_CATEGORY")
        }
    }

    /**
     * Izveido jaunu produktu
     * @param product Jaunais produkts
     * @return Izveidotais produkts
     * @throws DataIntegrityViolationException ja produkts ar tādu kodu jau eksistē
     */
    @Transactional
    fun createProduct(product: Product): Product {
        logger.info("Izveido jaunu produktu: ${product.code}")
        val timer = metricsService.startTimer("CREATE_PRODUCT")

        try {
            // Validē produkta datus
            validationService.validateProduct(product)

            // Pārbauda, vai produkts ar tādu kodu jau eksistē
            if (productRepository.existsByCode(product.code)) {
                val error = "Produkts ar kodu '${product.code}' jau eksistē"
                logger.error(error)
                metricsService.recordError("DUPLICATE_PRODUCT_CODE", "ProductService")
                throw DataIntegrityViolationException(error)
            }

            // Pārbauda svītrkodu, ja tas ir norādīts
            product.barcode?.let { barcode ->
                if (productRepository.existsByBarcode(barcode)) {
                    val error = "Produkts ar svītrkodu '$barcode' jau eksistē"
                    logger.error(error)
                    metricsService.recordError("DUPLICATE_BARCODE", "ProductService")
                    throw DataIntegrityViolationException(error)
                }
            }

            val savedProduct = productRepository.save(product)

            // Reģistrē audita ierakstu
            auditService.logAction(
                action = "PRODUCT_CREATED",
                userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
                details = "Izveidots jauns produkts: ${product.code} - ${product.name}",
                entityId = savedProduct.id
            )

            metricsService.recordProductOperation("CREATE")
            logger.info("Produkts veiksmīgi izveidots: ${savedProduct.id}")
            return savedProduct

        } catch (e: Exception) {
            logger.error("Kļūda izveidojot produktu: ${e.message}", e)
            auditService.logError(
                action = "CREATE_PRODUCT",
                userId = "SYSTEM",
                error = "Kļūda izveidojot produktu: ${product.code}",
                exception = e
            )
            throw e
        } finally {
            metricsService.stopTimer(timer, "CREATE_PRODUCT")
        }
    }

    /**
     * Atjaunina esošu produktu
     * @param id Produkta identifikators
     * @param product Atjauninātie produkta dati
     * @return Atjauninātais produkts
     * @throws NoSuchElementException ja produkts nav atrasts
     * @throws DataIntegrityViolationException ja kods jau eksistē citam produktam
     */
    @Transactional
    fun updateProduct(id: Long, product: Product): Product {
        logger.info("Atjaunina produktu: $id")
        val timer = metricsService.startTimer("UPDATE_PRODUCT")

        try {
            // Pārbauda, vai produkts eksistē
            val existingProduct = productRepository.findById(id)
                .orElseThrow {
                    logger.error("Produkts ar ID $id nav atrasts")
                    metricsService.recordError("PRODUCT_NOT_FOUND", "ProductService")
                    NoSuchElementException("Produkts ar ID $id nav atrasts")
                }

            // Validē produkta datus
            validationService.validateProduct(product)

            // Pārbauda, vai cits produkts ar tādu kodu jau eksistē
            val existingProductWithCode = productRepository.findByCode(product.code)
            if (existingProductWithCode != null && existingProductWithCode.id != id) {
                val error = "Cits produkts ar kodu '${product.code}' jau eksistē"
                logger.error(error)
                metricsService.recordError("DUPLICATE_PRODUCT_CODE", "ProductService")
                throw DataIntegrityViolationException(error)
            }

            // Pārbauda svītrkodu, ja tas ir norādīts
            product.barcode?.let { barcode ->
                val existingProductWithBarcode = productRepository.findByBarcode(barcode)
                if (existingProductWithBarcode != null && existingProductWithBarcode.id != id) {
                    val error = "Cits produkts ar svītrkodu '$barcode' jau eksistē"
                    logger.error(error)
                    metricsService.recordError("DUPLICATE_BARCODE", "ProductService")
                    throw DataIntegrityViolationException(error)
                }
            }

            // Sagatavo atjaunināto produktu
            val updatedProduct = product.copy(
                id = id,
                updatedAt = LocalDateTime.now()
            )

            val savedProduct = productRepository.save(updatedProduct)

            // Reģistrē audita ierakstu
            auditService.logDataChange(
                entityType = "PRODUCT",
                entityId = id,
                operation = "UPDATE",
                userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
                oldValues = mapOf(
                    "code" to existingProduct.code,
                    "name" to existingProduct.name,
                    "price" to existingProduct.price
                ),
                newValues = mapOf(
                    "code" to product.code,
                    "name" to product.name,
                    "price" to product.price
                )
            )

            metricsService.recordProductOperation("UPDATE")
            logger.info("Produkts veiksmīgi atjaunināts: $id")
            return savedProduct

        } catch (e: Exception) {
            logger.error("Kļūda atjauninot produktu: ${e.message}", e)
            auditService.logError(
                action = "UPDATE_PRODUCT",
                userId = "SYSTEM",
                error = "Kļūda atjauninot produktu: $id",
                exception = e
            )
            throw e
        } finally {
            metricsService.stopTimer(timer, "UPDATE_PRODUCT")
        }
    }

    /**
     * Dzēš produktu
     * @param id Produkta identifikators
     * @throws NoSuchElementException ja produkts nav atrasts
     */
    @Transactional
    fun deleteProduct(id: Long) {
        logger.info("Dzēš produktu: $id")
        val timer = metricsService.startTimer("DELETE_PRODUCT")

        try {
            val product = productRepository.findById(id)
                .orElseThrow {
                    logger.error("Produkts ar ID $id nav atrasts")
                    metricsService.recordError("PRODUCT_NOT_FOUND", "ProductService")
                    NoSuchElementException("Produkts ar ID $id nav atrasts")
                }

            productRepository.deleteById(id)

            // Reģistrē audita ierakstu
            auditService.logAction(
                action = "PRODUCT_DELETED",
                userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
                details = "Dzēsts produkts: ${product.code} - ${product.name}",
                entityId = id
            )

            metricsService.recordProductOperation("DELETE")
            logger.info("Produkts veiksmīgi dzēsts: $id")

        } catch (e: Exception) {
            logger.error("Kļūda dzēšot produktu: ${e.message}", e)
            auditService.logError(
                action = "DELETE_PRODUCT",
                userId = "SYSTEM",
                error = "Kļūda dzēšot produktu: $id",
                exception = e
            )
            throw e
        } finally {
            metricsService.stopTimer(timer, "DELETE_PRODUCT")
        }
    }

    /**
     * Deaktivizē produktu (nevis dzēš)
     * @param id Produkta identifikators
     * @return Deaktivizētais produkts
     */
    @Transactional
    fun deactivateProduct(id: Long): Product {
        logger.info("Deaktivizē produktu: $id")

        val product = getProductById(id)
        val deactivatedProduct = product.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )

        val savedProduct = productRepository.save(deactivatedProduct)

        auditService.logAction(
            action = "PRODUCT_DEACTIVATED",
            userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
            details = "Deaktivizēts produkts: ${product.code}",
            entityId = id
        )

        metricsService.recordProductOperation("DEACTIVATE")
        logger.info("Produkts veiksmīgi deaktivizēts: $id")
        return savedProduct
    }

    /**
     * Aktivizē produktu
     * @param id Produkta identifikators
     * @return Aktivizētais produkts
     */
    @Transactional
    fun activateProduct(id: Long): Product {
        logger.info("Aktivizē produktu: $id")

        val product = getProductById(id)
        val activatedProduct = product.copy(
            isActive = true,
            updatedAt = LocalDateTime.now()
        )

        val savedProduct = productRepository.save(activatedProduct)

        auditService.logAction(
            action = "PRODUCT_ACTIVATED",
            userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
            details = "Aktivizēts produkts: ${product.code}",
            entityId = id
        )

        metricsService.recordProductOperation("ACTIVATE")
        logger.info("Produkts veiksmīgi aktivizēts: $id")
        return savedProduct
    }

    /**
     * Atgriež visas produktu kategorijas
     * @return Kategoriju saraksts
     */
    fun getAllCategories(): List<String> {
        logger.debug("Iegūst visas produktu kategorijas")
        val timer = metricsService.startTimer("GET_ALL_CATEGORIES")

        try {
            val categories = productRepository.findAllCategories()
            logger.info("Iegūtas ${categories.size} kategorijas")
            return categories
        } finally {
            metricsService.stopTimer(timer, "GET_ALL_CATEGORIES")
        }
    }
}