package com.coremvc.mapper;

import com.coremvc.dto.CategoryDto;
import com.coremvc.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public Category toEntity(CategoryDto categoryDto) {
        if (categoryDto == null) {
            return null;
        }

        return Category.builder()
                .name(categoryDto.getName())
                .isActive(categoryDto.getIsActive())
                .build();
    }
}
