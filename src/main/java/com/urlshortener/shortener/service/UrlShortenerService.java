package com.urlshortener.shortener.service;

import com.urlshortener.shortener.model.UrlMapping;
import com.urlshortener.shortener.repository.UrlMappingRepository;
import org.springframework.cache.annotation.CacheEvict; // NEW IMPORT
import org.springframework.cache.annotation.Cacheable; // NEW IMPORT
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.EnableCaching;

import java.time.LocalDateTime;
import java.util.Random; 

// Note: No need for java.util.stream.Collectors import here

@Service
public class UrlShortenerService {

    // ... (Existing fields and constructor remain unchanged)

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private final Random random = new Random();

    private final UrlMappingRepository urlMappingRepository;

    public UrlShortenerService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    /**
     * Creates a short URL and evicts any existing cache entry with the same shortCode.
     * @param longUrl The original URL.
     * @param customCode Optional custom code.
     * @param expirationHours Optional hours until expiration.
     */
    @CacheEvict(value = "urls", key = "#result") // Clears the cache using the generated/custom shortCode
    public String shortenUrl(String longUrl, String customCode, Integer expirationHours) {
        String finalCode;
        
        if (customCode != null && !customCode.isEmpty()) {
            // ... (Existing custom code validation and uniqueness check) ...
            finalCode = customCode;

            if (finalCode.length() < 3 || finalCode.contains(" ") || !finalCode.matches("[a-zA-Z0-9_-]+")) {
                throw new RuntimeException("Custom code must be at least 3 characters and can only contain letters, numbers, hyphens, or underscores.");
            }
            if (urlMappingRepository.existsByShortCode(finalCode)) {
                throw new RuntimeException("Custom short code '" + finalCode + "' is already taken.");
            }
        } else {
            // ... (Existing random code generation logic) ...
            do {
                finalCode = generateShortCode();
            } while (urlMappingRepository.existsByShortCode(finalCode));
        }

        UrlMapping newMapping = new UrlMapping(finalCode, longUrl);
        
        if (expirationHours != null && expirationHours > 0) {
            newMapping.setExpiresAt(LocalDateTime.now().plusHours(expirationHours)); 
        }
        
        urlMappingRepository.save(newMapping);
        return finalCode;
    }

    /**
     * Retrieves the long URL, checking the Redis cache first.
     * If not found in cache, retrieves from PostgreSQL (DB) and puts it in Redis automatically.
     * @param shortCode The short link alias.
     */
    @Cacheable(value = "urls", key = "#shortCode") // Checks Redis first using the shortCode as the key
    public String getLongUrl(String shortCode) {
        
        // This logic runs ONLY if the shortCode is NOT in Redis cache
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short code not found: " + shortCode));
        
        // Expiration Check (Must run regardless of cache status)
        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Short link has expired and is no longer active.");
        }
        
        // Increment the counter (Only if not expired)
        mapping.setClickCount(mapping.getClickCount() + 1);
        urlMappingRepository.save(mapping);
        
        // Returns the long URL. Spring handles putting this result into Redis using 'shortCode' as the key.
        return mapping.getLongUrl();
    }

    // ... (Existing generateShortCode helper method remains unchanged) ...

    private String generateShortCode() {
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            codeBuilder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return codeBuilder.toString();
    }
}