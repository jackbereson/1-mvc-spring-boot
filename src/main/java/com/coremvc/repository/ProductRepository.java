package com.coremvc.repository;

import com.coremvc.model.Product;
import com.coremvc.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);
    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
