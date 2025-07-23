package com.example.service.cache;

import lombok.Getter;

import java.util.Optional;
import java.util.Set;

/**
 * Service interface for cache operations
 */
public interface CacheService {

    /**
     * Put a value into cache
     *
     * @param cacheName Cache name
     * @param key       Cache key
     * @param value     Value to cache
     */
    void put(String cacheName, String key, Object value);

    /**
     * Get a value from cache
     *
     * @param cacheName Cache name
     * @param key       Cache key
     * @param clazz     Value type
     * @return Optional containing the cached value if found
     */
    <T> Optional<T> get(String cacheName, String key, Class<T> clazz);

    /**
     * Evict a single cache entry
     *
     * @param cacheName Cache name
     * @param key       Cache key
     */
    void evict(String cacheName, String key);

    /**
     * Evict all entries from a cache
     *
     * @param cacheName Cache name
     */
    void evictAll(String cacheName);

    /**
     * Evict all entries from multiple caches
     *
     * @param cacheNames Cache names
     */
    void evictAll(String... cacheNames);

    /**
     * Check if a cache contains a specific key
     *
     * @param cacheName Cache name
     * @param key       Cache key
     * @return true if key exists in cache
     */
    boolean hasKey(String cacheName, String key);

    /**
     * Get all keys from a cache
     *
     * @param cacheName Cache name
     * @return Set of keys
     */
    Set<String> getKeys(String cacheName);

    /**
     * Get cache size
     *
     * @param cacheName Cache name
     * @return Number of entries in cache
     */
    long getSize(String cacheName);

    /**
     * Put multiple values into cache
     *
     * @param cacheName Cache name
     * @param values    Map of key-value pairs
     */
    void putAll(String cacheName, java.util.Map<String, Object> values);

    /**
     * Get multiple values from cache
     *
     * @param cacheName Cache name
     * @param keys      Set of keys
     * @param clazz     Value type
     * @return Map of key-value pairs found in cache
     */
    <T> java.util.Map<String, T> getAll(String cacheName, Set<String> keys, Class<T> clazz);

    /**
     * Evict multiple cache entries
     *
     * @param cacheName Cache name
     * @param keys      Set of keys to evict
     */
    void evictAll(String cacheName, Set<String> keys);

    /**
     * Clear all caches
     */
    void clearAll();

    /**
     * Get cache statistics
     *
     * @param cacheName Cache name
     * @return Cache statistics
     */
    CacheStats getCacheStats(String cacheName);

    /**
     * Warm up cache with data
     *
     * @param cacheName  Cache name
     * @param warmupData Data to warm up the cache
     */
    void warmUp(String cacheName, java.util.Map<String, Object> warmupData);

    /**
     * Set TTL for a cache entry
     *
     * @param cacheName  Cache name
     * @param key        Cache key
     * @param ttlSeconds TTL in seconds
     */
    void setTtl(String cacheName, String key, long ttlSeconds);

    /**
     * Get TTL for a cache entry
     *
     * @param cacheName Cache name
     * @param key       Cache key
     * @return TTL in seconds (-1 if key doesn't exist, -2 if key exists but has no TTL)
     */
    long getTtl(String cacheName, String key);

    /**
     * Cache statistics
     */
    @Getter
    class CacheStats {
        private final long size;
        private final long hitCount;
        private final long missCount;
        private final double hitRate;

        public CacheStats(long size, long hitCount, long missCount) {
            this.size = size;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.hitRate = (hitCount + missCount) > 0 ? (double) hitCount / (hitCount + missCount) : 0.0;
        }

        public long getTotalRequests() {
            return hitCount + missCount;
        }
    }
} 