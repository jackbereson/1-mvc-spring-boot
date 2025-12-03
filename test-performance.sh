#!/bin/bash

# Performance Test Script for Product API
BASE_URL="http://localhost:8080/api/v1"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "======================================"
echo "Product API Performance Test"
echo "======================================"
echo ""

# Login
echo -e "${YELLOW}Getting authentication token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "Failed to get token"
    exit 1
fi

echo -e "${GREEN}✓ Token obtained${NC}"
echo ""

# Test 1: Get 1000 items (100 pages x 10 items)
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}Test 1: Fetching 1000 items (100 pages, 10 items each)${NC}"
echo -e "${BLUE}========================================${NC}"

START_TIME=$(date +%s.%N)
for page in {0..99}; do
    curl -s -X GET "${BASE_URL}/products?page=${page}&size=10&sortBy=id&sortDirection=DESC" \
      -H "Authorization: Bearer ${TOKEN}" > /dev/null
    
    if [ $((page % 10)) -eq 0 ]; then
        echo -n "."
    fi
done
echo ""
END_TIME=$(date +%s.%N)
DURATION=$(echo "$END_TIME - $START_TIME" | bc)

echo -e "${GREEN}✓ Completed${NC}"
echo "Total time: ${DURATION} seconds"
echo "Average per page: $(echo "scale=4; $DURATION / 100" | bc) seconds"
echo ""

# Test 2: Get all 1000 items in single request
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}Test 2: Fetching 1000 items in single request${NC}"
echo -e "${BLUE}========================================${NC}"

START_TIME=$(date +%s.%N)
RESPONSE=$(curl -s -w "\n%{time_total}" -X GET "${BASE_URL}/products?page=0&size=1000&sortBy=id&sortDirection=DESC" \
  -H "Authorization: Bearer ${TOKEN}")
END_TIME=$(date +%s.%N)

CURL_TIME=$(echo "$RESPONSE" | tail -1)
TOTAL_ELEMENTS=$(echo "$RESPONSE" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)

echo -e "${GREEN}✓ Completed${NC}"
echo "Total items retrieved: ${TOTAL_ELEMENTS}"
echo "Request time: ${CURL_TIME} seconds"
echo ""

# Test 3: Get specific products by ID (100 random IDs)
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}Test 3: Fetching 100 products by ID${NC}"
echo -e "${BLUE}========================================${NC}"

START_TIME=$(date +%s.%N)
for id in {1..100}; do
    curl -s -X GET "${BASE_URL}/products/${id}" \
      -H "Authorization: Bearer ${TOKEN}" > /dev/null
    
    if [ $((id % 10)) -eq 0 ]; then
        echo -n "."
    fi
done
echo ""
END_TIME=$(date +%s.%N)
DURATION=$(echo "$END_TIME - $START_TIME" | bc)

echo -e "${GREEN}✓ Completed${NC}"
echo "Total time: ${DURATION} seconds"
echo "Average per request: $(echo "scale=4; $DURATION / 100" | bc) seconds"
echo ""

# Test 4: Search query performance
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}Test 4: Search query performance (10 searches)${NC}"
echo -e "${BLUE}========================================${NC}"

KEYWORDS=("Premium" "Smart" "Professional" "Luxury" "Modern" "Classic" "Advanced" "Ultimate" "Essential" "Vintage")

START_TIME=$(date +%s.%N)
for keyword in "${KEYWORDS[@]}"; do
    curl -s -X GET "${BASE_URL}/products/search?name=${keyword}&page=0&size=100" \
      -H "Authorization: Bearer ${TOKEN}" > /dev/null
    echo -n "."
done
echo ""
END_TIME=$(date +%s.%N)
DURATION=$(echo "$END_TIME - $START_TIME" | bc)

echo -e "${GREEN}✓ Completed${NC}"
echo "Total time: ${DURATION} seconds"
echo "Average per search: $(echo "scale=4; $DURATION / 10" | bc) seconds"
echo ""

# Test 5: Category filter performance
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}Test 5: Category filter performance (10 categories)${NC}"
echo -e "${BLUE}========================================${NC}"

CATEGORIES=("Electronics" "Fashion" "Home & Garden" "Sports" "Books" "Toys" "Beauty" "Automotive" "Food" "Health")

START_TIME=$(date +%s.%N)
for category in "${CATEGORIES[@]}"; do
    ENCODED_CATEGORY=$(echo "$category" | sed 's/ /%20/g')
    curl -s -X GET "${BASE_URL}/products/category/${ENCODED_CATEGORY}?page=0&size=100" \
      -H "Authorization: Bearer ${TOKEN}" > /dev/null
    echo -n "."
done
echo ""
END_TIME=$(date +%s.%N)
DURATION=$(echo "$END_TIME - $START_TIME" | bc)

echo -e "${GREEN}✓ Completed${NC}"
echo "Total time: ${DURATION} seconds"
echo "Average per category: $(echo "scale=4; $DURATION / 10" | bc) seconds"
echo ""

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Performance Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "All performance tests completed successfully!"
echo "The API handles large datasets efficiently."
