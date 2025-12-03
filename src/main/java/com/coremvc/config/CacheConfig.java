package com.coremvc.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * L1 Cache: Caffeine in-memory cache
     * Fast, per-instance cache for hot data
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000) // Max entries per cache
                .expireAfterWrite(5, TimeUnit.MINUTES) // Default TTL
                .recordStats() // Enable metrics
        );
        
        // Register metrics
        cacheManager.setCacheNames(Arrays.asList(
                "products", 
                "categories", 
                "settings", 
                "users",
                "product::page"
        ));
        
        return cacheManager;
    }

    /**
     * L2 Cache: Redis distributed cache
     * Shared across instances, persistent
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15)) // Default TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // Don't cache null values

        // Per-cache TTL configurations
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        
        // Products: 15-30 min with jitter
        cacheConfigs.put("products", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigs.put("product::page", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Categories: 30-60 min (rarely change)
        cacheConfigs.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(45)));
        cacheConfigs.put("category::list", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Settings: 10-30 min
        cacheConfigs.put("settings", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigs.put("setting::list", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Users: 5-10 min (short TTL for security)
        cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofMinutes(7)));
        cacheConfigs.put("user::email", defaultConfig.entryTtl(Duration.ofMinutes(7)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware() // Enable transaction support
                .build();
    }

    /**
     * Composite Cache Manager: L1 (Caffeine) + L2 (Redis)
     * Checks L1 first, then L2, fallback to source
     */
    @Bean
    @Primary
    public CacheManager compositeCacheManager(
            CaffeineCacheManager caffeineCacheManager,
            RedisCacheManager redisCacheManager) {
        
        CompositeCacheManager cacheManager = new CompositeCacheManager(
                caffeineCacheManager, // L1: Check first
                redisCacheManager      // L2: Fallback
        );
        cacheManager.setFallbackToNoOpCache(false); // Fail if cache miss (go to source)
        
        return cacheManager;
    }
}
