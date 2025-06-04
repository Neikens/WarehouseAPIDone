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

@Service
class DatabaseImportService(
    private val productRepository: ProductRepository
) {
    private val logger = LoggerFactory.getLogger(DatabaseImportService::class.java)

    @Transactional
    fun importProductsFromExternalDatabase(connectionDto: DatabaseConnectionDto): ImportResultDto {
        try {
            // Load the JDBC driver
            Class.forName(connectionDto.driverClassName)

            // Connect to the external database
            DriverManager.getConnection(
                connectionDto.jdbcUrl,
                connectionDto.username,
                connectionDto.password
            ).use { connection ->
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(
                    """
                    SELECT code, description, barcode, category 
                    FROM products
                    """
                )

                var importedCount = 0
                val errors = mutableListOf<String>()

                while (resultSet.next()) {
                    try {
                        val product = Product(
                            code = resultSet.getString("code"),
                            description = resultSet.getString("description"),
                            barcode = resultSet.getString("barcode"),
                            category = resultSet.getString("category"),
                            name = resultSet.getString("description"), // Using description as name
                            price = BigDecimal("0.00") // Default price
                        )

                        // Check if product already exists
                        if (productRepository.findByCode(product.code) == null) {
                            productRepository.save(product)
                            importedCount++
                        }
                    } catch (e: Exception) {
                        val error = "Error importing product: ${e.message}"
                        errors.add(error)
                        logger.error(error, e)
                    }
                }

                return ImportResultDto(
                    success = true,
                    message = "Import completed with $importedCount products imported",
                    importedRecords = importedCount,
                    errors = errors
                )
            }
        } catch (e: Exception) {
            logger.error("Database import failed", e)
            return ImportResultDto(
                success = false,
                message = "Import failed: ${e.message}",
                errors = listOf(e.message ?: "Unknown error occurred")
            )
        }
    }
}
