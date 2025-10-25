package com.urlshortener.shortener.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching // CRUCIAL: Enables the caching system
public class RedisConfig {

    // This bean tells Spring how to configure the 'urls' cache used in the Service layer
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            // Set the cache to expire after a default time (e.g., 60 minutes)
            .entryTtl(Duration.ofMinutes(60)) 
            // Use String serializer for cache keys (e.g., "redis-test")
            .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
            // Use JSON serializer for cache values (the actual URL data)
            .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}