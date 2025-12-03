package com.coremvc.config;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Cache metrics and monitoring configuration.
 * Provides observability for cache hit/miss ratios and performance.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CacheMetricsConfig {

    private final CacheManager cacheManager;

    /**
     * Log cache statistics periodically for monitoring
     */
    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void logCacheStatistics() {
        if (cacheManager == null) {
            return;
        }

        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) cache;
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                
                com.github.benmanes.caffeine.cache.stats.CacheStats stats = nativeCache.stats();
                
                double hitRate = stats.hitRate() * 100;
                log.info("Cache [{}] - Hits: {}, Misses: {}, Hit Rate: {:.2f}%, Evictions: {}, Load Time: {}ms",
                        cacheName,
                        stats.hitCount(),
                        stats.missCount(),
                        String.format("%.2f", hitRate),
                        stats.evictionCount(),
                        stats.averageLoadPenalty() / 1_000_000 // Convert to ms
                );
            }
        });
    }
}
