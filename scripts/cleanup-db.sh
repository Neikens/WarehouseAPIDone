#!/bin/bash

echo "Cleaning up PostgreSQL database..."

PGPASSWORD=parole123 psql -U admin1 -d warehouse_db << EOF
DO \$\$ 
BEGIN
    -- Disable triggers temporarily
    SET session_replication_role = 'replica';
    
    -- Truncate all tables
    TRUNCATE TABLE transactions CASCADE;
    TRUNCATE TABLE inventory_items CASCADE;
    TRUNCATE TABLE products CASCADE;
    TRUNCATE TABLE warehouses CASCADE;
    
    -- Reset sequences
    ALTER SEQUENCE transactions_id_seq RESTART WITH 1;
    ALTER SEQUENCE products_id_seq RESTART WITH 1;
    ALTER SEQUENCE warehouses_id_seq RESTART WITH 1;
    
    -- Re-enable triggers
    SET session_replication_role = 'origin';
END \$\$;
EOF

if [ $? -eq 0 ]; then
    echo "Database cleanup completed successfully!"
else
    echo "Error during database cleanup!"
    exit 1
fi
