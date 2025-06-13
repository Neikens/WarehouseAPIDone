package com.warehouse.api.service

import com.warehouse.api.exception.ValidationException
import com.warehouse.api.model.Product
import com.warehouse.api.model.Transaction
import com.warehouse.api.model.Warehouse
import com.warehouse.api.model.TransactionType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.regex.Pattern

/**
 * Validācijas serviss
 * Nodrošina datu validāciju visām galvenajām entītijām
 */
@Service
class ValidationService {

    companion object {
        // Regulārās izteiksmes validācijai
        private val PRODUCT_CODE_PATTERN = Pattern.compile("^[A-Z0-9-_]{1,50}$")
        private val BARCODE_PATTERN = Pattern.compile("^[0-9]{8,13}$")
        private val DIMENSIONS_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?\\s*x\\s*\\d+(\\.\\d+)?\\s*x\\s*\\d+(\\.\\d+)?$")
    }

    /**
     * Validē produkta datus
     * @param product produkts validācijai
     * @throws ValidationException ja validācija neizdodas
     */
    fun validateProduct(product: Product) {
        val errors = mutableListOf<String>()

        // Produkta koda validācija
        if (product.code.isBlank()) {
            errors.add("Produkta kods nevar būt tukšs")
        } else if (!PRODUCT_CODE_PATTERN.matcher(product.code).matches()) {
            errors.add("Produkta kods var saturēt tikai lielos burtus, ciparus, defises un pasvītras")
        }

        // Apraksta validācija
        if (product.description.isBlank()) {
            errors.add("Produkta apraksts nevar būt tukšs")
        } else if (product.description.length > 1000) {
            errors.add("Produkta apraksts nevar pārsniegt 1000 rakstzīmes")
        }

        // Kategorijas validācija
        if (product.category.isBlank()) {
            errors.add("Produkta kategorija nevar būt tukša")
        } else if (product.category.length > 100) {
            errors.add("Produkta kategorija nevar pārsniegt 100 rakstzīmes")
        }

        // Nosaukuma validācija
        if (product.name.isBlank()) {
            errors.add("Produkta nosaukums nevar būt tukšs")
        } else if (product.name.length > 255) {
            errors.add("Produkta nosaukums nevar pārsniegt 255 rakstzīmes")
        }

        // Cenas validācija
        if (product.price < BigDecimal.ZERO) {
            errors.add("Produkta cena nevar būt negatīva")
        } else if (product.price.scale() > 2) {
            errors.add("Produkta cena var būt maksimums ar 2 decimālzīmēm")
        }

        // Svītrkoda validācija (ja norādīts)
        product.barcode?.let { barcode ->
            if (barcode.isNotBlank() && !BARCODE_PATTERN.matcher(barcode).matches()) {
                errors.add("Svītrkods var saturēt tikai 8-13 ciparus")
            }
        }

        // Svara validācija (ja norādīts)
        product.weight?.let { weight ->
            if (weight < BigDecimal.ZERO) {
                errors.add("Produkta svars nevar būt negatīvs")
            }
        }

        // Izmēru validācija (ja norādīti)
        product.dimensions?.let { dimensions ->
            if (dimensions.isNotBlank() && !DIMENSIONS_PATTERN.matcher(dimensions).matches()) {
                errors.add("Izmēri jānorāda formātā: garums x platums x augstums (piemēram: 10.5 x 20 x 30)")
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationException("Produkta validācijas kļūdas: ${errors.joinToString("; ")}")
        }
    }

    /**
     * Validē noliktavas datus
     * @param warehouse noliktava validācijai
     * @throws ValidationException ja validācija neizdodas
     */
    fun validateWarehouse(warehouse: Warehouse) {
        val errors = mutableListOf<String>()

        // Nosaukuma validācija
        if (warehouse.name.isBlank()) {
            errors.add("Noliktavas nosaukums nevar būt tukšs")
        } else if (warehouse.name.length > 100) {
            errors.add("Noliktavas nosaukums nevar pārsniegt 100 rakstzīmes")
        }

        // Atrašanās vietas validācija
        if (warehouse.location.isBlank()) {
            errors.add("Noliktavas atrašanās vieta nevar būt tukša")
        } else if (warehouse.location.length > 255) {
            errors.add("Noliktavas atrašanās vieta nevar pārsniegt 255 rakstzīmes")
        }

        // Kapacitātes validācija
        if (warehouse.capacity <= 0) {
            errors.add("Noliktavas kapacitātei jābūt lielākai par 0")
        } else if (warehouse.capacity > 1000000) {
            errors.add("Noliktavas kapacitāte nevar pārsniegt 1,000,000")
        }

        // Apraksta validācija (ja norādīts)
        warehouse.description?.let { description ->
            if (description.length > 1000) {
                errors.add("Noliktavas apraksts nevar pārsniegt 1000 rakstzīmes")
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationException("Noliktavas validācijas kļūdas: ${errors.joinToString("; ")}")
        }
    }

    /**
     * Validē transakcijas datus
     * @param transaction transakcija validācijai
     * @throws ValidationException ja validācija neizdodas
     */
    fun validateTransaction(transaction: Transaction) {
        val errors = mutableListOf<String>()

        // Daudzuma validācija
        if (transaction.quantity <= BigDecimal.ZERO) {
            errors.add("Transakcijas daudzumam jābūt lielākam par 0")
        } else if (transaction.quantity.scale() > 2) {
            errors.add("Transakcijas daudzums var būt maksimums ar 2 decimālzīmēm")
        }

        // Transakcijas tipa specifiskā validācija
        when (transaction.transactionType) {
            TransactionType.RECEIPT -> {
                if (transaction.destinationWarehouse == null) {
                    errors.add("Saņemšanas transakcijā jānorāda galamērķa noliktava")
                }
                if (transaction.sourceWarehouse != null) {
                    errors.add("Saņemšanas transakcijā nevajag norādīt avota noliktavu")
                }
            }
            TransactionType.ISSUE -> {
                if (transaction.sourceWarehouse == null) {
                    errors.add("Izdošanas transakcijā jānorāda avota noliktava")
                }
                if (transaction.destinationWarehouse != null) {
                    errors.add("Izdošanas transakcijā nevajag norādīt galamērķa noliktavu")
                }
            }
            TransactionType.TRANSFER -> {
                if (transaction.sourceWarehouse == null) {
                    errors.add("Pārvietošanas transakcijā jānorāda avota noliktava")
                }
                if (transaction.destinationWarehouse == null) {
                    errors.add("Pārvietošanas transakcijā jānorāda galamērķa noliktava")
                }
                if (transaction.sourceWarehouse?.id == transaction.destinationWarehouse?.id) {
                    errors.add("Avota un galamērķa noliktava nevar būt vienāda")
                }
            }
        }

        // Apraksta validācija (ja norādīts)
        transaction.description?.let { description ->
            if (description.length > 500) {
                errors.add("Transakcijas apraksts nevar pārsniegt 500 rakstzīmes")
            }
        }

        // Lietotāja ID validācija (ja norādīts)
        transaction.userId?.let { userId ->
            if (userId.length > 50) {
                errors.add("Lietotāja ID nevar pārsniegt 50 rakstzīmes")
            }
        }

        // Atsauces numura validācija (ja norādīts)
        transaction.referenceNumber?.let { refNumber ->
            if (refNumber.length > 100) {
                errors.add("Atsauces numurs nevar pārsniegt 100 rakstzīmes")
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationException("Transakcijas validācijas kļūdas: ${errors.joinToString("; ")}")
        }
    }

    /**
     * Validē, vai daudzums ir derīgs krājumu operācijām
     */
    fun validateQuantity(quantity: BigDecimal, operation: String) {
        if (quantity <= BigDecimal.ZERO) {
            throw ValidationException("$operation: daudzumam jābūt pozitīvam")
        }
        if (quantity.scale() > 2) {
            throw ValidationException("$operation: daudzums var būt maksimums ar 2 decimālzīmēm")
        }
    }

    /**
     * Validē ID vērtības
     */
    fun validateId(id: Long?, entityName: String) {
        if (id == null || id <= 0) {
            throw ValidationException("$entityName ID jābūt pozitīvam skaitlim")
        }
    }
}