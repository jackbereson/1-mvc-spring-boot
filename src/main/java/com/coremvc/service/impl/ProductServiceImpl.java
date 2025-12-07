package com.coremvc.service.impl;

import com.coremvc.dto.ProductDto;
import com.coremvc.dto.RestPage;
import com.coremvc.exception.ResourceNotFoundException;
import com.coremvc.mapper.ProductMapper;
import com.coremvc.model.Product;
import com.coremvc.repository.ProductRepository;
import com.coremvc.service.ProductService;
import com.coremvc.util.SettingConstants;
import com.coremvc.util.SettingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product::page", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        log.info("Fetching paginated products from DATABASE (cache miss) - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        // get Setting key Website here
        var test = SettingHelper.loadStatic(SettingConstants.SettingKey.TITLE.getValue());

        //print test
        log.info("Test: {}", test);

        Page<ProductDto> page = productRepository.findAllOptimized(pageable)
                .map(productMapper::toDto);
        return new RestPage<>(page.getContent(), pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(Long id) {
        log.info("Fetching product with id={} from DATABASE (cache miss)", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return productMapper.toDto(product);
    }

    @Override
    @CacheEvict(value = {"products", "product::page"}, allEntries = true)
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @CachePut(value = "products", key = "#id")
    @CacheEvict(value = "product::page", allEntries = true)
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));


        if (productDto.getName() != null) {
            existingProduct.setName(productDto.getName());
        }
        if (productDto.getDescription() != null) {
            existingProduct.setDescription(productDto.getDescription());
        }
        if (productDto.getPrice() != null) {
            existingProduct.setPrice(productDto.getPrice());
        }
        if (productDto.getCategoryId() != null) {
            existingProduct.setCategoryId(productDto.getCategoryId());
        }
        if (productDto.getThumbnailUrl() != null) {
            existingProduct.setThumbnailUrl(productDto.getThumbnailUrl());
        }
        if (productDto.getIsActive() != null) {
            existingProduct.setIsActive(productDto.getIsActive());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

        @Override
    @CacheEvict(value = {"products", "product::page"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Override
    public Page<ProductDto> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> searchProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable).map(productMapper::toDto);
    }
}
