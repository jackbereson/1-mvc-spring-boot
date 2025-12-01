package com.coremvc.controller;

import com.coremvc.dto.ApiResponse;
import com.coremvc.dto.CategoryDto;
import com.coremvc.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categorys")
@CrossOrigin(origins= "*")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> getAllCategorys(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CategoryDto> categorys = categoryService.getAllCategorys(pageable);

        return ResponseEntity.ok(
                new ApiResponse<>("Categorys retrieved successfully", categorys, true)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long id) {
        CategoryDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Category retrieved successfully", category, true)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto createdCategory = categoryService.createCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Category created successfully", createdCategory, true)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto) {

        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
        return ResponseEntity.ok(
                new ApiResponse<>("Category updated successfully", updatedCategory, true)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Category deleted successfully", null, true)
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> searchCategorys(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryDto> categorys = categoryService.searchCategorysByName(name, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>("Categorys retrieved successfully", categorys, true)
        );
    }
}
