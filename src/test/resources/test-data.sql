-- =============================================================================
-- TESTU DATI (H2 COMPATIBLE)
-- =============================================================================

-- Noliktavu dati
INSERT INTO warehouses (id, name, location, capacity, description) VALUES
                                                                       (1, 'Galvenā noliktava', 'Rīga, Daugavgrīvas iela 114', 10000.0, 'Galvenā noliktava Rīgā'),
                                                                       (2, 'Reģionālā noliktava', 'Liepāja, Graudu iela 44', 5000.0, 'Reģionālā noliktava Liepājā'),
                                                                       (3, 'Mazā noliktava', 'Daugavpils, Rīgas iela 22A', 2000.0, 'Mazā noliktava Daugavpilī');

-- Produktu dati (ar is_active kolonnu)
INSERT INTO products (id, code, name, description, barcode, category, price, weight, dimensions, is_active) VALUES
                                                                                                                (1, 'PROD001', 'Laptop Dell XPS 13', 'Portatīvais dators Dell XPS 13', '1234567890123', 'Electronics', 1299.99, 1.2, '30x21x1.5', TRUE),
                                                                                                                (2, 'PROD002', 'Wireless Mouse', 'Bezvadu pele Logitech', '2345678901234', 'Electronics', 29.99, 0.1, '10x6x3', TRUE),
                                                                                                                (3, 'PROD003', 'Office Chair', 'Biroja krēsls ergonomisks', '3456789012345', 'Furniture', 199.99, 15.0, '60x60x120', TRUE);

-- Krājumu dati
INSERT INTO inventory_items (id, product_id, warehouse_id, quantity, minimum_level, maximum_level) VALUES
                                                                                                       (1, 1, 1, 50.0, 10.0, 100.0),
                                                                                                       (2, 1, 2, 25.0, 5.0, 50.0),
                                                                                                       (3, 2, 1, 200.0, 50.0, 500.0),
                                                                                                       (4, 2, 2, 100.0, 25.0, 250.0),
                                                                                                       (5, 3, 1, 30.0, 5.0, 50.0);

-- Transakciju dati
INSERT INTO transactions (id, product_id, transaction_type, quantity, destination_warehouse_id, description, user_id) VALUES
                                                                                                                          (1, 1, 'RECEIPT', 50.0, 1, 'Sākotnējais krājums', 'admin'),
                                                                                                                          (2, 2, 'RECEIPT', 200.0, 1, 'Sākotnējais krājums', 'admin'),
                                                                                                                          (3, 3, 'RECEIPT', 30.0, 1, 'Sākotnējais krājums', 'admin');