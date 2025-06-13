#!/bin/bash

echo "Notīra PostgreSQL datubāzi..."

# PostgreSQL datubāzes notīrīšanas skripts
# Izmanto PGPASSWORD vides mainīgo drošai autentifikācijai
PGPASSWORD=parole123 psql -U admin1 -d warehouse_db << EOF
DO \$\$
BEGIN
    -- Īslaicīgi atslēdz triggerus, lai izvairītos no ārējās atslēgas kļūdām
    SET session_replication_role = 'replica';

    -- Notīra visas tabulas, saglabājot struktūru
    -- CASCADE nodrošina, ka tiek dzēsti arī saistītie ieraksti
    TRUNCATE TABLE transactions CASCADE;
    TRUNCATE TABLE inventory_items CASCADE;
    TRUNCATE TABLE products CASCADE;
    TRUNCATE TABLE warehouses CASCADE;

    -- Atjauno secību skaitītājus no 1
    ALTER SEQUENCE transactions_id_seq RESTART WITH 1;
    ALTER SEQUENCE products_id_seq RESTART WITH 1;
    ALTER SEQUENCE warehouses_id_seq RESTART WITH 1;

    -- Atjauno triggerus
    SET session_replication_role = 'origin';

    RAISE NOTICE 'Datubāzes notīrīšana pabeigta veiksmīgi';
END \$\$;
EOF

# Pārbauda, vai komanda izpildījās veiksmīgi
if [ $? -eq 0 ]; then
    echo "Datubāzes notīrīšana pabeigta veiksmīgi!"
else
    echo "Kļūda datubāzes notīrīšanas laikā!"
    exit 1
fi