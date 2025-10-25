package com.urlshortener.shortener.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.CacheManager; // We will use Spring's CacheManager directly
import org.springframework.context.annotation.Bean; // To define the CacheManager bean

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    // Define the cache names and specifications using Spring's native Caffeine support
    @Bean
    public CacheManager cacheManager() {
        // We use Spring's SimpleCacheManager and manually register the Caffeine caches
        org.springframework.cache.support.SimpleCacheManager cacheManager = new org.springframework.cache.support.SimpleCacheManager();

        // Create the 'rateLimitCache' with a 60 second (1 minute) expiry policy
        com.github.benmanes.caffeine.cache.Cache<Object, Object> rateLimitCache = Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(1000) // Max 1000 IP addresses in the cache
            .build();

        cacheManager.setCaches(Arrays.asList(
            // Register the cache with the CacheManager
            new org.springframework.cache.caffeine.CaffeineCache("rateLimitCache", rateLimitCache)
        ));

        return cacheManager;
    }
}