#!/bin/bash

# Validācijas testu skripts
# Pārbauda API kļūdu apstrādi un validācijas loģiku

BASE_URL="http://localhost:8080"
AUTH_HEADER="Authorization: Basic YWRtaW46YWRtaW4xMjM="

echo "=== Sāk validācijas testus ==="

# Funkcija atbildes formatētai izvadei
print_response() {
    echo "Atbilde:"
    echo "$1" | python3 -m json.tool 2>/dev/null || echo "$1"
    echo "------------------------"
}

# Tests 1: Nederīga produkta izveide (tukši lauki)
echo -e "\n1. Testē nederīgu produkta izveidi..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/products" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "code": "",
    "description": "",
    "category": "",
    "name": "",
    "price": -1
}')
print_response "$RESPONSE"

# Tests 2: Nederīga noliktavas izveide (negatīva kapacitāte)
echo -e "\n2. Testē nederīgu noliktavas izveidi..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/warehouses" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "name": "",
    "location": "",
    "capacity": -1
}')
print_response "$RESPONSE"

# Tests 3: Nederīga autentifikācija
echo -e "\n3. Testē nederīgu autentifikāciju..."
RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/products" \
-H "Authorization: Basic nederīgs:akreditācijas_dati")
print_response "$RESPONSE"

# Tests 4: Nederīga transakcija (neeksistējoši ID)
echo -e "\n4. Testē nederīgu transakciju..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/transactions" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "productId": 999999,
    "sourceWarehouseId": 999999,
    "quantity": -1,
    "transactionType": "NEDERĪGS_TIPS"
}')
print_response "$RESPONSE"

# Tests 5: Neeksistējoša resursa meklēšana
echo -e "\n5. Testē neeksistējoša produkta meklēšanu..."
RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/products/999999" \
-H "$AUTH_HEADER")
print_response "$RESPONSE"

echo -e "\n=== Validācijas testi pabeigti ==="