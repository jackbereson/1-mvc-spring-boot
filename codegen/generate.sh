#!/bin/bash

# Spring Boot Entity Generator
# Usage: ./codegen/generate.sh EntityName

if [ -z "$1" ]; then
    echo "❌ Error: Entity name is required"
    echo ""
    echo "Usage: ./codegen/generate.sh EntityName"
    echo "Example: ./codegen/generate.sh Setting"
    exit 1
fi

ENTITY_NAME=$1
ENTITY_LOWER=$(echo "$ENTITY_NAME" | awk '{print tolower(substr($0,1,1)) substr($0,2)}')
TABLE_NAME=$(echo "$ENTITY_NAME" | sed 's/\(.\)\([A-Z]\)/\1_\2/g' | tr '[:upper:]' '[:lower:]')s

BASE_PACKAGE="com/coremvc"
BASE_PATH="src/main/java/$BASE_PACKAGE"
TEMPLATE_DIR="codegen/templates"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Counters for reporting
CREATED_COUNT=0
REPLACED_COUNT=0
SKIPPED_COUNT=0

echo ""
echo -e "${BLUE}🚀 Generating Spring Boot Entity: ${YELLOW}$ENTITY_NAME${NC}"
echo -e "${BLUE}📦 Package: ${NC}com.coremvc"
echo -e "${BLUE}📄 Table name: ${NC}$TABLE_NAME"
echo ""

# Function to generate file from template
generate_file() {
    local template_file=$1
    local output_file=$2
    local layer_name=$3
    
    if [ ! -f "$template_file" ]; then
        echo -e "${YELLOW}⚠️  Template not found: $template_file${NC}"
        return 1
    fi
    
    # Check if output file already exists
    if [ -f "$output_file" ]; then
        echo -e "${YELLOW}⚠️  File already exists: ${NC}$layer_name"
        read -p "   Replace it? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo -e "   ${BLUE}↪ Skipped${NC}"
            SKIPPED_COUNT=$((SKIPPED_COUNT + 1))
            return 0
        fi
        # Read template and replace placeholders
        sed -e "s/{{ENTITY_NAME}}/$ENTITY_NAME/g" \
            -e "s/{{ENTITY_LOWER}}/$ENTITY_LOWER/g" \
            -e "s/{{TABLE_NAME}}/$TABLE_NAME/g" \
            "$template_file" > "$output_file"
        echo -e "   ${GREEN}✓ Replaced${NC} $layer_name"
        REPLACED_COUNT=$((REPLACED_COUNT + 1))
    else
        # Read template and replace placeholders
        sed -e "s/{{ENTITY_NAME}}/$ENTITY_NAME/g" \
            -e "s/{{ENTITY_LOWER}}/$ENTITY_LOWER/g" \
            -e "s/{{TABLE_NAME}}/$TABLE_NAME/g" \
            "$template_file" > "$output_file"
        echo -e "${GREEN}✓${NC} Created $layer_name"
        CREATED_COUNT=$((CREATED_COUNT + 1))
    fi
}

# Generate each layer
generate_file "$TEMPLATE_DIR/Model.java.template" "$BASE_PATH/model/$ENTITY_NAME.java" "Model"
generate_file "$TEMPLATE_DIR/Dto.java.template" "$BASE_PATH/dto/${ENTITY_NAME}Dto.java" "DTO"
generate_file "$TEMPLATE_DIR/Repository.java.template" "$BASE_PATH/repository/${ENTITY_NAME}Repository.java" "Repository"
generate_file "$TEMPLATE_DIR/Mapper.java.template" "$BASE_PATH/mapper/${ENTITY_NAME}Mapper.java" "Mapper"
generate_file "$TEMPLATE_DIR/Service.java.template" "$BASE_PATH/service/${ENTITY_NAME}Service.java" "Service Interface"
generate_file "$TEMPLATE_DIR/ServiceImpl.java.template" "$BASE_PATH/service/impl/${ENTITY_NAME}ServiceImpl.java" "Service Implementation"
generate_file "$TEMPLATE_DIR/Controller.java.template" "$BASE_PATH/controller/${ENTITY_NAME}Controller.java" "Controller"

echo ""
echo -e "${GREEN}✅ Generation completed!${NC}"
echo ""
echo -e "${BLUE}📊 Summary Report:${NC}"
echo -e "  ${GREEN}✓ Created:${NC} $CREATED_COUNT file(s)"
echo -e "  ${YELLOW}🔄 Replaced:${NC} $REPLACED_COUNT file(s)"
echo -e "  ${BLUE}↪ Skipped:${NC} $SKIPPED_COUNT file(s)"
echo ""

if [ $CREATED_COUNT -gt 0 ] || [ $REPLACED_COUNT -gt 0 ]; then
    echo -e "${BLUE}📁 Generated files:${NC}"
    echo "  📄 $BASE_PATH/model/$ENTITY_NAME.java"
    echo "  📄 $BASE_PATH/dto/${ENTITY_NAME}Dto.java"
    echo "  📄 $BASE_PATH/repository/${ENTITY_NAME}Repository.java"
    echo "  📄 $BASE_PATH/mapper/${ENTITY_NAME}Mapper.java"
    echo "  📄 $BASE_PATH/service/${ENTITY_NAME}Service.java"
    echo "  📄 $BASE_PATH/service/impl/${ENTITY_NAME}ServiceImpl.java"
    echo "  📄 $BASE_PATH/controller/${ENTITY_NAME}Controller.java"
    echo ""
    echo -e "${BLUE}🎯 API Endpoints:${NC} /api/v1/${ENTITY_LOWER}s"
fi
echo ""
