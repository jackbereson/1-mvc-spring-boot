package com.coremvc.service.impl;

import com.coremvc.dto.CategoryDto;
import com.coremvc.exception.ResourceNotFoundException;
import com.coremvc.mapper.CategoryMapper;
import com.coremvc.model.Category;
import com.coremvc.repository.CategoryRepository;
import com.coremvc.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> getAllCategorys() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategorys(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (categoryDto.getName() != null) {
            existingCategory.setName(categoryDto.getName());
        }
        if (categoryDto.getIsActive() != null) {
            existingCategory.setIsActive(categoryDto.getIsActive());
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    @Override
    public Page<CategoryDto> searchCategorysByName(String name, Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(categoryMapper::toDto);
    }
}
