package com.example.service.cache;

import com.example.config.properties.RedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisProperties redisProperties;

    @Override
    public void put(String cacheName, String key, Object value) {
        try {
            Cache cache = getCache(cacheName);
            cache.put(key, value);
            log.debug("Cached value for key '{}' in cache '{}'", key, cacheName);
        } catch (Exception e) {
            log.error("Error caching value for key '{}' in cache '{}': {}", key, cacheName, e.getMessage());
            throw new RuntimeException("Failed to cache value", e);
        }
    }

    @Override
    public <T> Optional<T> get(String cacheName, String key, Class<T> clazz) {
        try {
            Cache cache = getCache(cacheName);
            Cache.ValueWrapper wrapper = cache.get(key);

            if (wrapper == null) {
                log.debug("Cache miss for key '{}' in cache '{}'", key, cacheName);
                return Optional.empty();
            }

            Object value = wrapper.get();
            if (value == null) {
                log.debug("Cached null value for key '{}' in cache '{}'", key, cacheName);
                return Optional.empty();
            }

            if (clazz.isInstance(value)) {
                log.debug("Cache hit for key '{}' in cache '{}'", key, cacheName);
                return Optional.of(clazz.cast(value));
            }

            log.warn("Cached value for key '{}' in cache '{}' is not of expected type {}",
                    key, cacheName, clazz.getSimpleName());
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error retrieving cached value for key '{}' in cache '{}': {}",
                    key, cacheName, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void evict(String cacheName, String key) {
        try {
            Cache cache = getCache(cacheName);
            cache.evict(key);
            log.debug("Evicted key '{}' from cache '{}'", key, cacheName);
        } catch (Exception e) {
            log.error("Error evicting key '{}' from cache '{}': {}", key, cacheName, e.getMessage());
        }
    }

    @Override
    public void evictAll(String cacheName) {
        try {
            Cache cache = getCache(cacheName);
            cache.clear();
            log.info("Cleared all entries from cache '{}'", cacheName);
        } catch (Exception e) {
            log.error("Error clearing cache '{}': {}", cacheName, e.getMessage());
        }
    }

    @Override
    public void evictAll(String... cacheNames) {
        for (String cacheName : cacheNames) {
            evictAll(cacheName);
        }
    }

    @Override
    public boolean hasKey(String cacheName, String key) {
        try {
            String redisKey = buildRedisKey(cacheName, key);
            return redisTemplate.hasKey(redisKey);
        } catch (Exception e) {
            log.error("Error checking if key '{}' exists in cache '{}': {}", key, cacheName, e.getMessage());
            return false;
        }
    }

    @Override
    public Set<String> getKeys(String cacheName) {
        try {
            String pattern = buildRedisKey(cacheName, "*");
            Set<String> redisKeys = redisTemplate.keys(pattern);

            String prefix = buildRedisKey(cacheName, "");
            return redisKeys.stream()
                    .map(redisKey -> redisKey.substring(prefix.length()))
                    .collect(Collectors.toSet());

        } catch (Exception e) {
            log.error("Error getting keys from cache '{}': {}", cacheName, e.getMessage());
            return new HashSet<>();
        }
    }

    @Override
    public long getSize(String cacheName) {
        try {
            String pattern = buildRedisKey(cacheName, "*");
            Set<String> keys = redisTemplate.keys(pattern);
            return keys.size();
        } catch (Exception e) {
            log.error("Error getting size of cache '{}': {}", cacheName, e.getMessage());
            return 0;
        }
    }

    @Override
    public void putAll(String cacheName, Map<String, Object> values) {
        try {
            Cache cache = getCache(cacheName);
            values.forEach(cache::put);
            log.info("Cached {} values in cache '{}'", values.size(), cacheName);
        } catch (Exception e) {
            log.error("Error caching multiple values in cache '{}': {}", cacheName, e.getMessage());
            throw new RuntimeException("Failed to cache multiple values", e);
        }
    }

    @Override
    public <T> Map<String, T> getAll(String cacheName, Set<String> keys, Class<T> clazz) {
        Map<String, T> result = new HashMap<>();

        for (String key : keys) {
            get(cacheName, key, clazz).ifPresent(value -> result.put(key, value));
        }

        log.debug("Retrieved {} out of {} requested keys from cache '{}'",
                result.size(), keys.size(), cacheName);
        return result;
    }

    @Override
    public void evictAll(String cacheName, Set<String> keys) {
        try {
            Cache cache = getCache(cacheName);
            keys.forEach(cache::evict);
            log.info("Evicted {} keys from cache '{}'", keys.size(), cacheName);
        } catch (Exception e) {
            log.error("Error evicting multiple keys from cache '{}': {}", cacheName, e.getMessage());
        }
    }

    @Override
    public void clearAll() {
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            cacheNames.forEach(this::evictAll);
            log.info("Cleared all caches: {}", cacheNames);
        } catch (Exception e) {
            log.error("Error clearing all caches: {}", e.getMessage());
        }
    }

    @Override
    public CacheStats getCacheStats(String cacheName) {
        try {
            long size = getSize(cacheName);
            // Note: Redis doesn't provide hit/miss statistics out of the box
            // You would need to implement custom metrics or use Redis modules
            return new CacheStats(size, 0, 0);
        } catch (Exception e) {
            log.error("Error getting cache stats for cache '{}': {}", cacheName, e.getMessage());
            return new CacheStats(0, 0, 0);
        }
    }

    @Override
    public void warmUp(String cacheName, Map<String, Object> warmupData) {
        try {
            putAll(cacheName, warmupData);
            log.info("Warmed up cache '{}' with {} entries", cacheName, warmupData.size());
        } catch (Exception e) {
            log.error("Error warming up cache '{}': {}", cacheName, e.getMessage());
        }
    }

    @Override
    public void setTtl(String cacheName, String key, long ttlSeconds) {
        try {
            String redisKey = buildRedisKey(cacheName, key);
            redisTemplate.expire(redisKey, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Set TTL of {} seconds for key '{}' in cache '{}'", ttlSeconds, key, cacheName);
        } catch (Exception e) {
            log.error("Error setting TTL for key '{}' in cache '{}': {}", key, cacheName, e.getMessage());
        }
    }

    @Override
    public long getTtl(String cacheName, String key) {
        try {
            String redisKey = buildRedisKey(cacheName, key);
            return redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting TTL for key '{}' in cache '{}': {}", key, cacheName, e.getMessage());
            return -1;
        }
    }

    private Cache getCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("Cache '" + cacheName + "' not found");
        }
        return cache;
    }

    private String buildRedisKey(String cacheName, String key) {
        return redisProperties.getCache().getKeyPrefix() +
                redisProperties.getCache().getKeySeparator() +
                cacheName +
                redisProperties.getCache().getKeySeparator() +
                key;
    }
} 