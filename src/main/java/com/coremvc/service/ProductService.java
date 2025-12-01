package com.coremvc.service;

import com.coremvc.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService  {
    List<ProductDto> getAllProducts();

    Page<ProductDto> getAllProducts(Pageable pageable);

    ProductDto getProductById(Long id);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(Long id, ProductDto productDto);

    void deleteProduct(Long id);

    Page<ProductDto> getProductsByCategory(String category, Pageable pageable);

    Page<ProductDto> searchProductsByName(String name, Pageable pageable);
}
