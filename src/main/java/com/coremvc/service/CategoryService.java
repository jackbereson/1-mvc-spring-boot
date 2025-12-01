package com.coremvc.service;

import com.coremvc.dto.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategorys();
    
    Page<CategoryDto> getAllCategorys(Pageable pageable);
    
    CategoryDto getCategoryById(Long id);
    
    CategoryDto createCategory(CategoryDto categoryDto);
    
    CategoryDto updateCategory(Long id, CategoryDto categoryDto);
    
    void deleteCategory(Long id);
    
    Page<CategoryDto> searchCategorysByName(String name, Pageable pageable);
}
