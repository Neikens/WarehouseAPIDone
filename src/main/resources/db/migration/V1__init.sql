CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          code VARCHAR(50) UNIQUE NOT NULL,
                          description TEXT,
                          barcode VARCHAR(100),
                          category VARCHAR(100),
                          quantity INTEGER DEFAULT 0,
                          price DECIMAL(10,2) DEFAULT 0.00,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE warehouses (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            location VARCHAR(255) NOT NULL,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              transaction_type VARCHAR(20) NOT NULL,
                              product_id BIGINT REFERENCES products(id),
                              source_warehouse_id BIGINT REFERENCES warehouses(id),
                              destination_warehouse_id BIGINT REFERENCES warehouses(id),
                              quantity DECIMAL(10,2) NOT NULL,
                              timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('RECEIPT', 'TRANSFER', 'ISSUE'))
);

CREATE TABLE inventory_items (
                                 id BIGSERIAL PRIMARY KEY,
                                 product_id BIGINT REFERENCES products(id),
                                 warehouse_id BIGINT REFERENCES warehouses(id),
                                 quantity INTEGER DEFAULT 0,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT unique_product_warehouse UNIQUE (product_id, warehouse_id)
);