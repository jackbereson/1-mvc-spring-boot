#!/bin/bash

# Product API Test Script
# Make sure the application is running before executing this script

BASE_URL="http://localhost:8080/api/v1"
TOKEN=""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "Product API Test Script"
echo "======================================"
echo ""

# Function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Step 1: Login to get token
echo -e "${YELLOW}Step 1: Login to get authentication token${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}Failed to get authentication token. Make sure admin user exists.${NC}"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

print_result 0 "Login successful"
echo "Token: ${TOKEN:0:50}..."
echo ""

# Step 2: Get all products (paginated)
echo -e "${YELLOW}Step 2: Get all products (first page, 10 items)${NC}"
ALL_PRODUCTS=$(curl -s -X GET "${BASE_URL}/products?page=0&size=10&sortBy=id&sortDirection=DESC" \
  -H "Authorization: Bearer ${TOKEN}")

TOTAL_ELEMENTS=$(echo $ALL_PRODUCTS | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
echo "Response: $ALL_PRODUCTS" | jq '.' 2>/dev/null || echo "$ALL_PRODUCTS"
print_result 0 "Retrieved products - Total: $TOTAL_ELEMENTS"
echo ""

# Step 3: Get product by ID
echo -e "${YELLOW}Step 3: Get product by ID (ID: 1)${NC}"
PRODUCT_BY_ID=$(curl -s -X GET "${BASE_URL}/products/1" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Response: $PRODUCT_BY_ID" | jq '.' 2>/dev/null || echo "$PRODUCT_BY_ID"
print_result $? "Retrieved product by ID"
echo ""

# Step 4: Search products by name
echo -e "${YELLOW}Step 4: Search products by name (keyword: 'Premium')${NC}"
SEARCH_RESULT=$(curl -s -X GET "${BASE_URL}/products/search?name=Premium&page=0&size=5" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Response: $SEARCH_RESULT" | jq '.' 2>/dev/null || echo "$SEARCH_RESULT"
print_result $? "Search products by name"
echo ""

# Step 5: Get products by category
echo -e "${YELLOW}Step 5: Get products by category (Electronics)${NC}"
CATEGORY_PRODUCTS=$(curl -s -X GET "${BASE_URL}/products/category/Electronics?page=0&size=5" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Response: $CATEGORY_PRODUCTS" | jq '.' 2>/dev/null || echo "$CATEGORY_PRODUCTS"
print_result $? "Retrieved products by category"
echo ""

# Step 6: Create new product
echo -e "${YELLOW}Step 6: Create new product${NC}"
CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/products" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product - API Test",
    "description": "This is a test product created via API test script",
    "price": 99.99,
    "category": "Electronics",
    "thumbnailUrl": "https://picsum.photos/400/300?random=test",
    "isActive": true
  }')

NEW_PRODUCT_ID=$(echo $CREATE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Response: $CREATE_RESPONSE" | jq '.' 2>/dev/null || echo "$CREATE_RESPONSE"
print_result $? "Created new product - ID: $NEW_PRODUCT_ID"
echo ""

# Step 7: Update product
if [ ! -z "$NEW_PRODUCT_ID" ]; then
    echo -e "${YELLOW}Step 7: Update product (ID: $NEW_PRODUCT_ID)${NC}"
    UPDATE_RESPONSE=$(curl -s -X PUT "${BASE_URL}/products/${NEW_PRODUCT_ID}" \
      -H "Authorization: Bearer ${TOKEN}" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Test Product - Updated",
        "description": "This product has been updated",
        "price": 149.99,
        "category": "Electronics",
        "thumbnailUrl": "https://picsum.photos/400/300?random=updated",
        "isActive": true
      }')

    echo "Response: $UPDATE_RESPONSE" | jq '.' 2>/dev/null || echo "$UPDATE_RESPONSE"
    print_result $? "Updated product"
    echo ""

    # Step 8: Delete product
    echo -e "${YELLOW}Step 8: Delete product (ID: $NEW_PRODUCT_ID)${NC}"
    DELETE_RESPONSE=$(curl -s -X DELETE "${BASE_URL}/products/${NEW_PRODUCT_ID}" \
      -H "Authorization: Bearer ${TOKEN}")

    echo "Response: $DELETE_RESPONSE" | jq '.' 2>/dev/null || echo "$DELETE_RESPONSE"
    print_result $? "Deleted product"
    echo ""
fi

# Step 9: Test pagination with different page sizes
echo -e "${YELLOW}Step 9: Test pagination (page 2, size 20)${NC}"
PAGE_TEST=$(curl -s -X GET "${BASE_URL}/products?page=2&size=20&sortBy=price&sortDirection=ASC" \
  -H "Authorization: Bearer ${TOKEN}")

PAGE_NUMBER=$(echo $PAGE_TEST | grep -o '"number":[0-9]*' | cut -d':' -f2)
PAGE_SIZE=$(echo $PAGE_TEST | grep -o '"size":[0-9]*' | cut -d':' -f2)
echo "Page: $PAGE_NUMBER, Size: $PAGE_SIZE"
print_result $? "Pagination test"
echo ""

# Summary
echo "======================================"
echo -e "${GREEN}All tests completed!${NC}"
echo "======================================"
echo ""
echo "Summary:"
echo "- Total products in database: $TOTAL_ELEMENTS"
echo "- Test product created and deleted: ID $NEW_PRODUCT_ID"
echo ""
echo "You can now test the API manually using the following token:"
echo "$TOKEN"
