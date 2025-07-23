package com.example.service.cache;

import com.example.config.CacheConfig;
import com.example.config.properties.RedisProperties;
import com.example.model.dto.ArticleDTO;
import com.example.model.dto.CategoryDTO;
import com.example.service.ArticleService;
import com.example.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for cache warming strategies
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheWarmupService {

    private final CacheService cacheService;
    private final RedisProperties redisProperties;
    private final CategoryService categoryService;
    private final ArticleService articleService;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * Warm up all caches on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(1000) // Run after the other initialization
    @Async
    public void warmUpCachesOnStartup() {
        if (!redisProperties.isEnabled()) {
            log.info("Redis caching is disabled, skipping cache warmup");
            return;
        }

        log.info("Starting cache warmup process...");

        try {
            // Warm up caches in parallel
            CompletableFuture<Void> categoryWarmup = CompletableFuture.runAsync(this::warmUpCategoryCaches, executor);
            CompletableFuture<Void> articleWarmup = CompletableFuture.runAsync(this::warmUpArticleCaches, executor);

            // Wait for critical caches to be warmed up before warming accounts
            CompletableFuture.allOf(categoryWarmup, articleWarmup).join();

            log.info("Cache warmup process completed successfully");
        } catch (Exception e) {
            log.error("Error during cache warmup: {}", e.getMessage(), e);
        }
    }

    /**
     * Warm up category caches
     */
    public void warmUpCategoryCaches() {
        if (!redisProperties.getCache().getEntities().getCategory().isWarmupOnStartup()) {
            log.debug("Category cache warmup disabled");
            return;
        }

        try {
            log.info("Warming up category caches...");

            // Clear potentially corrupted cache entries before warmup
            try {
                cacheService.evictAll(CacheConfig.CacheNames.ALL_CATEGORIES);
                cacheService.evictAll(CacheConfig.CacheNames.CATEGORY);
                cacheService.evictAll(CacheConfig.CacheNames.CATEGORY_BY_NAME);
                cacheService.evictAll(CacheConfig.CacheNames.CATEGORY_BY_PATH);
                log.debug("Cleared potentially corrupted category cache entries");
            } catch (Exception e) {
                log.debug("Could not clear category cache entries: {}", e.getMessage());
            }

            // Load all categories
            List<CategoryDTO> categories = categoryService.findByCriteria(null);
            log.info("Loaded {} categories for cache warmup", categories.size());

            // Warm up individual category caches
            Map<String, Object> categoryDTOCache = new HashMap<>();
            Map<String, Object> categoryDTOByNameCache = new HashMap<>();
            Map<String, Object> categoryDTOByPathCache = new HashMap<>();

            for (CategoryDTO category : categories) {
                // Cache DTOs
                categoryDTOCache.put("dto_" + category.getId().toString(), category);
                categoryDTOByNameCache.put("dto_" + category.getName(), category);
                categoryDTOByPathCache.put("dto_" + category.getPath(), category);
            }

            // Bulk load into caches
            cacheService.putAll(CacheConfig.CacheNames.CATEGORY, categoryDTOCache);
            cacheService.putAll(CacheConfig.CacheNames.CATEGORY_BY_NAME, categoryDTOByNameCache);
            cacheService.putAll(CacheConfig.CacheNames.CATEGORY_BY_PATH, categoryDTOByPathCache);

            // Cache the list of all categories
            cacheService.put(CacheConfig.CacheNames.ALL_CATEGORIES, "dto_all", categories);

            log.info("Category cache warmup completed: {} categories cached", categories.size());
        } catch (Exception e) {
            log.error("Error warming up category caches: {}", e.getMessage(), e);
        }
    }

    /**
     * Warm up article caches
     */
    public void warmUpArticleCaches() {
        if (!redisProperties.getCache().getEntities().getArticle().isWarmupOnStartup()) {
            log.debug("Article cache warmup disabled");
            return;
        }

        try {
            log.info("Warming up article caches...");

            // Clear potentially corrupted cache entries before warmup
            try {
                cacheService.evictAll(CacheConfig.CacheNames.ALL_ARTICLES);
                cacheService.evictAll(CacheConfig.CacheNames.ARTICLE);
                cacheService.evictAll(CacheConfig.CacheNames.ARTICLE_BY_NAME);
                cacheService.evictAll(CacheConfig.CacheNames.ARTICLE_BY_PATH);
                log.debug("Cleared potentially corrupted article cache entries");
            } catch (Exception e) {
                log.debug("Could not clear article cache entries: {}", e.getMessage());
            }

            // Load all categories
            List<ArticleDTO> articles = articleService.findByCriteria(null);
            log.info("Loaded {} articles for cache warmup", articles.size());

            // Warm up individual article caches
            Map<String, Object> articleDTOCache = new HashMap<>();
            Map<String, Object> articleDTOByNameCache = new HashMap<>();
            Map<String, Object> articleDTOByPathCache = new HashMap<>();

            for (ArticleDTO article : articles) {
                // Cache DTOs
                articleDTOCache.put("dto_" + article.getId().toString(), article);
                articleDTOByNameCache.put("dto_" + article.getName(), article);
                articleDTOByPathCache.put("dto_" + article.getPath(), article);
            }

            // Bulk load into caches
            cacheService.putAll(CacheConfig.CacheNames.ARTICLE, articleDTOCache);
            cacheService.putAll(CacheConfig.CacheNames.ARTICLE_BY_NAME, articleDTOByNameCache);
            cacheService.putAll(CacheConfig.CacheNames.ARTICLE_BY_PATH, articleDTOByPathCache);

            // Cache the list of all categories
            cacheService.put(CacheConfig.CacheNames.ALL_ARTICLES, "dto_all", articles);

            log.info("Article cache warmup completed: {} articles cached", articles.size());
        } catch (Exception e) {
            log.error("Error warming up article caches: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual cache warmup for a specific entity type
     */
    public void warmUpEntityCache(String entityType) {
        try {
            log.info("Manual cache warmup requested for entity type: {}", entityType);

            switch (entityType.toLowerCase()) {
                case "category":
                    warmUpCategoryCaches();
                    break;
                case "article":
                    warmUpArticleCaches();
                    break;
                case "all":
                    warmUpCachesOnStartup();
                    break;
                default:
                    log.warn("Unknown entity type for cache warmup: {}", entityType);
            }
        } catch (Exception e) {
            log.error("Error during manual cache warmup for entity type '{}': {}", entityType, e.getMessage(), e);
        }
    }

    /**
     * Get cache warmup status
     */
    public Map<String, Object> getCacheWarmupStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Check cache sizes
            status.put("category_cache_size", cacheService.getSize(CacheConfig.CacheNames.CATEGORY));
            status.put("article_cache_size", cacheService.getSize(CacheConfig.CacheNames.ARTICLE));

            // Check if warmup is enabled
            status.put("category_warmup_enabled", redisProperties.getCache().getEntities().getCategory().isWarmupOnStartup());
            status.put("article_warmup_enabled", redisProperties.getCache().getEntities().getArticle().isWarmupOnStartup());

            status.put("status", "healthy");
        } catch (Exception e) {
            status.put("status", "error");
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * Cleanup resources
     */
    public void shutdown() {
        try {
            executor.shutdown();
            log.info("Cache warmup service shutdown completed");
        } catch (Exception e) {
            log.error("Error during cache warmup service shutdown: {}", e.getMessage(), e);
        }
    }
} 