package com.warehouse.api.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Metriku konfigurācijas klase
 * Konfigurē Micrometer metriku sistēmu aplikācijas monitoringam
 */
@Configuration
class MetricsConfig {

    /**
     * Izveido vienkāršu metriku reģistru
     * Ražošanas vidē varētu izmantot citus reģistrus (Prometheus, InfluxDB, utt.)
     */
    @Bean
    fun meterRegistry(): MeterRegistry {
        return SimpleMeterRegistry()
    }
}