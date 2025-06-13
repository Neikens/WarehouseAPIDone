package com.warehouse.api.controller

import com.warehouse.api.dto.DatabaseConnectionDto
import com.warehouse.api.dto.ImportResultDto
import com.warehouse.api.service.DatabaseImportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Datubāzes importa kontrolleris
 * Nodrošina funkcionalitāti datu importēšanai no ārējām datubāzēm
 */
@RestController
@RequestMapping("/api/v1/import")
@Tag(name = "Datubāzes Imports", description = "Datu importēšana no ārējām datubāzēm")
class DatabaseImportController(
    private val databaseImportService: DatabaseImportService
) {

    /**
     * Importē produktus no MySQL datubāzes
     * @param connectionDto Savienojuma parametri ārējai datubāzei
     * @return Importa rezultāts ar statistiku un kļūdu ziņojumiem
     */
    @PostMapping("/mysql")
    @Operation(
        summary = "Importē produktus no MySQL datubāzes",
        description = "Izveido savienojumu ar ārēju MySQL datubāzi un importē produktu datus"
    )
    fun importFromMySql(@RequestBody connectionDto: DatabaseConnectionDto): ResponseEntity<ImportResultDto> {
        val result = databaseImportService.importProductsFromExternalDatabase(connectionDto)

        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }

    /**
     * Importē produktus no PostgreSQL datubāzes
     */
    @PostMapping("/postgresql")
    @Operation(
        summary = "Importē produktus no PostgreSQL datubāzes",
        description = "Izveido savienojumu ar ārēju PostgreSQL datubāzi un importē produktu datus"
    )
    fun importFromPostgreSQL(@RequestBody connectionDto: DatabaseConnectionDto): ResponseEntity<ImportResultDto> {
        val result = databaseImportService.importProductsFromExternalDatabase(connectionDto)

        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }
}