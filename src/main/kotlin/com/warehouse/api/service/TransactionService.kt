package com.warehouse.api.service

import com.warehouse.api.model.Transaction
import com.warehouse.api.model.TransactionType
import com.warehouse.api.repository.TransactionRepository
import com.warehouse.api.repository.ProductRepository
import com.warehouse.api.repository.WarehouseRepository
import com.warehouse.api.exception.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

/**
 * Transakciju pārvaldības serviss
 * Nodrošina transakciju izveidi, validāciju un krājumu atjaunināšanu
 */
@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
    private val warehouseRepository: WarehouseRepository,
    private val auditService: AuditService,
    private val metricsService: MetricsService
) {
    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    /**
     * Lazy injection, lai izvairītos no cikliskās atkarības
     */
    @Autowired
    @Lazy
    private lateinit var inventoryService: InventoryService

    /**
     * Atgriež visas transakcijas
     */
    fun getAllTransactions(): List<Transaction> {
        logger.debug("Iegūst visas transakcijas")
        return transactionRepository.findAll()
    }

    /**
     * Atgriež transakcijas pēc produkta ID
     */
    fun getTransactionsByProduct(productId: Long): List<Transaction> {
        logger.debug("Iegūst transakcijas produktam: $productId")
        return transactionRepository.findByProductId(productId)
    }

    /**
     * Atgriež transakcijas pēc noliktavas ID
     */
    fun getTransactionsByWarehouse(warehouseId: Long): List<Transaction> {
        logger.debug("Iegūst transakcijas noliktavai: $warehouseId")
        return transactionRepository.findAllByWarehouseId(warehouseId)
    }

    /**
     * Atgriež transakcijas noteiktam laika periodam
     */
    fun getTransactionsByPeriod(startDate: LocalDateTime, endDate: LocalDateTime): List<Transaction> {
        logger.debug("Iegūst transakcijas periodam: $startDate - $endDate")
        return transactionRepository.findByTimestampBetween(startDate, endDate)
    }

    /**
     * Atgriež transakcijas noteiktam laika periodam (alias metode)
     */
    fun getTransactionsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Transaction> {
        return getTransactionsByPeriod(startDate, endDate)
    }

    /**
     * Izveido jaunu transakciju ar pilnu validāciju un krājumu atjaunināšanu
     * @param productId produkta ID
     * @param sourceWarehouseId avota noliktavas ID (var būt null RECEIPT gadījumā)
     * @param destinationWarehouseId galamērķa noliktavas ID (var būt null ISSUE gadījumā)
     * @param quantity transakcijas daudzums
     * @param type transakcijas tips
     * @return izveidotā transakcija
     */
    @Transactional
    fun createTransaction(
        productId: Long,
        sourceWarehouseId: Long?,
        destinationWarehouseId: Long?,
        quantity: BigDecimal,
        type: TransactionType
    ): Transaction {
        return createTransaction(
            productId = productId,
            sourceWarehouseId = sourceWarehouseId,
            destinationWarehouseId = destinationWarehouseId,
            quantity = quantity,
            type = type,
            description = null,
            userId = "SYSTEM",
            referenceNumber = null
        )
    }

    /**
     * Izveido jaunu transakciju ar pilniem parametriem
     * @param productId produkta ID
     * @param sourceWarehouseId avota noliktavas ID (var būt null RECEIPT gadījumā)
     * @param destinationWarehouseId galamērķa noliktavas ID (var būt null ISSUE gadījumā)
     * @param quantity transakcijas daudzums
     * @param type transakcijas tips
     * @param description papildu apraksts
     * @param userId lietotāja ID
     * @param referenceNumber atsauces numurs
     * @return izveidotā transakcija
     */
    @Transactional
    fun createTransaction(
        productId: Long,
        sourceWarehouseId: Long?,
        destinationWarehouseId: Long?,
        quantity: BigDecimal,
        type: TransactionType,
        description: String? = null,
        userId: String? = null,
        referenceNumber: String? = null
    ): Transaction {
        val startTime = System.currentTimeMillis()

        try {
            logger.info("Izveido transakciju: produkts=$productId, tips=$type, daudzums=$quantity")

            // Validē produkta esamību
            val product = productRepository.findById(productId)
                .orElseThrow {
                    logger.error("Produkts ar ID $productId nav atrasts")
                    NoSuchElementException("Produkts ar ID $productId nav atrasts")
                }

            // Pārbauda, vai produkts ir aktīvs
            if (!product.isAvailable()) {
                throw ValidationException("Produkts ${product.code} nav aktīvs")
            }

            // Validē un iegūst noliktavas
            val sourceWarehouse = sourceWarehouseId?.let {
                warehouseRepository.findById(it)
                    .orElseThrow {
                        logger.error("Avota noliktava ar ID $it nav atrasta")
                        NoSuchElementException("Avota noliktava ar ID $it nav atrasta")
                    }
            }

            val destinationWarehouse = destinationWarehouseId?.let {
                warehouseRepository.findById(it)
                    .orElseThrow {
                        logger.error("Galamērķa noliktava ar ID $it nav atrasta")
                        NoSuchElementException("Galamērķa noliktava ar ID $it nav atrasta")
                    }
            }

            // Izveido transakciju
            val transaction = Transaction(
                product = product,
                sourceWarehouse = sourceWarehouse,
                destinationWarehouse = destinationWarehouse,
                quantity = quantity,
                transactionType = type,
                description = description,
                userId = userId,
                referenceNumber = referenceNumber
            )

            // Validē transakcijas loģiku
            validateTransaction(transaction)

            // Pārbauda krājumu pietiekamību (ja nepieciešams)
            if (sourceWarehouse != null) {
                // LABOJUMS: noņemts !! un pievienota null pārbaude
                sourceWarehouse.id?.let { warehouseId ->
                    product.id?.let { prodId ->
                        validateSufficientStock(prodId, warehouseId, quantity)
                    }
                }
            }

            // Saglabā transakciju
            val savedTransaction = transactionRepository.save(transaction)

            // Atjaunina krājumus
            updateInventoryForTransaction(savedTransaction)

            // Reģistrē audita ierakstu
            auditService.logAction(
                action = "TRANSACTION_CREATED",
                userId = userId ?: "SYSTEM",
                details = "Izveidota transakcija: ${type.description}, produkts=${product.code}, daudzums=$quantity",
                entityId = savedTransaction.id
            )

            // Reģistrē metriku
            metricsService.recordTransaction(type.name)

            val duration = System.currentTimeMillis() - startTime
            auditService.logPerformanceMetric("CREATE_TRANSACTION", duration, userId)

            logger.info("Transakcija veiksmīgi izveidota: ${savedTransaction.id}")
            return savedTransaction

        } catch (e: Exception) {
            logger.error("Kļūda izveidojot transakciju: ${e.message}", e)
            auditService.logError(
                action = "CREATE_TRANSACTION",
                userId = userId ?: "SYSTEM",
                error = "Kļūda izveidojot transakciju: produkts=$productId, tips=$type",
                exception = e
            )
            throw e
        }
    }

    /**
     * Validē transakcijas datus
     */
    private fun validateTransaction(transaction: Transaction) {
        if (!transaction.isValid()) {
            throw ValidationException("Nederīga transakcija: ${getValidationErrorMessage(transaction)}")
        }

        if (transaction.quantity <= BigDecimal.ZERO) {
            throw ValidationException("Transakcijas daudzumam jābūt pozitīvam")
        }
    }

    /**
     * Atgriež validācijas kļūdas ziņojumu
     */
    private fun getValidationErrorMessage(transaction: Transaction): String {
        return when (transaction.transactionType) {
            TransactionType.RECEIPT -> {
                if (transaction.destinationWarehouse == null) "Saņemšanas transakcijā jānorāda galamērķa noliktava"
                else "Nezināma kļūda"
            }
            TransactionType.ISSUE -> {
                if (transaction.sourceWarehouse == null) "Izdošanas transakcijā jānorāda avota noliktava"
                else "Nezināma kļūda"
            }
            TransactionType.TRANSFER -> {
                when {
                    transaction.sourceWarehouse == null -> "Pārvietošanas transakcijā jānorāda avota noliktava"
                    transaction.destinationWarehouse == null -> "Pārvietošanas transakcijā jānorāda galamērķa noliktava"
                    transaction.sourceWarehouse.id == transaction.destinationWarehouse.id ->
                        "Avota un galamērķa noliktava nevar būt vienāda"
                    else -> "Nezināma kļūda"
                }
            }
        }
    }

    /**
     * Pārbauda, vai noliktavā ir pietiekami daudz krājumu
     */
    private fun validateSufficientStock(productId: Long, warehouseId: Long, requiredQuantity: BigDecimal) {
        val currentStock = inventoryService.getProductQuantityInWarehouse(productId, warehouseId)
        if (currentStock < requiredQuantity) {
            throw ValidationException(
                "Nepietiekami krājumi. Pieejams: $currentStock, nepieciešams: $requiredQuantity"
            )
        }
    }

    /**
     * Atjaunina krājumus pēc transakcijas
     */
    private fun updateInventoryForTransaction(transaction: Transaction) {
        when (transaction.transactionType) {
            TransactionType.RECEIPT -> {
                // Palielina krājumus galamērķa noliktavā
                // LABOJUMS: pievienota null pārbaude
                transaction.product.id?.let { productId ->
                    transaction.destinationWarehouse?.id?.let { warehouseId ->
                        inventoryService.increaseStock(productId, warehouseId, transaction.quantity)
                    }
                }
            }
            TransactionType.ISSUE -> {
                // Samazina krājumus avota noliktavā
                // LABOJUMS: pievienota null pārbaude
                transaction.product.id?.let { productId ->
                    transaction.sourceWarehouse?.id?.let { warehouseId ->
                        inventoryService.decreaseStock(productId, warehouseId, transaction.quantity)
                    }
                }
            }
            TransactionType.TRANSFER -> {
                // Samazina krājumus avota noliktavā un palielina galamērķa noliktavā
                // LABOJUMS: pievienota null pārbaude
                transaction.product.id?.let { productId ->
                    transaction.sourceWarehouse?.id?.let { sourceId ->
                        inventoryService.decreaseStock(productId, sourceId, transaction.quantity)
                    }
                    transaction.destinationWarehouse?.id?.let { destId ->
                        inventoryService.increaseStock(productId, destId, transaction.quantity)
                    }
                }
            }
        }
    }

    /**
     * Atgriež transakciju pēc ID
     */
    fun getTransactionById(id: Long): Transaction {
        logger.debug("Meklē transakciju ar ID: $id")
        return transactionRepository.findById(id)
            .orElseThrow {
                logger.warn("Transakcija ar ID $id nav atrasta")
                NoSuchElementException("Transakcija ar ID $id nav atrasta")
            }
    }
}