package com.urlshortener.shortener.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.urlshortener.shortener.model.UrlMapping;
import com.urlshortener.shortener.repository.UrlMappingRepository;

@Service
public class UrlShortenerService {
    private final UrlMappingRepository urlMappingRepository;
    private static final String CHARACTERS= "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH=7;
    private final Random random=new Random();
    //constructor based ddependency injection
    public UrlShortenerService(UrlMappingRepository urlMappingRepository){
        this.urlMappingRepository=urlMappingRepository;
    }
    /**it creates a short URL for the given long URLs
     * Generates a unique shortcode and saves the mapping in the database
     */
    public String shortenUrl(String longUrl){
        String shortCode;
    //keep generating short codes until we find a unique one
        do{
            shortCode=generateShortCode();
        }while(urlMappingRepository.existsByShortCode(shortCode));
        urlMappingRepository.save(new UrlMapping(shortCode,longUrl));
        return shortCode;
    }
    /**
     * Helper method to generate a random alphanumeric short code
     */
    private String generateShortCode(){
        StringBuilder codeBuilder=new StringBuilder();
        for(int i=0;i<CODE_LENGTH;i++){
            codeBuilder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return codeBuilder.toString();

    }
    /**
     * Retrieves the long URL associated with a given short code.
     */
    public String getLongUrl(String shortCode) {
        // Find the mapping by short code.
        // We use the findByShortCode method we defined in the repository.
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                // If it doesn't exist, throw an exception.
                .orElseThrow(() -> new RuntimeException("Short code not found: " + shortCode));
        
        // If it *does* exist, return the longUrl from the mapping object.
        return mapping.getLongUrl();
    }
}
