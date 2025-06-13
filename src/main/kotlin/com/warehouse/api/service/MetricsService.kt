package com.warehouse.api.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.time.LocalDateTime

/**
 * Metriku serviss
 * Nodrošina sistēmas veiktspējas un lietojuma metriku reģistrēšanu
 */
@Service
class MetricsService(private val registry: MeterRegistry) {

    private val logger = LoggerFactory.getLogger(MetricsService::class.java)

    // Kešo skaitītājus labākai veiktspējai
    private val counters = ConcurrentHashMap<String, Counter>()
    private val gaugeValues = ConcurrentHashMap<String, AtomicLong>()
    private val timers = ConcurrentHashMap<String, Timer>()

    /**
     * Reģistrē transakcijas metriku
     * @param type Transakcijas tips (RECEIPT, TRANSFER, ISSUE)
     */
    fun recordTransaction(type: String) {
        logger.debug("Reģistrē transakcijas metriku: $type")

        val counter = counters.computeIfAbsent("warehouse.transactions.$type") {
            Counter.builder("warehouse.transactions")
                .description("Noliktavas transakciju skaits")
                .tag("type", type)
                .register(registry)
        }

        counter.increment()

        // Reģistrē arī kopējo transakciju skaitītāju
        val totalCounter = counters.computeIfAbsent("warehouse.transactions.total") {
            Counter.builder("warehouse.transactions.total")
                .description("Kopējais transakciju skaits")
                .register(registry)
        }
        totalCounter.increment()
    }

    /**
     * Reģistrē produkta operācijas metriku
     * @param operation Operācijas tips (CREATE, UPDATE, DELETE, VIEW)
     */
    fun recordProductOperation(operation: String) {
        logger.debug("Reģistrē produkta operācijas metriku: $operation")

        val counter = counters.computeIfAbsent("warehouse.products.$operation") {
            Counter.builder("warehouse.products")
                .description("Produktu operāciju skaits")
                .tag("operation", operation)
                .register(registry)
        }

        counter.increment()
    }

    /**
     * Atjaunina noliktavas krājumu līmeņa metriku
     * @param warehouseId Noliktavas identifikators
     * @param level Pašreizējais krājumu līmenis
     */
    fun updateInventoryLevel(warehouseId: Long, level: Double) {
        logger.debug("Atjaunina krājumu līmeni noliktavai $warehouseId: $level")

        val gaugeKey = "warehouse.inventory.level.$warehouseId"
        val atomicLevel = gaugeValues.computeIfAbsent(gaugeKey) {
            val atomic = AtomicLong(level.toLong())
            // Reģistrē Gauge ar statisko metodi
            registry.gauge("warehouse.inventory.level",
                listOf(io.micrometer.core.instrument.Tag.of("warehouseId", warehouseId.toString())),
                atomic) { it.get().toDouble() }
            atomic
        }

        atomicLevel.set(level.toLong())
    }

    /**
     * Reģistrē krājumu pārbaudes metriku
     * @param warehouseId Noliktavas identifikators
     */
    fun recordInventoryCheck(warehouseId: Long) {
        logger.debug("Reģistrē krājumu pārbaudi noliktavai: $warehouseId")

        val counter = counters.computeIfAbsent("warehouse.inventory.checks.$warehouseId") {
            Counter.builder("warehouse.inventory.checks")
                .description("Krājumu pārbaužu skaits")
                .tag("warehouseId", warehouseId.toString())
                .register(registry)
        }

        counter.increment()

        // Reģistrē pēdējās pārbaudes laiku
        val gaugeKey = "warehouse.inventory.lastCheck.$warehouseId"
        val lastCheckTime = gaugeValues.computeIfAbsent(gaugeKey) {
            val atomic = AtomicLong(System.currentTimeMillis())
            registry.gauge("warehouse.inventory.lastCheck",
                listOf(io.micrometer.core.instrument.Tag.of("warehouseId", warehouseId.toString())),
                atomic) { it.get().toDouble() }
            atomic
        }
        lastCheckTime.set(System.currentTimeMillis())
    }

    /**
     * Reģistrē zemu krājumu brīdinājuma metriku
     * @param productId Produkta identifikators
     */
    fun recordLowStockAlert(productId: Long) {
        logger.debug("Reģistrē zemu krājumu brīdinājumu produktam: $productId")

        val counter = counters.computeIfAbsent("warehouse.inventory.lowstock.$productId") {
            Counter.builder("warehouse.inventory.lowstock")
                .description("Zemu krājumu brīdinājumu skaits")
                .tag("productId", productId.toString())
                .register(registry)
        }

        counter.increment()

        // Reģistrē kopējo zemu krājumu brīdinājumu skaitītāju
        val totalLowStockCounter = counters.computeIfAbsent("warehouse.inventory.lowstock.total") {
            Counter.builder("warehouse.inventory.lowstock.total")
                .description("Kopējais zemu krājumu brīdinājumu skaits")
                .register(registry)
        }
        totalLowStockCounter.increment()
    }

