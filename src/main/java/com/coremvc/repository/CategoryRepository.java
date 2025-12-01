package com.coremvc.repository;

import com.coremvc.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long id);
    
    Page<Category> findByIsActiveTrue(Pageable pageable);
    
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
