package com.warehouse.api.exception

/**
 * Validācijas izņēmuma klase
 * Tiek izmesta, kad dati neatbilst validācijas prasībām
 */
class ValidationException(message: String) : RuntimeException(message)