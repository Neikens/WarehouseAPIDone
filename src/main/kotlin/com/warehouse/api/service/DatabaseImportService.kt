package com.warehouse.api.service

import com.warehouse.api.dto.DatabaseConnectionDto
import com.warehouse.api.dto.ImportResultDto
import com.warehouse.api.model.Product
import com.warehouse.api.repository.ProductRepository
import org.springframework.stereotype.Service
import java.sql.DriverManager
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.SQLException
import java.time.LocalDateTime

/**
 * Datubāzes importa serviss
 * Nodrošina produktu importēšanu no ārējām datubāzēm
 */
@Service
class DatabaseImportService(
    private val productRepository: ProductRepository,
    private val auditService: AuditService
) {
    private val logger = LoggerFactory.getLogger(DatabaseImportService::class.java)

    /**
     * Importē produktus no ārējas datubāzes
     * @param connectionDto Datubāzes savienojuma parametri
     * @return Importa rezultāts ar statistiku un kļūdām
     */
    @Transactional
    fun importProductsFromExternalDatabase(connectionDto: DatabaseConnectionDto): ImportResultDto {
        val startTime = System.currentTimeMillis()
        logger.info("Sākas produktu imports no ārējas datubāzes: ${connectionDto.jdbcUrl}")

        try {
            // Ielādē JDBC draiveri
            Class.forName(connectionDto.driverClassName)
            logger.debug("JDBC draiveris ielādēts: ${connectionDto.driverClassName}")

            // Izveido savienojumu ar ārējo datubāzi
            DriverManager.getConnection(
                connectionDto.jdbcUrl,
                connectionDto.username,
                connectionDto.password
            ).use { connection ->
                logger.info("Savienojums ar ārējo datubāzi izveidots")

                val statement = connection.createStatement()

                // Pārbauda, vai tabula eksistē
                val tableExistsQuery = """
                    SELECT COUNT(*) FROM information_schema.tables 
                    WHERE table_name = 'products'
                """

                val tableCheckResult = statement.executeQuery(tableExistsQuery)
                tableCheckResult.next()
                if (tableCheckResult.getInt(1) == 0) {
                    throw SQLException("Tabula 'products' nav atrasta ārējā datubāzē")
                }

                // Izpilda galveno importa vaicājumu
                val resultSet = statement.executeQuery(
                    """
                    SELECT code, description, barcode, category, 
                           COALESCE(name, description) as name,
                           COALESCE(price, 0.00) as price
                    FROM products
                    WHERE code IS NOT NULL AND code != ''
                    ORDER BY code
                    """
                )

                var importedCount = 0
                var skippedCount = 0
                var updatedCount = 0
                val errors = mutableListOf<String>()

                logger.info("Sākas produktu apstrāde")

                while (resultSet.next()) {
                    try {
                        val productCode = resultSet.getString("code")?.trim()
                        val description = resultSet.getString("description")?.trim() ?: ""
                        val barcode = resultSet.getString("barcode")?.trim()
                        val category = resultSet.getString("category")?.trim() ?: "Nezināma"
                        val name = resultSet.getString("name")?.trim() ?: description
                        val price = resultSet.getBigDecimal("price") ?: BigDecimal.ZERO

                        // Validē obligātos laukus
                        if (productCode.isNullOrBlank()) {
                            errors.add("Izlaists produkts ar tukšu kodu")
                            continue
                        }

                        if (description.isBlank()) {
                            errors.add("Izlaists produkts $productCode ar tukšu aprakstu")
                            continue
                        }

                        // Pārbauda, vai produkts jau eksistē
                        val existingProduct = productRepository.findByCode(productCode)

                        if (existingProduct != null) {
                            // Atjaunina esošo produktu
                            val updatedProduct = existingProduct.copy(
                                description = description,
                                barcode = barcode,
                                category = category,
                                name = name,
                                price = price,
                                updatedAt = LocalDateTime.now()
                            )

                            productRepository.save(updatedProduct)
                            updatedCount++

                            auditService.logAction(
                                "PRODUCT_UPDATED_FROM_IMPORT",
                                "SYSTEM",
                                "Produkts atjaunināts no importa: $productCode"
                            )
                        } else {
                            // Izveido jaunu produktu
                            val newProduct = Product(
                                code = productCode,
                                description = description,
                                barcode = barcode,
                                category = category,
                                name = name,
                                price = price
                            )

                            productRepository.save(newProduct)
                            importedCount++

                            auditService.logAction(
                                "PRODUCT_IMPORTED",
                                "SYSTEM",
                                "Jauns produkts importēts: $productCode"
                            )
                        }

                        // Logē progresu katru 100. produktu
                        if ((importedCount + updatedCount) % 100 == 0) {
                            logger.info("Apstrādāti ${importedCount + updatedCount} produkti")
                        }

                    } catch (e: Exception) {
                        val error = "Kļūda importējot produktu: ${e.message}"
                        errors.add(error)
                        logger.error(error, e)

                        auditService.logError(
                            "PRODUCT_IMPORT_ERROR",
                            "SYSTEM",
                            error,
                            e
                        )
                    }
                }

                val duration = System.currentTimeMillis() - startTime
                val successMessage = "Imports pabeigts: $importedCount jauni, $updatedCount atjaunināti, $skippedCount izlaisti"

                logger.info(successMessage)
                auditService.logPerformanceMetric("DATABASE_IMPORT", duration, "SYSTEM")

                return ImportResultDto(
                    success = true,
                    message = successMessage,
                    importedRecords = importedCount,
                    skippedRecords = skippedCount,
                    errors = errors,
                    durationMs = duration,
                    endTime = LocalDateTime.now()
                )
            }
        } catch (e: ClassNotFoundException) {
            val error = "JDBC draiveris nav atrasts: ${connectionDto.driverClassName}"
            logger.error(error, e)
            auditService.logError("DATABASE_IMPORT_DRIVER_ERROR", "SYSTEM", error, e)

            return ImportResultDto(
                success = false,
                message = error,
                errors = listOf(error)
            )
        } catch (e: SQLException) {
            val error = "Datubāzes kļūda: ${e.message}"
            logger.error(error, e)
            auditService.logError("DATABASE_IMPORT_SQL_ERROR", "SYSTEM", error, e)

            return ImportResultDto(
                success = false,
                message = error,
                errors = listOf(error)
            )
        } catch (e: Exception) {
            val error = "Neparedzēta kļūda importa laikā: ${e.message}"
            logger.error(error, e)
            auditService.logError("DATABASE_IMPORT_UNEXPECTED_ERROR", "SYSTEM", error, e)

            return ImportResultDto(
                success = false,
                message = error,
                errors = listOf(error)
            )
        }
    }

    /**
     * Pārbauda savienojumu ar ārējo datubāzi
     * @param connectionDto Datubāzes savienojuma parametri
     * @return true, ja savienojums ir veiksmīgs
     */
    fun testConnection(connectionDto: DatabaseConnectionDto): Boolean {
        return try {
            Class.forName(connectionDto.driverClassName)
            DriverManager.getConnection(
                connectionDto.jdbcUrl,
                connectionDto.username,
                connectionDto.password
            ).use { connection ->
                connection.isValid(5) // 5 sekunžu timeout
            }
        } catch (e: Exception) {
            logger.error("Savienojuma tests neizdevās: ${e.message}", e)
            false
        }
    }
}