    /**
     * Reģistrē krājumu atjaunināšanas metriku
     * @param warehouseId Noliktavas identifikators
     * @param productId Produkta identifikators
     */
    fun recordInventoryUpdate(warehouseId: Long, productId: Long) {
        logger.debug("Reģistrē krājumu atjaunināšanu: noliktava=$warehouseId, produkts=$productId")

        val counter = counters.computeIfAbsent("warehouse.inventory.updates") {
            Counter.builder("warehouse.inventory.updates")
                .description("Krājumu atjaunināšanu skaits")
                .tag("warehouseId", warehouseId.toString())
                .tag("productId", productId.toString())
                .register(registry)
        }

        counter.increment()

        // Reģistrē kopējo atjaunināšanu skaitītāju
        val totalUpdatesCounter = counters.computeIfAbsent("warehouse.inventory.updates.total") {
            Counter.builder("warehouse.inventory.updates.total")
                .description("Kopējais krājumu atjaunināšanu skaits")
                .register(registry)
        }
        totalUpdatesCounter.increment()
    }

    /**
     * Reģistrē API pieprasījuma metriku
     * @param endpoint API galapunkts
     * @param method HTTP metode
     * @param statusCode Atbildes statusa kods
     */
    fun recordApiRequest(endpoint: String, method: String, statusCode: Int) {
        logger.debug("Reģistrē API pieprasījumu: $method $endpoint -> $statusCode")

        val counter = counters.computeIfAbsent("warehouse.api.requests.$method.$endpoint.$statusCode") {
            Counter.builder("warehouse.api.requests")
                .description("API pieprasījumu skaits")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", statusCode.toString())
                .register(registry)
        }

        counter.increment()
    }

    /**
     * Mēra operācijas izpildes laiku
     * @param operationName Operācijas nosaukums
     * @return Timer.Sample objekts, ko jāaptur pēc operācijas pabeigšanas
     */
    fun startTimer(operationName: String): Timer.Sample {
        logger.debug("Sāk mērīt laiku operācijai: $operationName")

        timers.computeIfAbsent(operationName) {
            Timer.builder("warehouse.operation.duration")
                .description("Operāciju izpildes laiks")
                .tag("operation", operationName)
                .register(registry)
        }

        return Timer.start(registry)
    }

    /**
     * Beidz mērīt operācijas laiku
     * @param sample Timer.Sample objekts no startTimer metodes
     * @param operationName Operācijas nosaukums
     */
    fun stopTimer(sample: Timer.Sample, operationName: String) {
        val timer = timers[operationName]
        if (timer != null) {
            val duration = sample.stop(timer)
            logger.debug("Operācija '$operationName' pabeigta: ${duration / 1_000_000}ms")
        }
    }

    /**
     * Reģistrē datubāzes operācijas metriku
     * @param operation Operācijas tips (SELECT, INSERT, UPDATE, DELETE)
     * @param table Tabulas nosaukums
     */
    fun recordDatabaseOperation(operation: String, table: String) {
        logger.debug("Reģistrē datubāzes operāciju: $operation uz $table")

        val counter = counters.computeIfAbsent("warehouse.database.$operation.$table") {
            Counter.builder("warehouse.database.operations")
                .description("Datubāzes operāciju skaits")
                .tag("operation", operation)
                .tag("table", table)
                .register(registry)
        }

        counter.increment()
    }

    /**
     * Reģistrē kļūdas metriku
     * @param errorType Kļūdas tips
     * @param component Komponente, kurā radās kļūda
     */
    fun recordError(errorType: String, component: String) {
        logger.debug("Reģistrē kļūdu: $errorType komponente $component")

        val counter = counters.computeIfAbsent("warehouse.errors.$errorType.$component") {
            Counter.builder("warehouse.errors")
                .description("Sistēmas kļūdu skaits")
                .tag("type", errorType)
                .tag("component", component)
                .register(registry)
        }

        counter.increment()
    }

    /**
     * Atjaunina aktīvo lietotāju skaita metriku
     * @param count Aktīvo lietotāju skaits
     */
    fun updateActiveUsers(count: Int) {
        logger.debug("Atjaunina aktīvo lietotāju skaitu: $count")

        val gaugeKey = "warehouse.users.active"
        val activeUsers = gaugeValues.computeIfAbsent(gaugeKey) {
            val atomic = AtomicLong(count.toLong())
            registry.gauge("warehouse.users.active", atomic) { it.get().toDouble() }
            atomic
        }

        activeUsers.set(count.toLong())
    }

    /**
     * Reģistrē sistēmas resursu izmantošanas metriku
     * @param resourceType Resursa tips (CPU, MEMORY, DISK)
     * @param usage Izmantošanas procents
     */
    fun recordResourceUsage(resourceType: String, usage: Double) {
        logger.debug("Reģistrē resursa izmantošanu: $resourceType = $usage%")

        val gaugeKey = "warehouse.system.resources.$resourceType"
        val resourceUsage = gaugeValues.computeIfAbsent(gaugeKey) {
            val atomic = AtomicLong((usage * 100).toLong())
            registry.gauge("warehouse.system.resources",
                listOf(io.micrometer.core.instrument.Tag.of("type", resourceType)),
                atomic) { it.get().toDouble() / 100 }
            atomic
        }

        resourceUsage.set((usage * 100).toLong())
    }

    /**
     * Iegūst metriku statistiku
     * @return Metriku kopsavilkums
     */
    fun getMetricsSummary(): Map<String, Any> {
        return mapOf(
            "timestamp" to LocalDateTime.now(),
            "counters" to counters.keys.size,
            "gauges" to gaugeValues.keys.size,
            "timers" to timers.keys.size,
            "registeredMetrics" to registry.meters.size
        )
    }
}