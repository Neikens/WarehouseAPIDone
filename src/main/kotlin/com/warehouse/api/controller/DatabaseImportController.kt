package com.warehouse.api.controller

import com.warehouse.api.dto.DatabaseConnectionDto
import com.warehouse.api.dto.ImportResultDto
import com.warehouse.api.service.DatabaseImportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/import")
class DatabaseImportController(
    private val databaseImportService: DatabaseImportService
) {
    @PostMapping("/mysql")
    fun importFromMySql(@RequestBody connectionDto: DatabaseConnectionDto): ResponseEntity<ImportResultDto> {
        val result = databaseImportService.importProductsFromExternalDatabase(connectionDto)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }
}
