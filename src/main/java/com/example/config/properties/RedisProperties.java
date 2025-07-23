package com.example.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for Redis connection and caching
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application.redis")
public class RedisProperties {

    /**
     * Enable Redis caching
     */
    private boolean enabled = true;

    /**
     * Redis deployment mode: STANDALONE or CLUSTER
     */
    @NotNull
    private Mode mode = Mode.STANDALONE;

    /**
     * Standalone configuration
     */
    private Standalone standalone = new Standalone();

    /**
     * Cluster configuration
     */
    private Cluster cluster = new Cluster();

    /**
     * Connection pool configuration
     */
    private Pool pool = new Pool();

    /**
     * Cache configuration
     */
    private Cache cache = new Cache();

    /**
     * Security configuration
     */
    private Security security = new Security();

    /**
     * Timeout configuration
     */
    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    public static class Standalone {
        /**
         * Redis server host
         */
        @NotBlank
        private String host = "localhost";

        /**
         * Redis server port
         */
        @Min(1)
        @Max(65535)
        private int port = 6379;

        /**
         * Database index
         */
        @Min(0)
        @Max(15)
        private int database = 0;
    }

    @Getter
    @Setter
    public static class Cluster {
        /**
         * Cluster nodes in format: host1:port1,host2:port2,...
         */
        private String nodes;

        /**
         * Maximum number of redirections to follow when executing commands across the cluster
         */
        @Min(1)
        private int maxRedirects = 3;

        /**
         * Whether to enable adaptive topology refresh
         */
        private boolean adaptiveTopologyRefresh = true;

        /**
         * Whether to enable periodic topology refresh
         */
        private boolean periodicTopologyRefresh = true;

        /**
         * Topology refresh period
         */
        private Duration topologyRefreshPeriod = Duration.ofMinutes(30);
    }

    @Getter
    @Setter
    public static class Pool {
        /**
         * Maximum number of connections in the pool
         */
        @Min(1)
        private int maxTotal = 20;

        /**
         * Maximum number of idle connections in the pool
         */
        @Min(0)
        private int maxIdle = 10;

        /**
         * Minimum number of idle connections in the pool
         */
        @Min(0)
        private int minIdle = 2;

        /**
         * Maximum time to wait for a connection from the pool
         */
        private Duration maxWait = Duration.ofSeconds(10);

        /**
         * Whether to test connections when borrowing from the pool
         */
        private boolean testOnBorrow = true;

        /**
         * Whether to test connections when returning to the pool
         */
        private boolean testOnReturn = false;

        /**
         * Whether to test idle connections in the pool
         */
        private boolean testWhileIdle = true;

        /**
         * Time between eviction runs
         */
        private Duration timeBetweenEvictionRuns = Duration.ofSeconds(30);

        /**
         * Minimum time a connection can be idle before being evicted
         */
        private Duration minEvictableIdleTime = Duration.ofMinutes(1);
    }

    @Getter
    @Setter
    public static class Cache {
        /**
         * Default cache TTL (Time To Live)
         */
        private Duration defaultTtl = Duration.ofHours(1);

        /**
         * Cache key prefix
         */
        private String keyPrefix = "simulator";

        /**
         * Cache key separator
         */
        private String keySeparator = ":";

        /**
         * Whether to enable cache null values
         */
        private boolean cacheNullValues = true;

        /**
         * Entity-specific cache configurations
         */
        private EntityCache entities = new EntityCache();
    }

    @Getter
    @Setter
    public static class EntityCache {
        /**
         * Category cache configuration
         */
        private EntityCacheConfig category = new EntityCacheConfig(Duration.ofHours(24));

        /**
         * Article cache configuration
         */
        private EntityCacheConfig article = new EntityCacheConfig(Duration.ofHours(6));
    }

    @Getter
    @Setter
    public static class EntityCacheConfig {
        /**
         * Cache TTL for this entity
         */
        private Duration ttl;

        /**
         * Maximum number of cached items (0 = unlimited)
         */
        private int maxSize = 1000;

        /**
         * Whether to enable cache warming on startup
         */
        private boolean warmupOnStartup = false;

        public EntityCacheConfig() {
            this.ttl = Duration.ofHours(1);
        }

        public EntityCacheConfig(Duration ttl) {
            this.ttl = ttl;
        }
    }

    @Getter
    @Setter
    public static class Security {
        /**
         * Redis username
         */
        private String username;

        /**
         * Redis password
         */
        private String password;

        /**
         * Whether to enable SSL/TLS
         */
        private boolean ssl = false;

        /**
         * SSL key store path
         */
        private String keyStore;

        /**
         * SSL key store password
         */
        private String keyStorePassword;

        /**
         * SSL trust store path
         */
        private String trustStore;

        /**
         * SSL trust store password
         */
        private String trustStorePassword;
    }

    @Getter
    @Setter
    public static class Timeout {
        /**
         * Connection timeout
         */
        private Duration connection = Duration.ofSeconds(10);

        /**
         * Socket timeout
         */
        private Duration socket = Duration.ofSeconds(10);

        /**
         * Command timeout
         */
        private Duration command = Duration.ofSeconds(5);

        /**
         * Shutdown timeout
         */
        private Duration shutdown = Duration.ofSeconds(100);
    }

    public enum Mode {
        STANDALONE, CLUSTER
    }

    /**
     * Get cluster nodes as a list
     */
    public List<String> getClusterNodesList() {
        if (cluster.nodes == null || cluster.nodes.trim().isEmpty()) {
            return List.of();
        }
        return List.of(cluster.nodes.split(","));
    }

    /**
     * Check if cluster mode is enabled
     */
    public boolean isClusterMode() {
        return mode == Mode.CLUSTER;
    }

    /**
     * Check if standalone mode is enabled
     */
    public boolean isStandaloneMode() {
        return mode == Mode.STANDALONE;
    }
}
