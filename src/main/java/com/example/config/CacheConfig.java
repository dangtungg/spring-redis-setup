package com.example.config;

import com.example.config.properties.RedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Cache manager and cache name definitions
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {

    private final RedisProperties redisProperties;
    private final GenericJackson2JsonRedisSerializer jsonRedisSerializer;

    /**
     * Cache names for different entities
     */
    public static final class CacheNames {
        public static final String ALL_CATEGORIES = "all_categories";
        public static final String CATEGORY = "category";
        public static final String CATEGORY_BY_NAME = "category_by_name";
        public static final String CATEGORY_BY_PATH = "category_by_path";

        public static final String ALL_ARTICLES = "all_articles";
        public static final String ARTICLE = "article";
        public static final String ARTICLE_BY_NAME = "article_by_name";
        public static final String ARTICLE_BY_PATH = "article_by_path";
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(createDefaultCacheConfiguration())
                .withInitialCacheConfigurations(createCacheConfigurations())
                .transactionAware();

        RedisCacheManager cacheManager = builder.build();
        log.info("Redis cache manager initialized with {} cache configurations", createCacheConfigurations().size());

        return cacheManager;
    }

    private RedisCacheConfiguration createDefaultCacheConfiguration() {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(redisProperties.getCache().getDefaultTtl())
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer))
                .computePrefixWith(cacheName ->
                        redisProperties.getCache().getKeyPrefix() +
                                redisProperties.getCache().getKeySeparator() +
                                cacheName +
                                redisProperties.getCache().getKeySeparator());
        // Conditionally disable caching null values
        if (!redisProperties.getCache().isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        return config;
    }

    private Map<String, RedisCacheConfiguration> createCacheConfigurations() {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Category caches
        Duration bankTtl = redisProperties.getCache().getEntities().getCategory().getTtl();
        cacheConfigurations.put(CacheNames.CATEGORY, createCacheConfiguration(bankTtl));
        cacheConfigurations.put(CacheNames.CATEGORY_BY_NAME, createCacheConfiguration(bankTtl));
        cacheConfigurations.put(CacheNames.CATEGORY_BY_PATH, createCacheConfiguration(bankTtl));
        cacheConfigurations.put(CacheNames.ALL_CATEGORIES, createCacheConfiguration(bankTtl));

        // Article caches
        Duration accountTtl = redisProperties.getCache().getEntities().getArticle().getTtl();
        cacheConfigurations.put(CacheNames.ARTICLE, createCacheConfiguration(accountTtl));
        cacheConfigurations.put(CacheNames.ARTICLE_BY_NAME, createCacheConfiguration(accountTtl));
        cacheConfigurations.put(CacheNames.ARTICLE_BY_PATH, createCacheConfiguration(accountTtl));
        cacheConfigurations.put(CacheNames.ALL_ARTICLES, createCacheConfiguration(accountTtl));

        return cacheConfigurations;
    }

    private RedisCacheConfiguration createCacheConfiguration(Duration ttl) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer))
                .computePrefixWith(cacheName ->
                        redisProperties.getCache().getKeyPrefix() +
                                redisProperties.getCache().getKeySeparator() +
                                cacheName +
                                redisProperties.getCache().getKeySeparator());
        // Conditionally disable caching null values
        if (!redisProperties.getCache().isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        return config;
    }
}
