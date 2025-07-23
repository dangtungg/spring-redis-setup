# Redis Caching Implementation

This document describes the Redis caching implementation for Web application.

## Overview

The caching system provides:

- **Redis-based caching** with support for both standalone and cluster modes
- **Master data caching** for Categories and Articles
- **Automatic cache invalidation** on data updates
- **Cache warming** strategies for improved performance
- **Comprehensive monitoring** and health checks

## Architecture

### Core Components

1. **RedisProperties** - Configuration properties for Redis connection and caching
2. **RedisConfig** - Redis connection factory and template configuration
3. **CacheConfig** - Cache manager and cache name definitions
4. **CacheService** - Service layer for cache operations
5. **CacheInvalidationService** - Cache invalidation strategies
6. **CacheWarmupService** - Cache warming on startup

### Cache Structure

```
spring_redis:{environment}:{cache_name}:{key}
```

- **Environment**: local, dev, prod, etc.
- **Cache Name**: category, article, etc.
- **Key**: Entity identifier or composite key

## Configuration Classes Relationship

### RedisConfig vs CacheConfig

The caching system uses two main configuration classes that work together:

#### **RedisConfig** - Redis Connection & Template Setup

**Purpose**: Establishes the foundation for Redis connectivity and low-level operations.

**Key Responsibilities**:

- Creates `RedisConnectionFactory` for both standalone and cluster modes
- Configures `RedisTemplate<String, Object>` for direct Redis operations
- Sets up serializers (String for keys, JSON for values)
- Manages connection pools, timeouts, and security settings

**Beans Created**:

```java

@Bean
public RedisConnectionFactory redisConnectionFactory() {
    // Creates connection factory based on mode (standalone/cluster)
}

@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    // Configures template for manual Redis operations
}
```

#### **CacheConfig** - Spring Cache Abstraction

**Purpose**: Provides high-level Spring caching functionality and cache management.

**Key Responsibilities**:

- Creates `CacheManager` using the Redis connection factory
- Defines cache names and their specific configurations
- Sets up entity-specific TTL and cache policies
- Manages cache prefixes and key generation strategies

**Beans Created**:

```java

@Bean
public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Creates cache manager with entity-specific configurations
}
```

### How RedisTemplate and CacheManager Work Together

#### **Shared Foundation**

Both beans use the **same** `RedisConnectionFactory`:

- **Single connection pool** to Redis (efficient resource usage)
- **Consistent configuration** (timeouts, authentication, SSL)
- **No duplicate connections** or configuration conflicts

#### **Different Use Cases**

| Component         | Purpose                  | Usage                                                          | Examples                                                     |
|-------------------|--------------------------|----------------------------------------------------------------|--------------------------------------------------------------|
| **RedisTemplate** | Direct Redis operations  | Manual cache operations, TTL management, custom Redis commands | `CacheServiceImpl`, Admin operations, Pattern-based searches |
| **CacheManager**  | Spring Cache abstraction | Annotation-driven caching                                      | `@Cacheable`, `@CacheEvict`, `@CachePut` in service methods  |

#### **Complementary Operations**

**Example 1: Service Layer Caching**

```java
// CacheManager handles this automatically
@Cacheable(value = CacheNames.CATEGORY, key = "#id")
public CategoryDTO findById(Long id) {
    return super.findById(id);
}
```

**Example 2: Manual Cache Operations**

```java
// RedisTemplate used for direct operations
public void setCustomTtl(String cacheName, String key, long ttlSeconds) {
    String redisKey = buildRedisKey(cacheName, key);
    redisTemplate.expire(redisKey, ttlSeconds, TimeUnit.SECONDS);
}
```

**Example 3: Cache Service Implementation**

```java

@Service
public class CacheServiceImpl {
    private final CacheManager cacheManager;    // For Spring cache operations
    private final RedisTemplate redisTemplate;  // For direct Redis operations

    public void put(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);  // Uses CacheManager
        cache.put(key, value);
    }

    public void setTtl(String cacheName, String key, long ttlSeconds) {
        String redisKey = buildRedisKey(cacheName, key);
        redisTemplate.expire(redisKey, ttlSeconds, TimeUnit.SECONDS);  // Uses RedisTemplate
    }
}
```

### Configuration Flow

