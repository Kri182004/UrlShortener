

package com.urlshortener.shortener.limiter;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RateLimiter {

    private final Cache rateLimitCache;
    private static final int MAX_REQUESTS = 5; // Max requests per 60 seconds

    public RateLimiter(CacheManager cacheManager) {
        // Get the cache we defined in CacheConfig
        this.rateLimitCache = cacheManager.getCache("rateLimitCache");
    }

    /**
     * Checks if the given IP address has exceeded the allowed request limit.
     * @param ipAddress The unique identifier for the user.
     */
    public void checkLimit(String ipAddress) {
        // Get the current count for this IP, defaulting to 0 if not found
        Cache.ValueWrapper wrapper = rateLimitCache.get(ipAddress);
        Integer currentCount = (wrapper != null) ? (Integer) wrapper.get() : 0;

        if (currentCount >= MAX_REQUESTS) {
            // Throw a 429 Too Many Requests error
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, 
                "Rate limit exceeded. You can only create " + MAX_REQUESTS + " links per minute.");
        }

        // Increment and put the new count back in the cache
        rateLimitCache.put(ipAddress, currentCount + 1);
    }
}