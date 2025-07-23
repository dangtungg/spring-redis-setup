package com.example.service.cache;

import com.example.config.CacheConfig;
import com.example.config.properties.RedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling cache invalidation strategies
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheInvalidationService {

    private final CacheService cacheService;
    private final RedisProperties redisProperties;

    /**
     * Invalidate all caches for a category
     */
    public void invalidateCategoryCaches(Long categoryId, String categoryName, String categoryPath) {
        try {
            // Evict specific category caches
            if (categoryId != null) {
                cacheService.evict(CacheConfig.CacheNames.CATEGORY, "dto_" + categoryId.toString());
            }
            if (categoryName != null) {
                cacheService.evict(CacheConfig.CacheNames.CATEGORY_BY_NAME, "entity_" + categoryName);
                cacheService.evict(CacheConfig.CacheNames.CATEGORY_BY_NAME, "dto_" + categoryName);
            }
            if (categoryPath != null) {
                cacheService.evict(CacheConfig.CacheNames.CATEGORY_BY_PATH, "entity_" + categoryPath);
                cacheService.evict(CacheConfig.CacheNames.CATEGORY_BY_PATH, "dto_" + categoryPath);
            }

            // Evict list caches
            cacheService.evictAll(CacheConfig.CacheNames.ALL_CATEGORIES);

            log.info("Invalidated category caches for categoryId: {}, categoryName: {}, categoryPath: {}",
                    categoryId, categoryName, categoryPath);
        } catch (Exception e) {
            log.error("Error invalidating category caches: {}", e.getMessage());
        }
    }

    /**
     * Invalidate all caches for an article
     */
    public void invalidateArticleCaches(Long articleId, String articleName, String articlePath) {
        try {
            // Evict specific article caches
            if (articleId != null) {
                cacheService.evict(CacheConfig.CacheNames.ARTICLE, "dto_" + articleId.toString());
            }
            if (articleName != null) {
                cacheService.evict(CacheConfig.CacheNames.ARTICLE_BY_NAME, "entity_" + articleName);
                cacheService.evict(CacheConfig.CacheNames.ARTICLE_BY_NAME, "dto_" + articleName);
            }
            if (articlePath != null) {
                cacheService.evict(CacheConfig.CacheNames.ARTICLE_BY_PATH, "entity_" + articlePath);
                cacheService.evict(CacheConfig.CacheNames.ARTICLE_BY_PATH, "dto_" + articlePath);
            }

            // Evict list caches
            cacheService.evictAll(CacheConfig.CacheNames.ALL_ARTICLES);

            log.info("Invalidated article caches for articleId: {}, articleName: {}, articlePath: {}",
                    articleId, articleName, articlePath);
        } catch (Exception e) {
            log.error("Error invalidating article caches: {}", e.getMessage());
        }
    }

    /**
     * Invalidate all master data caches
     */
    public void invalidateAllMasterDataCaches() {
        try {
            List<String> masterDataCaches = List.of(
                    CacheConfig.CacheNames.CATEGORY,
                    CacheConfig.CacheNames.CATEGORY_BY_NAME,
                    CacheConfig.CacheNames.CATEGORY_BY_PATH,
                    CacheConfig.CacheNames.ALL_CATEGORIES,
                    CacheConfig.CacheNames.ARTICLE,
                    CacheConfig.CacheNames.ARTICLE_BY_NAME,
                    CacheConfig.CacheNames.ARTICLE_BY_PATH,
                    CacheConfig.CacheNames.ALL_ARTICLES
            );

            cacheService.evictAll(masterDataCaches.toArray(new String[0]));
            log.info("Invalidated all master data caches");
        } catch (Exception e) {
            log.error("Error invalidating all master data caches: {}", e.getMessage());
        }
    }

    /**
     * Invalidate caches by pattern
     */
    public void invalidateCachesByPattern(String pattern) {
        try {
            // This would require implementing pattern-based cache invalidation
            // For now, we'll just log and potentially implement if needed
            log.warn("Pattern-based cache invalidation not implemented yet: {}", pattern);
        } catch (Exception e) {
            log.error("Error invalidating caches by pattern '{}': {}", pattern, e.getMessage());
        }
    }

    /**
     * Invalidate expired caches (manual cleanup)
     */
    public void invalidateExpiredCaches() {
        try {
            // This would require implementing TTL-based cache cleanup
            // Redis handles this automatically, but we can implement manual cleanup if needed
            log.info("Checking for expired caches...");

            // Example: Check cache sizes and log statistics
            logCacheStatistics();
        } catch (Exception e) {
            log.error("Error during expired cache cleanup: {}", e.getMessage());
        }
    }

    /**
     * Log cache statistics for monitoring
     */
    public void logCacheStatistics() {
        try {
            log.info("=== Cache Statistics ===");

            // Category caches
            long bankCacheSize = cacheService.getSize(CacheConfig.CacheNames.CATEGORY);
            long bankByNameSize = cacheService.getSize(CacheConfig.CacheNames.CATEGORY_BY_NAME);
            long bankByPathSize = cacheService.getSize(CacheConfig.CacheNames.CATEGORY_BY_PATH);
            log.info("Category caches - Main: {}, ByName: {}, ByPath: {}",
                    bankCacheSize, bankByNameSize, bankByPathSize);

            // Article caches
            long articleCacheSize = cacheService.getSize(CacheConfig.CacheNames.ARTICLE);
            long articleByNameSize = cacheService.getSize(CacheConfig.CacheNames.ARTICLE_BY_NAME);
            long articleByPathSize = cacheService.getSize(CacheConfig.CacheNames.ARTICLE_BY_PATH);
            log.info("Article caches - Main: {}, ByName: {}, ByPath: {}",
                    articleCacheSize, articleByNameSize, articleByPathSize);

            log.info("=== End Cache Statistics ===");
        } catch (Exception e) {
            log.error("Error logging cache statistics: {}", e.getMessage());
        }
    }

    /**
     * Warm up specific cache
     */
    public void warmUpCache(String cacheName) {
        try {
            // This would require implementing cache warming strategies
            // For now, we'll just log
            log.info("Warming up cache: {}", cacheName);
        } catch (Exception e) {
            log.error("Error warming up cache '{}': {}", cacheName, e.getMessage());
        }
    }

    /**
     * Health check for the cache system
     */
    public boolean isHealthy() {
        try {
            // Simple health check - try to perform a basic cache operation
            cacheService.put(CacheConfig.CacheNames.CATEGORY, "health_check", "test");
            boolean exists = cacheService.hasKey(CacheConfig.CacheNames.CATEGORY, "health_check");
            cacheService.evict(CacheConfig.CacheNames.CATEGORY, "health_check");
            return exists;
        } catch (Exception e) {
            log.error("Cache health check failed: {}", e.getMessage());
            return false;
        }
    }
} 