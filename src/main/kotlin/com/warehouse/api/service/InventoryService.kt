package com.warehouse.api.service

import com.warehouse.api.model.InventoryItem
import com.warehouse.api.repository.InventoryItemRepository
import com.warehouse.api.repository.ProductRepository
import com.warehouse.api.repository.WarehouseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

/**
 * Krājumu pārvaldības serviss
 * Nodrošina krājumu uzskaiti, atjaunināšanu un pārvaldību
 */
@Service
class InventoryService(
    private val inventoryItemRepository: InventoryItemRepository,
    private val productRepository: ProductRepository,
    private val warehouseRepository: WarehouseRepository,
    private val auditService: AuditService,
    private val metricsService: MetricsService
) {
    private val logger = LoggerFactory.getLogger(InventoryService::class.java)

    /**
     * Atgriež visas krājumu vienības sistēmā
     */
    fun getAllInventoryItems(): List<InventoryItem> {
        logger.debug("Iegūst visas krājumu vienības")
        return inventoryItemRepository.findAll()
    }

    /**
     * Atgriež krājumu vienību pēc ID
     * @param id Krājumu vienības identifikators
     * @throws NoSuchElementException ja krājumu vienība nav atrasta
     */
    fun getInventoryItemById(id: Long): InventoryItem {
        logger.debug("Meklē krājumu vienību ar ID: $id")
        return inventoryItemRepository.findById(id)
            .orElseThrow {
                logger.warn("Krājumu vienība ar ID $id nav atrasta")
                NoSuchElementException("Krājumu vienība ar ID $id nav atrasta")
            }
    }

    /**
     * Atgriež visas krājumu vienības konkrētā noliktavā
     * @param warehouseId Noliktavas identifikators
     */
    fun getInventoryByWarehouse(warehouseId: Long): List<InventoryItem> {
        logger.debug("Iegūst krājumus noliktavā: $warehouseId")

        // Pārbauda, vai noliktava eksistē
        if (!warehouseRepository.existsById(warehouseId)) {
            throw NoSuchElementException("Noliktava ar ID $warehouseId nav atrasta")
        }

        return inventoryItemRepository.findByWarehouseId(warehouseId)
    }

    /**
     * Atgriež visas krājumu vienības konkrētam produktam
     * @param productId Produkta identifikators
     */
    fun getInventoryByProduct(productId: Long): List<InventoryItem> {
        logger.debug("Iegūst krājumus produktam: $productId")

        // Pārbauda, vai produkts eksistē
        if (!productRepository.existsById(productId)) {
            throw NoSuchElementException("Produkts ar ID $productId nav atrasts")
        }

        return inventoryItemRepository.findByProductId(productId)
    }

    /**
     * Atgriež produkta daudzumu konkrētā noliktavā
     * @param productId Produkta identifikators
     * @param warehouseId Noliktavas identifikators
     * @return Krājumu daudzums (0, ja nav ieraksta)
     */
    fun getProductQuantityInWarehouse(productId: Long, warehouseId: Long): BigDecimal {
        logger.debug("Iegūst produkta $productId daudzumu noliktavā $warehouseId")
        val inventoryItem = inventoryItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)
        return inventoryItem?.quantity ?: BigDecimal.ZERO
    }

    /**
     * Atjaunina krājumu daudzumu konkrētam produktam konkrētā noliktavā
     * @param productId Produkta identifikators
     * @param warehouseId Noliktavas identifikators
     * @param quantity Jaunais daudzums
     * @return Atjauninātā krājumu vienība
     */
    @Transactional
    fun updateInventoryQuantity(
        productId: Long,
        warehouseId: Long,
        quantity: BigDecimal
    ): InventoryItem {
        val startTime = System.currentTimeMillis()

        try {
            logger.info("Atjaunina krājumus: produkts=$productId, noliktava=$warehouseId, daudzums=$quantity")

            // Validē daudzumu
            if (quantity < BigDecimal.ZERO) {
                throw IllegalArgumentException("Krājumu daudzums nevar būt negatīvs")
            }

            // Pārbauda produkta eksistenci
            val product = productRepository.findById(productId)
                .orElseThrow {
                    logger.error("Produkts ar ID $productId nav atrasts")
                    NoSuchElementException("Produkts ar ID $productId nav atrasts")
                }
            logger.debug("Atrasts produkts: ${product.code}")

            // Pārbauda noliktavas eksistenci
            val warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow {
                    logger.error("Noliktava ar ID $warehouseId nav atrasta")
                    NoSuchElementException("Noliktava ar ID $warehouseId nav atrasta")
                }
            logger.debug("Atrasta noliktava: ${warehouse.name}")

            // Meklē esošo krājumu vienību
            val existingItem = inventoryItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)

            val result = if (existingItem != null) {
                logger.debug("Atjaunina esošo krājumu vienību: ${existingItem.id}")

                val oldQuantity = existingItem.quantity
                val updatedItem = existingItem.copy(
                    quantity = quantity,
                    updatedAt = LocalDateTime.now()
                )

                val savedItem = inventoryItemRepository.save(updatedItem)

                // Reģistrē audita ierakstu
                auditService.logDataChange(
                    entityType = "INVENTORY_ITEM",
                    entityId = savedItem.id!!,
                    operation = "UPDATE",
                    userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
                    oldValues = mapOf("quantity" to oldQuantity),
                    newValues = mapOf("quantity" to quantity)
                )

                savedItem
            } else {
                logger.debug("Izveido jaunu krājumu vienību")

                val newItem = InventoryItem(
                    product = product,
                    warehouse = warehouse,
                    quantity = quantity
                )

                val savedItem = inventoryItemRepository.save(newItem)

                // Reģistrē audita ierakstu
                auditService.logAction(
                    action = "INVENTORY_ITEM_CREATED",
                    userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
                    details = "Izveidota jauna krājumu vienība: produkts=${product.code}, noliktava=${warehouse.name}, daudzums=$quantity",
                    entityId = savedItem.id
                )

                savedItem
            }

            // Reģistrē metriku
            metricsService.recordInventoryUpdate(warehouseId, productId)

            val duration = System.currentTimeMillis() - startTime
            auditService.logPerformanceMetric("UPDATE_INVENTORY_QUANTITY", duration)

            logger.info("Krājumi veiksmīgi atjaunināti: ${result.id}")
            return result

        } catch (e: Exception) {
            logger.error("Kļūda atjauninot krājumus: ${e.message}", e)
            auditService.logError(
                action = "UPDATE_INVENTORY_QUANTITY",
                userId = "SYSTEM",
                error = "Kļūda atjauninot krājumus: produkts=$productId, noliktava=$warehouseId",
                exception = e
            )
            throw e
        }
    }

    /**
     * Palielina krājumu daudzumu (pievieno esošajam daudzumam)
     * @param productId Produkta identifikators
     * @param warehouseId Noliktavas identifikators
     * @param quantity Pievienojamais daudzums
     * @return Atjauninātā krājumu vienība
     */
    @Transactional
    fun increaseStock(productId: Long, warehouseId: Long, quantity: BigDecimal): InventoryItem {
        logger.info("Palielina krājumus: produkts=$productId, noliktava=$warehouseId, daudzums=+$quantity")

        val currentItem = inventoryItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)

        return if (currentItem != null) {
            val newQuantity = currentItem.quantity.add(quantity)
            updateInventoryQuantity(productId, warehouseId, newQuantity)
        } else {
            createInventoryItem(productId, warehouseId, quantity)
        }
    }

    /**
     * Samazina krājumu daudzumu (atņem no esošā daudzuma)
     * @param productId Produkta identifikators
     * @param warehouseId Noliktavas identifikators
     * @param quantity Atņemamais daudzums
     * @return Atjauninātā krājumu vienība
     */
    @Transactional
    fun decreaseStock(productId: Long, warehouseId: Long, quantity: BigDecimal): InventoryItem {
        logger.info("Samazina krājumus: produkts=$productId, noliktava=$warehouseId, daudzums=-$quantity")

        val currentItem = inventoryItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            ?: throw NoSuchElementException("Krājumu ieraksts nav atrasts produktam $productId noliktavā $warehouseId")

        val newQuantity = currentItem.quantity.subtract(quantity)
        if (newQuantity < BigDecimal.ZERO) {
            throw IllegalStateException("Nepietiekami krājumi. Pieejams: ${currentItem.quantity}, nepieciešams: $quantity")
        }

        return updateInventoryQuantity(productId, warehouseId, newQuantity)
    }

    /**
     * Koriģē krājumu daudzumu (pievieno vai atņem)
     * @param productId Produkta identifikators
     * @param warehouseId Noliktavas identifikators
     * @param adjustment Korekcijas daudzums (pozitīvs - pievieno, negatīvs - atņem)
     * @return Atjauninātā krājumu vienība
     */
    @Transactional
    fun adjustInventory(
        productId: Long,
        warehouseId: Long,
        adjustment: BigDecimal
    ): InventoryItem {
        logger.info("Koriģē krājumus: produkts=$productId, noliktava=$warehouseId, korekcija=$adjustment")

        val inventoryItem = inventoryItemRepository
            .findByProductIdAndWarehouseId(productId, warehouseId)
            ?: throw NoSuchElementException("Krājumu vienība nav atrasta")

        val newQuantity = inventoryItem.quantity.add(adjustment)

        if (newQuantity < BigDecimal.ZERO) {
            logger.warn("Mēģinājums izveidot negatīvus krājumus: $newQuantity")
            throw IllegalStateException("Krājumi nevar būt negatīvi. Pašreizējais daudzums: ${inventoryItem.quantity}, korekcija: $adjustment")
        }

        val updatedItem = inventoryItem.copy(
            quantity = newQuantity,
            updatedAt = LocalDateTime.now()
        )

        val result = inventoryItemRepository.save(updatedItem)

        // Reģistrē audita ierakstu
        auditService.logAction(
            action = "INVENTORY_ADJUSTED",
            userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
            details = "Krājumi koriģēti: korekcija=$adjustment, jauns daudzums=$newQuantity",
            entityId = result.id
        )

        logger.info("Krājumi veiksmīgi koriģēti: ${result.id}")
        return result
    }

    /**
     * Izveido jaunu krājumu vienību
     * @param productId Produkta identifikators
     * @param warehouseId Noliktavas identifikators
     * @param quantity Sākotnējais daudzums
     * @return Izveidotā krājumu vienība
     */
    @Transactional
    fun createInventoryItem(
        productId: Long,
        warehouseId: Long,
        quantity: BigDecimal
    ): InventoryItem {
        val startTime = System.currentTimeMillis()

        try {
            logger.info("Izveido krājumu vienību: produkts=$productId, noliktava=$warehouseId, daudzums=$quantity")

            // Validē daudzumu
            if (quantity < BigDecimal.ZERO) {
                throw IllegalArgumentException("Sākotnējais krājumu daudzums nevar būt negatīvs")
            }

            val product = productRepository.findById(productId)
                .orElseThrow {
                    logger.error("Produkts ar ID $productId nav atrasts")
                    NoSuchElementException("Produkts ar ID $productId nav atrasts")
                }
            logger.debug("Atrasts produkts: ${product.code}")

            val warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow {
                    logger.error("Noliktava ar ID $warehouseId nav atrasta")
                    NoSuchElementException("Noliktava ar ID $warehouseId nav atrasta")
                }
            logger.debug("Atrasta noliktava: ${warehouse.name}")

            // Pārbauda, vai krājumu vienība jau eksistē
            val existingItem = inventoryItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            if (existingItem != null) {
                logger.info("Krājumu vienība jau eksistē: ${existingItem.id}, atjaunina daudzumu")
                return updateInventoryQuantity(productId, warehouseId, quantity)
            }

            val newItem = InventoryItem(
                product = product,
                warehouse = warehouse,
                quantity = quantity
            )

            val savedItem = inventoryItemRepository.save(newItem)

            // Reģistrē audita ierakstu
            auditService.logAction(
                action = "INVENTORY_ITEM_CREATED",
                userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
                details = "Izveidota krājumu vienība: produkts=${product.code}, noliktava=${warehouse.name}, daudzums=$quantity",
                entityId = savedItem.id
            )

            // Reģistrē metriku
            metricsService.recordInventoryUpdate(warehouseId, productId)

            val duration = System.currentTimeMillis() - startTime
            auditService.logPerformanceMetric("CREATE_INVENTORY_ITEM", duration)

            logger.info("Krājumu vienība veiksmīgi izveidota: ${savedItem.id}")
            return savedItem

        } catch (e: Exception) {
            logger.error("Kļūda izveidojot krājumu vienību: ${e.message}", e)
            auditService.logError(
                action = "CREATE_INVENTORY_ITEM",
                userId = "SYSTEM",
                error = "Kļūda izveidojot krājumu vienību: produkts=$productId, noliktava=$warehouseId",
                exception = e
            )
            throw e
        }
    }

    /**
     * Aprēķina kopējo produkta daudzumu visos noliktavās
     * @param productId Produkta identifikators
     * @return Kopējais daudzums
     */
    fun getTotalQuantityByProduct(productId: Long): BigDecimal {
        logger.debug("Aprēķina kopējo daudzumu produktam: $productId")
        return inventoryItemRepository.getTotalQuantityByProductId(productId)
    }

    /**
     * Atgriež krājumu vienības, kas ir zem minimālā līmeņa
     * @return Saraksts ar krājumu vienībām zem minimālā līmeņa
     */
    fun getLowStockItems(): List<InventoryItem> {
        logger.debug("Meklē krājumus zem minimālā līmeņa")
        return inventoryItemRepository.findItemsBelowMinimumLevel()
    }

    /**
     * Dzēš krājumu vienību
     * @param id Krājumu vienības identifikators
     */
    @Transactional
    fun deleteInventoryItem(id: Long) {
        logger.info("Dzēš krājumu vienību: $id")

        val inventoryItem = getInventoryItemById(id)
        inventoryItemRepository.deleteById(id)

        auditService.logAction(
            action = "INVENTORY_ITEM_DELETED",
            userId = "SYSTEM", // TODO: Iegūt no drošības konteksta
            details = "Dzēsta krājumu vienība: produkts=${inventoryItem.product.code}, noliktava=${inventoryItem.warehouse.name}",
            entityId = id
        )

        logger.info("Krājumu vienība veiksmīgi dzēsta: $id")
    }
}