```mermaid
graph TD
    A[RedisProperties] --> B[RedisConfig]
    A --> C[CacheConfig]
    
    B --> D[RedisConnectionFactory]
    B --> E[RedisTemplate&lt;String, Object&gt;]
    B --> F[ObjectMapper]
    B --> G[Serializers]
    
    D --> E
    D --> H[CacheManager]
    C --> H
    G --> E
    G --> H
    
    E --> I[Direct Redis Operations]
    H --> J[Spring Cache Abstraction]
    
    I --> K[Manual Cache Operations<br/>TTL Management<br/>Custom Keys]
    J --> L[@Cacheable<br/>@CacheEvict<br/>@CachePut]
    
    style A fill:#e1f5fe
    style D fill:#f3e5f5
    style E fill:#fff3e0
    style H fill:#e8f5e8
```

### Benefits of This Architecture

1. **Resource Efficiency**: Single connection pool shared between both components
2. **Flexibility**: Choose the right tool for each use case
3. **Consistency**: Same Redis configuration and serialization
4. **Performance**: Optimal for both declarative and programmatic caching
5. **Maintainability**: Clear separation of concerns

### When to Use Each

**Use CacheManager/Annotations for**:

- Standard CRUD operations caching
- Simple key-based cache invalidation
- Consistent TTL across entity types
- Most business service methods

**Use RedisTemplate/CacheService for**:

- Custom TTL per cache entry
- Complex cache invalidation logic
- Bulk cache operations
- Administrative cache management
- Performance monitoring and optimization

## Configuration

### Local Development (Standalone Redis)

```yaml
# application-local.yaml
application:
  redis:
    enabled: true
    mode: STANDALONE
    standalone:
      host: localhost
      port: 6379
      database: 0
    cache:
      entities:
        category:
          ttl: 24h
          warmup-on-startup: true
        article:
          ttl: 6h
          warmup-on-startup: false
```

### Cloud Deployment (Redis Cluster)

```yaml
# application-prod.yaml
application:
  redis:
    enabled: true
    mode: CLUSTER
    cluster:
      nodes: ${REDIS_CLUSTER_NODES}
      max-redirects: 3
    security:
      username: ${REDIS_USERNAME}
      password: ${REDIS_PASSWORD}
      ssl: true
```

## Entity-Specific Caching

### Category Entities

- **Cache Duration**: 24 hours (stable master data)
- **Cache Keys**:
    - `category:{id}` - By ID
    - `category_by_name:{name}` - By name
    - `category_by_path:{path}` - By path
- **Cache Warming**: Enabled by default

### Article Entities

- **Cache Duration**: 6 hours (balance changes frequently)
- **Cache Keys**:
    - `article:{id}` - By ID
    - `article_by_name:{name}` - By name
    - `article_by_path:{path}` - By path
- **Cache Warming**: Enabled by default

## Cache Operations

### Service Layer Caching

The caching is implemented at the service layer using Spring's caching annotations:

```java

@Cacheable(value = CacheNames.CATEGORY_BY_NAME, key = "#name")
public Category getByName(String name) {
    // Database query only on cache miss
}

@CacheEvict(value = CacheNames.CATEGORY, key = "#dto.id")
public CategoryDTO update(CategoryDTO dto) {
    // Cache invalidation on update
}
```

### Manual Cache Operations

#### **Programmatic Operations**

```java

@Autowired
private CacheService cacheService;

// Manual cache operations
cacheService.put("category","123",categoryEntity);

Optional<Category> category = cacheService.get("category", "123", Category.class);
cacheService.evict("category","123");
```

#### **Administrative REST API**

The `CacheManagementController` provides REST endpoints for cache administration:

```bash
# Get cache entry
GET /api/admin/cache/{cacheName}/{key}

# Store cache entry
PUT /api/admin/cache/{cacheName}/{key}
Content-Type: application/json
{"id": 123, "name": "Test", "path": "/mock/path"}

# Remove cache entry  
DELETE /api/admin/cache/{cacheName}/{key}

# Get cache statistics
GET /api/admin/cache/{cacheName}/stats

# Warm up caches
POST /api/admin/cache/warmup/{entityType}

# Emergency cache operations
DELETE /api/admin/cache/entities/all

# TTL management
PUT /api/admin/cache/{cacheName}/{key}/ttl?ttlSeconds=3600
GET /api/admin/cache/{cacheName}/{key}/ttl

# Health check
GET /api/admin/cache/health
```

