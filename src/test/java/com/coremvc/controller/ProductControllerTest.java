package com.coremvc.controller;

import com.coremvc.dto.ProductDto;
import com.coremvc.model.Product;
import com.coremvc.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ProductController Integration Tests - Full API Flow")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    private Product testProduct;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .categoryId(1L)
                .thumbnailUrl("https://example.com/image.jpg")
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);

        productDto = ProductDto.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .categoryId(2L)
                .thumbnailUrl("https://example.com/new-image.jpg")
                .isActive(true)
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Should retrieve all products successfully with pagination")
    void testGetAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .with(user("admin").roles("ADMIN"))
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDirection", "ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Users retrieved successfully")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Test Product")))
                .andExpect(jsonPath("$.data.content[0].price", is(99.99)))
                .andExpect(jsonPath("$.data.content[0].categoryId", is(1)));
    }

    @Test
    @Order(2)
    @DisplayName("Should retrieve all products with DESC sorting")
    void testGetAllProducts_WithDescSorting() throws Exception {
        Product product2 = Product.builder()
                .name("Another Product")
                .description("Another Description")
                .price(new BigDecimal("199.99"))
                .categoryId(1L)
                .isActive(true)
                .build();
        productRepository.save(product2);

        mockMvc.perform(get("/api/v1/products")
                .with(user("admin").roles("ADMIN"))
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "price")
                .param("sortDirection", "DESC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].price", is(199.99)));
    }

    @Test
    @Order(3)
    @DisplayName("Should return empty page when no products exist")
    void testGetAllProducts_Empty() throws Exception {
        productRepository.deleteAll();
        productRepository.flush();

        // Clear cache to ensure fresh data is fetched
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

        mockMvc.perform(get("/api/v1/products")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @Order(4)
    @DisplayName("Should retrieve product by ID from database")
    void testGetProductById_Success() throws Exception {
        Long productId = testProduct.getId();

        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product retrieved successfully")))
                .andExpect(jsonPath("$.data.name", is("Test Product")))
                .andExpect(jsonPath("$.data.description", is("Test Description")))
                .andExpect(jsonPath("$.data.price", is(99.99)))
                .andExpect(jsonPath("$.data.categoryId", is(1)))
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 when product not found by ID")
    void testGetProductById_NotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @Order(6)
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product created successfully")))
                .andExpect(jsonPath("$.data.name", is("New Product")))
                .andExpect(jsonPath("$.data.description", is("New Description")))
                .andExpect(jsonPath("$.data.price", is(149.99)))
                .andExpect(jsonPath("$.data.categoryId", is(2)))
                .andExpect(jsonPath("$.data.isActive", is(true)));

        Assertions.assertEquals(2, productRepository.count());
    }

    @Test
    @Order(7)
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() throws Exception {
        Long productId = testProduct.getId();

        ProductDto updateDto = ProductDto.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("299.99"))
                .categoryId(3L)
                .thumbnailUrl("https://example.com/updated-image.jpg")
                .isActive(false)
                .build();

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product retrieved successfully")))
                .andExpect(jsonPath("$.data.name", is("Updated Product")))
                .andExpect(jsonPath("$.data.description", is("Updated Description")))
                .andExpect(jsonPath("$.data.price", is(299.99)))
                .andExpect(jsonPath("$.data.categoryId", is(3)))
                .andExpect(jsonPath("$.data.isActive", is(false)));

        Product updatedProductInDb = productRepository.findById(productId).orElseThrow();
        Assertions.assertEquals("Updated Product", updatedProductInDb.getName());
        Assertions.assertEquals(new BigDecimal("299.99"), updatedProductInDb.getPrice());
        Assertions.assertEquals(false, updatedProductInDb.getIsActive());
    }

    @Test
    @Order(8)
    @DisplayName("Should return 404 when updating non-existent product")
    void testUpdateProduct_NotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(put("/api/v1/products/{id}", nonExistentId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    @Order(9)
    @DisplayName("Should update product with partial data")
    void testUpdateProduct_PartialUpdate() throws Exception {
        Long productId = testProduct.getId();

        ProductDto partialUpdateDto = ProductDto.builder()
                .name("Partially Updated Product")
                .build();

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Partially Updated Product")))
                .andExpect(jsonPath("$.data.price", is(99.99)))
                .andExpect(jsonPath("$.data.categoryId", is(1)));
    }

    @Test
    @Order(10)
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() throws Exception {
        Long productId = testProduct.getId();

        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product deleted successfully")));

        Assertions.assertFalse(productRepository.findById(productId).isPresent());
    }

    @Test
    @Order(11)
    @DisplayName("Should return 404 when deleting non-existent product")
    void testDeleteProduct_NotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(delete("/api/v1/products/{id}", nonExistentId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    @Order(12)
    @DisplayName("Should retrieve products by categoryId with pagination")
    void testGetProductsByCategoryId_Success() throws Exception {
        Product product2 = Product.builder()
                .name("Another Electronics")
                .description("Another Description")
                .price(new BigDecimal("199.99"))
                .categoryId(1L)
                .isActive(true)
                .build();
        productRepository.save(product2);

        Product product3 = Product.builder()
                .name("Book Product")
                .description("Book Description")
                .price(new BigDecimal("29.99"))
                .categoryId(2L)
                .isActive(true)
                .build();
        productRepository.save(product3);

        mockMvc.perform(get("/api/v1/products/category-id/{categoryId}", 1L)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Products retrieved successfully")))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].categoryId", is(1)))
                .andExpect(jsonPath("$.data.content[1].categoryId", is(1)));
    }

    @Test
    @Order(13)
    @DisplayName("Should return empty page when no products found in categoryId")
    void testGetProductsByCategoryId_Empty() throws Exception {
        mockMvc.perform(get("/api/v1/products/category-id/{categoryId}", 999L)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @Order(14)
    @DisplayName("Should search products by name successfully")
    void testSearchProductsByName_Success() throws Exception {
        Product product2 = Product.builder()
                .name("Test Laptop")
                .description("Laptop Description")
                .price(new BigDecimal("999.99"))
                .categoryId(1L)
                .isActive(true)
                .build();
        productRepository.save(product2);

        mockMvc.perform(get("/api/v1/products/search")
                .param("name", "test")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Products retrieved successfully")))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].name", hasItem(containsString("Test"))));
    }

    @Test
    @Order(15)
    @DisplayName("Should search products by name case insensitive")
    void testSearchProductsByName_CaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("name", "PRODUCT")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Test Product")));
    }

    @Test
    @Order(16)
    @DisplayName("Should return empty page when no products match search")
    void testSearchProductsByName_NoMatch() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("name", "NonExistentProduct")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @Order(17)
    @DisplayName("Should deny access to getAllProducts without ADMIN role")
    void testGetAllProducts_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(18)
    @DisplayName("Should deny access to getProductById without ADMIN role")
    void testGetProductById_Unauthorized() throws Exception {
        Long productId = testProduct.getId();

        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(19)
    @DisplayName("Should deny access to createProduct without ADMIN role")
    void testCreateProduct_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(20)
    @DisplayName("Should deny access to updateProduct without ADMIN role")
    void testUpdateProduct_Unauthorized() throws Exception {
        Long productId = testProduct.getId();

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(21)
    @DisplayName("Should deny access to deleteProduct without ADMIN role")
    void testDeleteProduct_Unauthorized() throws Exception {
        Long productId = testProduct.getId();

        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(22)
    @DisplayName("Should allow getProductsByCategoryId without authentication")
    void testGetProductsByCategoryId_PublicAccess() throws Exception {
        mockMvc.perform(get("/api/v1/products/category-id/{categoryId}", 1L)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @Order(23)
    @DisplayName("Should allow searchProducts without authentication")
    void testSearchProducts_PublicAccess() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("name", "test")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
