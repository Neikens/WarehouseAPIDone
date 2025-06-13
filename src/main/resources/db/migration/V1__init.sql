-- Noliktavu pārvaldības sistēmas sākotnējā datubāzes struktūra
-- Izveido galvenās tabulas produktiem, noliktavām, transakcijām un krājumiem

-- Produktu tabula - glabā informāciju par visiem produktiem sistēmā
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          code VARCHAR(50) UNIQUE NOT NULL,           -- Unikāls produkta kods
                          description TEXT,                           -- Produkta apraksts
                          barcode VARCHAR(100),                       -- Produkta svītrkods
                          category VARCHAR(100) NOT NULL,             -- Produkta kategorija
                          name VARCHAR(255) NOT NULL,                 -- Produkta nosaukums
                          price DECIMAL(10,2) NOT NULL DEFAULT 0.00,  -- Produkta cena
                          weight DECIMAL(8,3),                        -- Produkta svars kilogramos
                          dimensions VARCHAR(50),                     -- Produkta izmēri (garums x platums x augstums)
                          is_active BOOLEAN NOT NULL DEFAULT true,    -- Vai produkts ir aktīvs
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Noliktavu tabula - glabā informāciju par visām noliktavām
CREATE TABLE warehouses (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,                 -- Noliktavas nosaukums
                            location VARCHAR(255) NOT NULL,             -- Noliktavas atrašanās vieta
                            capacity DECIMAL(10,2) NOT NULL,            -- Noliktavas kapacitāte
                            description TEXT,                           -- Noliktavas apraksts
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Transakciju tabula - reģistrē visas krājumu kustības
CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              transaction_type VARCHAR(20) NOT NULL,      -- Transakcijas tips (RECEIPT, TRANSFER, ISSUE)
                              product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
                              source_warehouse_id BIGINT REFERENCES warehouses(id) ON DELETE RESTRICT,      -- Avota noliktava
                              destination_warehouse_id BIGINT REFERENCES warehouses(id) ON DELETE RESTRICT, -- Galamērķa noliktava
                              quantity DECIMAL(10,2) NOT NULL,            -- Transakcijas daudzums
                              timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              description VARCHAR(500),                   -- Transakcijas apraksts
                              user_id VARCHAR(50),                        -- Lietotājs, kurš veica transakciju
                              reference_number VARCHAR(100),              -- Atsauces numurs

    -- Pārbauda, ka transakcijas tips ir derīgs
                              CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('RECEIPT', 'TRANSFER', 'ISSUE')),

    -- Pārbauda, ka daudzums ir pozitīvs
                              CONSTRAINT chk_positive_quantity CHECK (quantity > 0)
);

-- Krājumu tabula - glabā pašreizējos krājumus katrā noliktavā
CREATE TABLE inventory_items (
                                 id BIGSERIAL PRIMARY KEY,
                                 product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
                                 warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
                                 quantity DECIMAL(10,2) NOT NULL DEFAULT 0,  -- Pašreizējais daudzums
                                 minimum_level DECIMAL(10,2),                -- Minimālais krājumu līmenis
                                 maximum_level DECIMAL(10,2),                -- Maksimālais krājumu līmenis
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Nodrošina, ka katrs produkts var būt tikai vienreiz katrā noliktavā
                                 CONSTRAINT unique_product_warehouse UNIQUE (product_id, warehouse_id),

    -- Pārbauda, ka daudzums nav negatīvs
                                 CONSTRAINT chk_non_negative_quantity CHECK (quantity >= 0),

    -- Pārbauda, ka minimālais līmenis nav negatīvs
                                 CONSTRAINT chk_non_negative_minimum CHECK (minimum_level IS NULL OR minimum_level >= 0),

    -- Pārbauda, ka maksimālais līmenis ir lielāks par minimālo
                                 CONSTRAINT chk_maximum_greater_than_minimum CHECK (
                                     maximum_level IS NULL OR minimum_level IS NULL OR maximum_level >= minimum_level
                                     )
);

-- Indeksi produktu tabulai (uzlabo vaicājumu veiktspēju)
CREATE INDEX idx_product_code ON products(code);
CREATE INDEX idx_product_barcode ON products(barcode);
CREATE INDEX idx_product_category ON products(category);
CREATE INDEX idx_product_active ON products(is_active);

-- Indeksi noliktavu tabulai
CREATE INDEX idx_warehouse_name ON warehouses(name);
CREATE INDEX idx_warehouse_location ON warehouses(location);

-- Indeksi transakciju tabulai
CREATE INDEX idx_transaction_timestamp ON transactions(timestamp);
CREATE INDEX idx_transaction_product ON transactions(product_id);
CREATE INDEX idx_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_transaction_source_warehouse ON transactions(source_warehouse_id);
CREATE INDEX idx_transaction_destination_warehouse ON transactions(destination_warehouse_id);

-- Indeksi krājumu tabulai
CREATE INDEX idx_inventory_product ON inventory_items(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory_items(warehouse_id);
CREATE INDEX idx_inventory_quantity ON inventory_items(quantity);

-- Komentāri tabulām (PostgreSQL specifisks)
COMMENT ON TABLE products IS 'Produktu informācijas tabula';
COMMENT ON TABLE warehouses IS 'Noliktavu informācijas tabula';
COMMENT ON TABLE transactions IS 'Krājumu transakciju tabula';
COMMENT ON TABLE inventory_items IS 'Pašreizējo krājumu tabula';

-- Komentāri svarīgākajiem laukiem
COMMENT ON COLUMN products.code IS 'Unikāls produkta identifikators biznesa vajadzībām';
COMMENT ON COLUMN transactions.transaction_type IS 'RECEIPT=saņemšana, TRANSFER=pārvietošana, ISSUE=izdošana';
COMMENT ON COLUMN inventory_items.quantity IS 'Pašreizējais produkta daudzums noliktavā';