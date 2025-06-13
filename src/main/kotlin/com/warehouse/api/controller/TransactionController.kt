package com.warehouse.api.controller

import com.warehouse.api.model.Transaction
import com.warehouse.api.model.TransactionType
import com.warehouse.api.service.TransactionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Datu klase transakcijas izveidei
 */
data class CreateTransactionRequest(
    val productId: Long,
    val sourceWarehouseId: Long?,
    val destinationWarehouseId: Long?,
    val quantity: BigDecimal,
    val transactionType: TransactionType
)

/**
 * Transakciju pārvaldības kontrolleris
 * Apstrādā visas krājumu kustības (saņemšana, pārvietošana, izdošana)
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transakciju Pārvaldība", description = "Krājumu transakciju pārvaldība")
class TransactionController(private val transactionService: TransactionService) {

    /**
     * Atgriež visas transakcijas sistēmā
     */
    @GetMapping
    @Operation(summary = "Iegūst visas transakcijas")
    fun getAllTransactions(): ResponseEntity<List<Transaction>> =
        ResponseEntity.ok(transactionService.getAllTransactions())

    /**
     * Atgriež transakcijas noteiktam laika periodam
     */
    @GetMapping("/period")
    @Operation(summary = "Iegūst transakcijas laika periodam")
    fun getTransactionsByPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<List<Transaction>> {
        val transactions = transactionService.getTransactionsByPeriod(startDate, endDate)
        return ResponseEntity.ok(transactions)
    }

    /**
     * Atgriež transakcijas konkrētam produktam
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Iegūst transakcijas produktam")
    fun getTransactionsByProduct(@PathVariable productId: Long): ResponseEntity<List<Transaction>> {
        val transactions = transactionService.getTransactionsByProduct(productId)
        return ResponseEntity.ok(transactions)
    }

    /**
     * Izveido jaunu transakciju
     * Atkarībā no transakcijas tipa, dažādi parametri var būt obligāti
     */
    @PostMapping
    @Operation(summary = "Izveido jaunu transakciju")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTransaction(@RequestBody request: CreateTransactionRequest): ResponseEntity<Transaction> {
        return try {
            // Validē transakcijas parametrus atkarībā no tipa
            validateTransactionRequest(request)

            val transaction = transactionService.createTransaction(
                request.productId,
                request.sourceWarehouseId,
                request.destinationWarehouseId,
                request.quantity,
                request.transactionType
            )
            ResponseEntity.status(HttpStatus.CREATED).body(transaction)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Alternatīvā metode transakcijas izveidei ar URL parametriem
     * Saglabāta atpakaļsaderībai
     */
    @PostMapping("/legacy")
    @Operation(summary = "Izveido transakciju (mantotā metode)")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTransactionLegacy(
        @RequestParam productId: Long,
        @RequestParam(required = false) sourceWarehouseId: Long?,
        @RequestParam(required = false) destinationWarehouseId: Long?,
        @RequestParam quantity: BigDecimal,
        @RequestParam type: TransactionType
    ): ResponseEntity<Transaction> {
        return try {
            val transaction = transactionService.createTransaction(
                productId, sourceWarehouseId, destinationWarehouseId, quantity, type
            )
            ResponseEntity.status(HttpStatus.CREATED).body(transaction)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Privāta metode transakcijas pieprasījuma validācijai
     */
    private fun validateTransactionRequest(request: CreateTransactionRequest) {
        when (request.transactionType) {
            TransactionType.RECEIPT -> {
                // Saņemšanai nepieciešama galamērķa noliktava
                if (request.destinationWarehouseId == null) {
                    throw IllegalArgumentException("Saņemšanas transakcijai nepieciešama galamērķa noliktava")
                }
            }
            TransactionType.ISSUE -> {
                // Izdošanai nepieciešama avota noliktava
                if (request.sourceWarehouseId == null) {
                    throw IllegalArgumentException("Izdošanas transakcijai nepieciešama avota noliktava")
                }
            }
            TransactionType.TRANSFER -> {
                // Pārvietošanai nepieciešamas abas noliktavas
                if (request.sourceWarehouseId == null || request.destinationWarehouseId == null) {
                    throw IllegalArgumentException("Pārvietošanas transakcijai nepieciešamas gan avota, gan galamērķa noliktavas")
                }
                if (request.sourceWarehouseId == request.destinationWarehouseId) {
                    throw IllegalArgumentException("Avota un galamērķa noliktavas nevar būt vienādas")
                }
            }
        }

        // Pārbauda daudzumu
        if (request.quantity <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Transakcijas daudzumam jābūt pozitīvam")
        }
    }
}