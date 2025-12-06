package com.coremvc.repository;

import com.coremvc.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Optimized query with performance hints
    // - fetchSize: fetch in batches to reduce memory usage
    // - readOnly: skip dirty checking for read operations
    // - cacheable: disable L2 cache for large datasets
    @Query("SELECT p FROM Product p")
    @QueryHints({
        @QueryHint(name = "org.hibernate.fetchSize", value = "500"),
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false")
    })
    Page<Product> findAllOptimized(Pageable pageable);
    
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
