#!/bin/bash

BASE_URL="http://localhost:8080"
AUTH_HEADER="Authorization: Basic YWRtaW46YWRtaW4xMjM="

echo "=== Starting API Tests ==="

# Function to print response with formatting
print_response() {
    echo "Response:"
    echo "$1" | python3 -m json.tool 2>/dev/null || echo "$1"
    echo "------------------------"
}

# Test 1: Health Check
echo -e "\n1. Testing Health Check..."
RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/health")
print_response "$RESPONSE"

# Test 2: Create Warehouse
echo -e "\n2. Creating Test Warehouse..."
WAREHOUSE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/warehouses" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "name": "Test Warehouse",
    "location": "Test Location",
    "capacity": 1000
}')
print_response "$WAREHOUSE_RESPONSE"
WAREHOUSE_ID=$(echo "$WAREHOUSE_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)

# Test 3: Create Product
echo -e "\n3. Creating Test Product..."
PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/products" \
-H "$AUTH_HEADER" \
-H "Content-Type: application/json" \
-d '{
    "code": "TEST001",
    "description": "Test Product",
    "barcode": "123456789",
    "category": "Test Category"
}')
print_response "$PRODUCT_RESPONSE"
PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)

# Test 4: Create Transaction
echo -e "\n4. Creating Test Transaction..."
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

# Test 5: Get All Products
echo -e "\n5. Getting All Products..."
PRODUCTS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/products" \
-H "$AUTH_HEADER")
print_response "$PRODUCTS_RESPONSE"

# Test 6: Get All Warehouses
echo -e "\n6. Getting All Warehouses..."
WAREHOUSES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/warehouses" \
-H "$AUTH_HEADER")
print_response "$WAREHOUSES_RESPONSE"

# Test 7: Get All Transactions
echo -e "\n7. Getting All Transactions..."
TRANSACTIONS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/transactions" \
-H "$AUTH_HEADER")
print_response "$TRANSACTIONS_RESPONSE"

# Test 8: Check Metrics
echo -e "\n8. Checking System Metrics..."
METRICS_RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics" \
-H "$AUTH_HEADER")
print_response "$METRICS_RESPONSE"

echo -e "\n=== API Tests Complete ==="
