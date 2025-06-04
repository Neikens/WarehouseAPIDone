#!/bin/bash

echo "=== Running All Tests ==="

# Run unit tests
echo "Running unit tests..."
./gradlew test

# Run integration tests
echo "Running integration tests..."
./gradlew integrationTest

# Generate test reports
echo "Generating test reports..."
./gradlew jacocoTestReport

echo "=== Test Results ==="
echo "Test reports available in: build/reports/tests/test/index.html"
echo "Integration test reports available in: build/reports/tests/integrationTest/index.html"
echo "Coverage reports available in: build/reports/jacoco/test/html/index.html"
