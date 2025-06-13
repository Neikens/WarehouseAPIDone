package com.warehouse.api.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * Veiktspējas monitoringa aspekts
 * Izmanto AOP (Aspect-Oriented Programming) lai mērītu metožu izpildes laiku
 */
@Aspect
@Component
class PerformanceMonitoringAspect {

    private val logger = LoggerFactory.getLogger(PerformanceMonitoringAspect::class.java)

    /**
     * Mēra visu servisa slāņa metožu izpildes laiku
     * @Around anotācija nozīmē, ka šī metode tiek izpildīta pirms un pēc mērķa metodes
     */
    @Around("execution(* com.warehouse.api.service.*.*(..))")
    fun measureMethodExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val start = Instant.now()
        val methodName = "${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name}"

        try {
            // Izpilda oriģinālo metodi
            val result = joinPoint.proceed()
            val finish = Instant.now()
            val timeElapsed = Duration.between(start, finish)

            // Logē veiksmīgu izpildi
            logger.info("Metode '$methodName' izpildīta {} ms laikā", timeElapsed.toMillis())
            return result

        } catch (e: Exception) {
            val finish = Instant.now()
            val timeElapsed = Duration.between(start, finish)

            // Logē kļūdu ar izpildes laiku
            logger.error("Kļūda metodē '$methodName' pēc {} ms: {}", timeElapsed.toMillis(), e.message)
            throw e
        }
    }

    /**
     * Papildu monitorings kontrolleriem (kritiskākām operācijām)
     */
    @Around("execution(* com.warehouse.api.controller.*.*(..))")
    fun measureControllerExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val start = Instant.now()
        val methodName = "${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name}"

        try {
            val result = joinPoint.proceed()
            val finish = Instant.now()
            val timeElapsed = Duration.between(start, finish)

            // Brīdina, ja kontrollera metode aizņem pārāk daudz laika
            if (timeElapsed.toMillis() > 1000) {
                logger.warn("Lēna kontrollera metode '$methodName': {} ms", timeElapsed.toMillis())
            } else {
                logger.debug("Kontrollera metode '$methodName': {} ms", timeElapsed.toMillis())
            }

            return result
        } catch (e: Exception) {
            logger.error("Kļūda kontrollera metodē '$methodName': {}", e.message)
            throw e
        }
    }
}