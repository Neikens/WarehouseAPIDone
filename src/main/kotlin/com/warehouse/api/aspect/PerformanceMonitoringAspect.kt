package com.warehouse.api.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Aspect
@Component
class PerformanceMonitoringAspect {
    private val logger = LoggerFactory.getLogger(PerformanceMonitoringAspect::class.java)

    @Around("execution(* com.warehouse.api.service.*.*(..))")
    fun measureMethodExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val start = Instant.now()

        try {
            val result = joinPoint.proceed()
            val finish = Instant.now()
            val timeElapsed = Duration.between(start, finish)
            logger.info("Method ${joinPoint.signature.name} executed in ${timeElapsed.toMillis()} ms")
            return result
        } catch (e: Exception) {
            logger.error("Exception in ${joinPoint.signature.name}: ${e.message}")
            throw e
        }
    }
}
