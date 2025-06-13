-- =============================================================================
-- TESTU DATUBĀZES SHĒMAS FAILS (H2 COMPATIBLE)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- NOLIKTAVU TABULA
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS warehouses (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity DOUBLE NOT NULL CHECK (capacity > 0),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Indeksi atsevišķi
CREATE INDEX IF NOT EXISTS idx_warehouse_name ON warehouses(name);
CREATE INDEX IF NOT EXISTS idx_warehouse_location ON warehouses(location);

-- -----------------------------------------------------------------------------
-- PRODUKTU TABULA
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    barcode VARCHAR(13),
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    weight DECIMAL(8,3),
    dimensions VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Indeksi atsevišķi
CREATE INDEX IF NOT EXISTS idx_product_barcode ON products(barcode);
CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_product_active ON products(is_active);

-- -----------------------------------------------------------------------------
-- KRĀJUMU VIENĪBU TABULA
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory_items (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               product_id BIGINT NOT NULL,
                                               warehouse_id BIGINT NOT NULL,
                                               quantity DECIMAL(10,2) NOT NULL CHECK (quantity >= 0),
    minimum_level DECIMAL(10,2),
    maximum_level DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,

    CONSTRAINT uk_product_warehouse UNIQUE (product_id, warehouse_id),
    CHECK (maximum_level IS NULL OR minimum_level IS NULL OR maximum_level >= minimum_level)
    );

-- Indeksi atsevišķi
CREATE INDEX IF NOT EXISTS idx_inventory_product ON inventory_items(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse ON inventory_items(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventory_quantity ON inventory_items(quantity);

-- -----------------------------------------------------------------------------
-- TRANSAKCIJU TABULA
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            product_id BIGINT NOT NULL,
                                            transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('RECEIPT', 'ISSUE', 'TRANSFER', 'ADJUSTMENT')),
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    source_warehouse_id BIGINT,
    destination_warehouse_id BIGINT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    user_id VARCHAR(50),
    reference_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (source_warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (destination_warehouse_id) REFERENCES warehouses(id)
    );

-- Indeksi atsevišķi
CREATE INDEX IF NOT EXISTS idx_transaction_product ON transactions(product_id);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transaction_source ON transactions(source_warehouse_id);
CREATE INDEX IF NOT EXISTS idx_transaction_destination ON transactions(destination_warehouse_id);
CREATE INDEX IF NOT EXISTS idx_transaction_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transaction_reference ON transactions(reference_number);

-- -----------------------------------------------------------------------------
-- AUDITA TABULA
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    operation VARCHAR(20) NOT NULL,
    user_id VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_values TEXT,
    new_values TEXT,
    details TEXT
    );

-- Indeksi atsevišķi
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_operation ON audit_logs(operation);