**Use Cases**:

- **DevOps troubleshooting** and performance monitoring
- **Emergency cache management** during incidents
- **Cache warming** and optimization
- **Development and testing** support

## Cache Invalidation

### Automatic Invalidation

Cache invalidation happens automatically on:

- **Create operations** - Evict list caches
- **Update operations** - Evict entity and related caches
- **Delete operations** - Evict all related caches

### Manual Invalidation

```java

@Autowired
private CacheInvalidationService invalidationService;

// Invalidate specific category caches
invalidationService.invalidateCategoryCaches(categoryId, categoryName, categoryPath);

// Invalidate all master data caches
invalidationService.invalidateAllMasterDataCaches();
```

## Cache Warming

### Startup Warming

Cache warming occurs automatically on application startup for configured entities:

```java

@EventListener(ApplicationReadyEvent.class)
public void warmUpCachesOnStartup() {
    // Warm up critical caches in parallel
}
```

### Manual Warming

```java

@Autowired
private CacheWarmupService warmupService;

// Warm up specific entity caches
warmupService.warmUpEntityCache("category");
warmupService.warmUpEntityCache("all");
```

## Monitoring and Health Checks

### Cache Statistics

```java

@Autowired
private CacheInvalidationService invalidationService;

// Log cache statistics
invalidationService.logCacheStatistics();

// Check cache health
boolean healthy = invalidationService.isHealthy();
```

### Cache Metrics

The system provides metrics for:

- Cache hit/miss ratios
- Cache sizes
- Cache warming status
- Connection health

## Performance Considerations

### Cache Hit Rates

Expected cache hit rates: 95%+

### Memory Usage

Estimated memory usage per entity: ~5KB

### TTL Strategy

- **Long TTL (24h)**: Stable master data (Categories)
- **Medium TTL (6-12h)**: Semi-stable data (Articles)

## Best Practices

### Development

1. **Use local Redis**: Run Redis locally for development
2. **Enable cache warming**: For frequently accessed entities
3. **Monitor cache statistics**: Use logging to track performance
4. **Test cache invalidation**: Verify cache updates work correctly

### Production

1. **Use Redis Cluster**: For high availability
2. **Enable SSL/TLS**: For secure connections
3. **Monitor cache health**: Set up alerting for cache failures
4. **Tune TTL values**: Based on actual usage patterns

### Troubleshooting

1. **Cache misses**: Check TTL configuration and warming status
2. **Memory issues**: Monitor cache sizes and implement eviction policies
3. **Connection errors**: Verify Redis cluster health and network connectivity
4. **Performance degradation**: Check cache hit rates and optimize queries

## Environment Variables

### Local Development

```bash
# Optional - defaults work for local Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE=0
```

### Cloud Deployment

```bash
# Required for cluster mode
REDIS_CLUSTER_NODES=redis-1:6379,redis-2:6379,redis-3:6379
REDIS_USERNAME=your_username
REDIS_PASSWORD=your_password
REDIS_SSL_ENABLED=true
```

## Migration Guide

### From Non-Cached to Cached

1. **Enable Redis**: Set `application.redis.enabled=true`
2. **Configure connection**: Set standalone or cluster mode
3. **Test caching**: Verify cache operations work correctly
4. **Monitor performance**: Check cache hit rates and response times

### Cache Configuration Changes

1. **TTL changes**: Update entity TTL values in configuration
2. **Warming changes**: Enable/disable cache warming per entity
3. **Key changes**: Update cache keys (requires cache invalidation)

## FAQ

### Q: How do I disable caching for testing?

A: Set `application.redis.enabled=false` in your test configuration.

### Q: How do I clear all caches?

A: Use `CacheInvalidationService.invalidateAllMasterDataCaches()` or restart the application.

### Q: Why are my cache hit rates low?

A: Check TTL configuration, ensure cache warming is enabled, and verify cache keys are correct.

### Q: How do I monitor cache performance?

A: Use the built-in cache statistics logging or integrate with monitoring tools like Micrometer.

### Q: Can I use a different cache provider?

A: The implementation uses Spring's caching abstraction, so you can switch providers by changing the CacheManager
configuration.

## Support

For issues or questions regarding the caching implementation:

1. Check the application logs for cache-related errors
2. Verify Redis connectivity and cluster health
3. Review cache statistics and metrics
4. Consult the troubleshooting section above 