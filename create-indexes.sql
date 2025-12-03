-- Create performance indexes for products table
-- Run this script: psql -h localhost -U postgres -d dev -f create-indexes.sql

-- Index for sorting by created_at DESC (most common query)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_created_at_desc ON products (created_at DESC);

-- Index for sorting by id DESC (alternative sorting)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_id_desc ON products (id DESC);

-- Index for is_active filter
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_is_active ON products (is_active) WHERE is_active = true;

-- Index for category filter
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_category ON products (category) WHERE category IS NOT NULL;

-- Composite index for active products sorted by created_at
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_active_created_at ON products (is_active, created_at DESC) WHERE is_active = true;

-- Update table statistics for query planner
ANALYZE products;

-- Show all indexes on products table
\d products
