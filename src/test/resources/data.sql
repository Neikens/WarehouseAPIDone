-- Simple test data for integration tests
-- Only basic warehouse and product data

-- Insert test warehouses
INSERT INTO warehouses (name, location, capacity, description, created_at) VALUES
                                                                               ('Test Warehouse 1', 'Riga, Latvia', 1000.0, 'Primary test warehouse', CURRENT_TIMESTAMP),
                                                                               ('Test Warehouse 2', 'Daugavpils, Latvia', 500.0, 'Secondary test warehouse', CURRENT_TIMESTAMP);

-- Insert test products
INSERT INTO products (code, name, description, category, price, is_active, created_at, updated_at) VALUES
                                                                                                       ('TEST-001', 'Test Product 1', 'Test product description', 'Electronics', 99.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                       ('TEST-002', 'Test Product 2', 'Another test product', 'Office', 49.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert basic inventory items
INSERT INTO inventory_items (product_id, warehouse_id, quantity, created_at, updated_at) VALUES
                                                                                             (1, 1, 10.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                             (2, 1, 5.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);