#!/bin/bash

echo "=== Starting All Tests ==="

# Clean and build the project
echo -e "\nCleaning and building project..."
./gradlew clean build -x test

# Run unit tests
echo -e "\nRunning unit tests..."
./gradlew test --info

# Generate test coverage report
echo -e "\nGenerating test coverage report..."
./gradlew jacocoTestReport

# Print test results location
echo -e "\nTest results can be found at:"
echo "Build reports: build/reports/tests/test/index.html"
echo "Coverage reports: build/reports/jacoco/test/html/index.html"

echo -e "\n=== All Tests Complete ==="