package com.warehouse.api.controller

import com.warehouse.api.service.ReportService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(private val reportService: ReportService) {
    
    @GetMapping("/inventory/{warehouseId}")
    fun getInventoryReport(@PathVariable warehouseId: Long): Map<String, Any> {
        return reportService.generateInventoryReport(warehouseId)
    }

    @GetMapping("/transactions")
    fun getTransactionReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): Map<String, Any> {
        return reportService.generateTransactionReport(startDate, endDate)
    }
}
