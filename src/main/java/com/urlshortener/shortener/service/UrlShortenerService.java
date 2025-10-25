package com.urlshortener.shortener.service;

import com.urlshortener.shortener.model.UrlMapping;
import com.urlshortener.shortener.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;

import java.util.Random; // <-- This is the crucial import you were missing!

@Service
public class UrlShortenerService {

    // These fields are needed for the random code generation
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private final Random random = new Random();

    private final UrlMappingRepository urlMappingRepository;

    // Constructor-based dependency injection
    public UrlShortenerService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    /**
     * Creates a short URL for the given long URL, optionally using a custom short code.
     */
    public String shortenUrl(String longUrl, String customCode) {
        String finalCode;
        
        if (customCode != null && !customCode.isEmpty()) {
            // Case 1: Custom code was provided
            finalCode = customCode;

            // Validation: Custom code cannot have spaces, must be a decent length, and alphanumeric
            if (finalCode.length() < 3 || finalCode.contains(" ") || !finalCode.matches("[a-zA-Z0-9_-]+")) {
                throw new RuntimeException("Custom code must be at least 3 characters and can only contain letters, numbers, hyphens, or underscores.");
            }

            // Check if it's already taken in the database
            if (urlMappingRepository.existsByShortCode(finalCode)) {
                throw new RuntimeException("Custom short code '" + finalCode + "' is already taken.");
            }
        } else {
            // Case 2: No custom code, generate a random one (Existing logic)
            do {
                finalCode = generateShortCode();
            } while (urlMappingRepository.existsByShortCode(finalCode));
        }

        // Save the new mapping using the final code (either custom or random)
        UrlMapping newMapping = new UrlMapping(finalCode, longUrl);
        urlMappingRepository.save(newMapping);

        return finalCode;
    }

    /**
     * Retrieves the long URL associated with a given short code.
     */
    /**
     * Retrieves the long URL associated with a given short code and increments the click counter.
     */
    public String getLongUrl(String shortCode) {
        // 1. Find the mapping. If not found, throws exception (existing logic)
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short code not found: " + shortCode));
        
        // 2. INCREMENT THE COUNTER
        mapping.setClickCount(mapping.getClickCount() + 1);

        // 3. Save the updated mapping back to the database
        urlMappingRepository.save(mapping);
        
        // 4. Return the long URL for redirection (existing logic)
        return mapping.getLongUrl();
    }

    /**
     * Helper method to generate a random alphanumeric string of CODE_LENGTH.
     */
    private String generateShortCode() {
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            codeBuilder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return codeBuilder.toString();
    }
}