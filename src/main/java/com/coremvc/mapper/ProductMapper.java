package com.coremvc.mapper;

import com.coremvc.dto.ProductDto;
import com.coremvc.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .category(product.getCategory())
                .thumbnailUrl(product.getThumbnailUrl())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public Product toEntity(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }

        return Product.builder()
                .name(productDto.getName())
                .price(productDto.getPrice())
                .description(productDto.getDescription())
                .category(productDto.getCategory())
                .thumbnailUrl(productDto.getThumbnailUrl())
                .isActive(productDto.getIsActive())
                .build();
    }
}
