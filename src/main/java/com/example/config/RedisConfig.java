package com.example.config;

import com.example.config.properties.RedisProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * Redis connection factory and template configuration
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnProperty(prefix = "application.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        if (redisProperties.isClusterMode()) {
            return createClusterConnectionFactory();
        } else {
            return createStandaloneConnectionFactory();
        }
    }

    private RedisConnectionFactory createStandaloneConnectionFactory() {
        log.info("Creating Redis standalone connection factory");

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.getStandalone().getHost());
        configuration.setPort(redisProperties.getStandalone().getPort());
        configuration.setDatabase(redisProperties.getStandalone().getDatabase());

        // Set authentication if provided
        if (redisProperties.getSecurity().getUsername() != null) {
            configuration.setUsername(redisProperties.getSecurity().getUsername());
        }
        if (redisProperties.getSecurity().getPassword() != null) {
            configuration.setPassword(RedisPassword.of(redisProperties.getSecurity().getPassword()));
        }

        JedisConnectionFactory factory = new JedisConnectionFactory(configuration, createJedisClientConfiguration());
        factory.afterPropertiesSet();

        log.info("Redis standalone connection factory created for {}:{}",
                redisProperties.getStandalone().getHost(), redisProperties.getStandalone().getPort());

        return factory;
    }

    private RedisConnectionFactory createClusterConnectionFactory() {
        log.info("Creating Redis cluster connection factory");

        List<String> clusterNodes = redisProperties.getClusterNodesList();
        if (clusterNodes.isEmpty()) {
            throw new IllegalArgumentException("Redis cluster nodes must be configured when using cluster mode");
        }

        RedisClusterConfiguration configuration = new RedisClusterConfiguration(clusterNodes);
        configuration.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());

        // Set authentication if provided
        if (redisProperties.getSecurity().getUsername() != null) {
            configuration.setUsername(redisProperties.getSecurity().getUsername());
        }
        if (redisProperties.getSecurity().getPassword() != null) {
            configuration.setPassword(RedisPassword.of(redisProperties.getSecurity().getPassword()));
        }

        JedisConnectionFactory factory = new JedisConnectionFactory(configuration, createJedisClientConfiguration());
        factory.afterPropertiesSet();

        log.info("Redis cluster connection factory created for nodes: {}", clusterNodes);

        return factory;
    }

    private JedisClientConfiguration createJedisClientConfiguration() {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();

        // Configure timeouts
        builder.connectTimeout(redisProperties.getTimeout().getConnection())
                .readTimeout(redisProperties.getTimeout().getSocket())
                .usePooling()
                .poolConfig(createJedisPoolConfig());

        // Configure SSL if enabled
        if (redisProperties.getSecurity().isSsl()) {
            builder.useSsl();
        }

        return builder.build();
    }

    private JedisPoolConfig createJedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        RedisProperties.Pool pool = redisProperties.getPool();

        poolConfig.setMaxTotal(pool.getMaxTotal());
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMinIdle(pool.getMinIdle());
        poolConfig.setMaxWait(pool.getMaxWait());
        poolConfig.setTestOnBorrow(pool.isTestOnBorrow());
        poolConfig.setTestOnReturn(pool.isTestOnReturn());
        poolConfig.setTestWhileIdle(pool.isTestWhileIdle());
        poolConfig.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
        poolConfig.setMinEvictableIdleTime(pool.getMinEvictableIdleTime());

        return poolConfig;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(createObjectMapper());

        // Key serialization
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value serialization
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // Enable transaction support
        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();

        log.info("RedisTemplate configured with Jackson JSON serialization");

        return template;
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        return createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());          // Support for Java 8 Date/Time API
        objectMapper.registerModule(new ParameterNamesModule());    // Need for Page/PageImpl
        objectMapper.registerModule(new Hibernate6Module());        // Support for Hibernate 6
        objectMapper.findAndRegisterModules();
        objectMapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Configure date/time serialization to use ISO strings instead of array format [2025, 7, 15, 12, 38, 2, 688925000]
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // This ensures consistent precision handling
        objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        // Set visibility for all fields
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        return objectMapper;
    }

    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }

    @Bean
    public GenericJackson2JsonRedisSerializer jsonRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer(createObjectMapper());
    }
}
