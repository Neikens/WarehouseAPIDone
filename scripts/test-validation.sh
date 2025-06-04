#!/bin/bash

BASE_URL="http://localhost:8080"
AUTH_HEADER="Authorization: Basic YWRtaW46YWRtaW4xMjM="

echo "=== Starting Validation Tests ==="

# Function to print response with formatting
print_response() {
    echo "Response:"
    echo "$1" | python3 -m json.tool 2>/dev/null || echo "$1"
    echo "------------------------"
}

# Test 1: Invalid Product Creation
echo -e "\n1. Testing Invalid Product Creation..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/products" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "code": "",
    "description": "",
    "category": ""
}')
print_response "$RESPONSE"

# Test 2: Invalid Warehouse Creation
echo -e "\n2. Testing Invalid Warehouse Creation..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/warehouses" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "name": "",
    "capacity": -1
}')
print_response "$RESPONSE"

# Test 3: Invalid Authentication
echo -e "\n3. Testing Invalid Authentication..."
RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/products" \
-H "Authorization: Basic invalid:credentials")
print_response "$RESPONSE"

# Test 4: Invalid Transaction
echo -e "\n4. Testing Invalid Transaction..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/transactions" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "productId": 999999,
    "sourceWarehouseId": 999999,
    "quantity": -1,
    "transactionType": "INVALID"
}')
print_response "$RESPONSE"

echo -e "\n=== Validation Tests Complete ==="
