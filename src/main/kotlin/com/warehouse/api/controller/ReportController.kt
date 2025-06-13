package com.warehouse.api.controller

import com.warehouse.api.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * Atskaišu ģenerēšanas kontrolleris
 * Nodrošina dažādu veidu atskaites par noliktavas darbību
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Atskaites", description = "Atskaišu ģenerēšanas galapunkts")
class ReportController(private val reportService: ReportService) {

    /**
     * Ģenerē krājumu atskaiti konkrētai noliktavai
     * Ietver informāciju par visiem produktiem noliktavā
     */
    @GetMapping("/inventory/{warehouseId}")
    @Operation(summary = "Ģenerē krājumu atskaiti konkrētai noliktavai")
    fun getInventoryReport(@PathVariable warehouseId: Long): ResponseEntity<Map<String, Any>> {
        return try {
            val report = reportService.generateInventoryReport(warehouseId)
            ResponseEntity.ok(report)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Ģenerē transakciju atskaiti noteiktam laika periodam
     * Izmanto ISO formātu datumu parametriem
     */
    @GetMapping("/transactions")
    @Operation(summary = "Ģenerē transakciju atskaiti")
    fun getTransactionReport(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startDate: LocalDateTime,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        endDate: LocalDateTime
    ): ResponseEntity<Map<String, Any>> {

        // Validē, ka sākuma datums ir pirms beigu datuma
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build()
        }

        val report = reportService.generateTransactionReport(startDate, endDate)
        return ResponseEntity.ok(report)
    }

    /**
     * Ģenerē kopsavilkuma atskaiti par visu sistēmu
     */
    @GetMapping("/summary")
    @Operation(summary = "Ģenerē sistēmas kopsavilkuma atskaiti")
    fun getSystemSummaryReport(): ResponseEntity<Map<String, Any>> {
        val report = reportService.generateSystemSummaryReport()
        return ResponseEntity.ok(report)
    }
}