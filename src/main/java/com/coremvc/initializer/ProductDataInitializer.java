package com.coremvc.initializer;

import com.coremvc.model.Category;
import com.coremvc.model.Product;
import com.coremvc.repository.CategoryRepository;
import com.coremvc.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;


    @Value("${product.init:false}")
    private Boolean productInit;

    private static final String[] CATEGORIES = {
            "Electronics", "Fashion", "Home & Garden", "Sports", "Books",
            "Toys", "Beauty", "Automotive", "Food", "Health"
    };

    private static final String[] ADJECTIVES = {
            "Premium", "Professional", "Classic", "Modern", "Vintage",
            "Luxury", "Essential", "Ultimate", "Advanced", "Smart"
    };

    private static final String[] PRODUCT_TYPES = {
            "Laptop", "Phone", "Tablet", "Watch", "Camera",
            "Headphones", "Speaker", "Monitor", "Keyboard", "Mouse",
            "Shirt", "Pants", "Shoes", "Bag", "Hat",
            "Sofa", "Table", "Chair", "Lamp", "Rug",
            "Ball", "Racket", "Weights", "Mat", "Bottle",
            "Novel", "Magazine", "Comic", "Guide", "Manual",
            "Doll", "Game", "Puzzle", "Car", "Robot",
            "Cream", "Lotion", "Perfume", "Soap", "Shampoo"
    };

    private static final String[] DESCRIPTIONS = {
            "High-quality product with excellent features",
            "Perfect for everyday use",
            "Durable and long-lasting design",
            "Best value for money",
            "Innovative and cutting-edge technology",
            "Comfortable and stylish",
            "Eco-friendly and sustainable",
            "Award-winning design",
            "Customer favorite",
            "New and improved version"
    };

    @Override
    public void run(String... args) throws Exception {
        if (!productInit) {
            return;
        }

        categoryRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();

        // add category data
        log.info("Starting to Category...");
        List<Category> categories = new ArrayList<>();
        for (String category : CATEGORIES) {
            log.info("Category: {}", category);
            Category cat = Category.builder()
                    .name(category)
                    .description("Description for " + category)
                    .build();
            categories.add(cat);
        }

        categoryRepository.saveAll(categories);

        log.info("Starting to initialize 100000 products...");
        
        List<Product> products = new ArrayList<>();
        Random random = new Random();

        List<Category> categoryList = categoryRepository.findAll();

        

        for (int i = 1; i <= 100000; i++) {
            String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
            String productType = PRODUCT_TYPES[random.nextInt(PRODUCT_TYPES.length)];
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            String description = DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)];
            
            BigDecimal price = BigDecimal.valueOf(9.99 + (random.nextDouble() * 990.01))
                    .setScale(2, RoundingMode.HALF_UP);

            var categoryEntity = categoryList.stream()
                    .filter(cat -> cat.getName().equals(category))
                    .findFirst()
                    .orElse(null);

            Product product = Product.builder()
                    .name(adjective + " " + productType + " #" + i)
                    .description(description + " - Product ID: " + i)
                    .price(price)
                    .categoryId(categoryEntity != null ? categoryEntity.getId() : null)
                    .thumbnailUrl("https://picsum.photos/400/300?random=" + i)
                    .isActive(random.nextInt(100) < 95) // 95% active
                    .build();

            products.add(product);

            if (i % 1000 == 0) {
                productRepository.saveAll(products);
                products.clear();
                log.info("Saved {} products...", i);
            }
        }

        if (!products.isEmpty()) {
            productRepository.saveAll(products);
        }

        log.info("Successfully initialized 100000 products!");
        log.info("Total products in database: {}", productRepository.count());
    }

    private void deleteAllInBatch() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
    }
}
