#!/bin/bash

# API testēšanas skripts noliktavas pārvaldības sistēmai
# Veic pilnu API funkcionalitātes pārbaudi

BASE_URL="http://localhost:8080"
AUTH_HEADER="Authorization: Basic YWRtaW46YWRtaW4xMjM="

echo "=== Sāk API testus ==="

# Funkcija atbildes formatētai izvadei
print_response() {
    echo "Atbilde:"
    echo "$1" | python3 -m json.tool 2>/dev/null || echo "$1"
    echo "------------------------"
}

# Tests 1: Sistēmas veselības pārbaude
echo -e "\n1. Testē sistēmas veselības stāvokli..."
RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/health")
print_response "$RESPONSE"

# Tests 2: Noliktavas izveide
echo -e "\n2. Izveido testa noliktavu..."
WAREHOUSE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/warehouses" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "name": "Testa Noliktava",
    "location": "Testa Atrašanās vieta",
    "capacity": 1000
}')
print_response "$WAREHOUSE_RESPONSE"

# Izvelk noliktavas ID turpmākai izmantošanai
WAREHOUSE_ID=$(echo "$WAREHOUSE_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)
echo "Izveidotās noliktavas ID: $WAREHOUSE_ID"

# Tests 3: Produkta izveide
echo -e "\n3. Izveido testa produktu..."
PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/products" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "code": "TEST001",
    "description": "Testa Produkts",
    "barcode": "123456789",
    "category": "Testa Kategorija",
    "name": "Testa Produkts",
    "price": 10.50
}')
print_response "$PRODUCT_RESPONSE"

# Izvelk produkta ID turpmākai izmantošanai
PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)
echo "Izveidotā produkta ID: $PRODUCT_ID"

# Tests 4: Transakcijas izveide (tikai ja ir derīgi ID)
if [ ! -z "$PRODUCT_ID" ] && [ ! -z "$WAREHOUSE_ID" ] && [ "$PRODUCT_ID" != "null" ] && [ "$WAREHOUSE_ID" != "null" ]; then
    echo -e "\n4. Izveido testa transakciju..."
    TRANSACTION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/transactions" \
    -H "$AUTH_HEADER" \
    -H "Content-Type: application/json" \
    -d "{
        \"productId\": $PRODUCT_ID,
        \"sourceWarehouseId\": $WAREHOUSE_ID,
        \"quantity\": 10,
        \"transactionType\": \"RECEIPT\"
    }")
    print_response "$TRANSACTION_RESPONSE"
else
    echo -e "\n4. Izlaiž transakcijas testu - nav derīgu ID"
fi

# Tests 5: Visu produktu iegūšana
echo -e "\n5. Iegūst visus produktus..."
PRODUCTS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/products" \
-H "$AUTH_HEADER")
print_response "$PRODUCTS_RESPONSE"

# Tests 6: Visu noliktavu iegūšana
echo -e "\n6. Iegūst visas noliktavas..."
WAREHOUSES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/warehouses" \
-H "$AUTH_HEADER")
print_response "$WAREHOUSES_RESPONSE"

# Tests 7: Visu transakciju iegūšana
echo -e "\n7. Iegūst visas transakcijas..."
TRANSACTIONS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/transactions" \
-H "$AUTH_HEADER")
print_response "$TRANSACTIONS_RESPONSE"

# Tests 8: Sistēmas metriku pārbaude
echo -e "\n8. Pārbauda sistēmas metrikas..."
METRICS_RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics" \
-H "$AUTH_HEADER")
print_response "$METRICS_RESPONSE"

echo -e "\n=== API testi pabeigti ==="