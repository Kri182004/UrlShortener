package com.urlshortener.shortener.controller;

import com.urlshortener.shortener.dto.ClickAnalyticsResponse;
import com.urlshortener.shortener.model.UrlMapping; 
import com.urlshortener.shortener.repository.UrlMappingRepository; 
import com.urlshortener.shortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest; // Added for Rate Limiting/Geolocation

import com.urlshortener.shortener.limiter.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional; 
import java.util.stream.Collectors;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final UrlMappingRepository urlMappingRepository; 
     private final RateLimiter rateLimiter; // Assuming RateLimiter is commented out or removed for now
    // private final GeoLocatorService geoLocatorService; // Assuming Geolocation is commented out or removed for now

    // Corrected Constructor (Only using core dependencies needed by this code)
    public UrlShortenerController(UrlShortenerService urlShortenerService,
                                  UrlMappingRepository urlMappingRepository,RateLimiter rateLimiter) {
        this.urlShortenerService = urlShortenerService;
        this.urlMappingRepository = urlMappingRepository;
        this.rateLimiter = rateLimiter;
    }

    //------------------------------------------------------------------------------------------------------
    
    /**
     * API endpoint to create a new short URL.
     * Handles longUrl, customCode, and expirationHours parameters.
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<String> shortenUrl(
            @RequestParam String longUrl,
            @RequestParam(required = false) String customCode,
            @RequestParam(required = false) Integer expirationHours
             , HttpServletRequest request ) { // Rate Limiter dependency removed for simplicity here
        
                String ipAddress = request.getRemoteAddr();
        rateLimiter.checkLimit(ipAddress);
        // Calls the service with all parameters (expirationHours handles expiration logic)
        String shortCode = urlShortenerService.shortenUrl(longUrl, customCode, expirationHours);

        // Returns the full short URL using the /r/ prefix
        String shortUrl = "http://localhost:8080/r/" + shortCode;
        return ResponseEntity.ok(shortUrl);
    }

    //------------------------------------------------------------------------------------------------------
    
    /**
     * The main redirect endpoint. Increments click count in the Service layer.
     */
    @GetMapping("/r/{shortCode}")
    public void redirectToLongUrl(@PathVariable String shortCode, 
                                  HttpServletResponse response 
                                  /* , HttpServletRequest request */) throws IOException { // GeoIP dependency removed for simplicity here
        
        // The service layer handles finding the URL and incrementing the click counter
        // String ipAddress = request.getRemoteAddr(); // Used for Geolocation/Rate Limiting
        // String longUrl = urlShortenerService.getLongUrl(shortCode, ipAddress); // Original call with IP
        String longUrl = urlShortenerService.getLongUrl(shortCode); // Simplified call
        
        // Sends HTTP 302 Redirect to the client's browser
        response.sendRedirect(longUrl);
    }

    //------------------------------------------------------------------------------------------------------

    /**
     * API endpoint to retrieve the click count for a specific short code (Analytics).
     * Returns a clean JSON object (DTO).
     */
    @GetMapping("/api/r/{shortCode}/clicks")
    public ResponseEntity<ClickAnalyticsResponse> getClickCount(@PathVariable String shortCode) {
        
        // Try to find the URL mapping using the repository
        Optional<UrlMapping> mappingOptional = urlMappingRepository.findByShortCode(shortCode);
        
        if (mappingOptional.isEmpty()) {
            // Throw exception for 404 handler to catch
            throw new RuntimeException("Short code not found: " + shortCode);
        }

        UrlMapping mapping = mappingOptional.get();
        
        // Create the clean DTO object
        ClickAnalyticsResponse responseDto = new ClickAnalyticsResponse(
            mapping.getShortCode(),
            mapping.getLongUrl(),
            mapping.getClickCount()
        );
                                         
        return ResponseEntity.ok(responseDto);
    }

    //------------------------------------------------------------------------------------------------------

    /**
     * API endpoint to retrieve ALL short links for the dashboard list view.
     */
    @GetMapping("/api/all-links")
    public ResponseEntity<List<ClickAnalyticsResponse>> getAllLinks() {
        
        // Use the repository to fetch all UrlMapping records from the database
        List<UrlMapping> allMappings = urlMappingRepository.findAll();
        
        // Convert the list of UrlMapping entities into a list of DTOs
        List<ClickAnalyticsResponse> responseList = allMappings.stream()
            .map(mapping -> new ClickAnalyticsResponse(
                mapping.getShortCode(),
                mapping.getLongUrl(),
                mapping.getClickCount()
            ))
            .collect(Collectors.toList());
                                         
        return ResponseEntity.ok(responseList);
    }

    //------------------------------------------------------------------------------------------------------
    
    /**
     * Centralized Exception handler to catch RuntimeExceptions (like 'not found' or 'already taken') 
     * and return a user-friendly JSON response with the correct HTTP status code (400, 404, 500).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleServiceException(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (message.contains("not found")) {
            status = HttpStatus.NOT_FOUND; // 404
        } else if (message.contains("already taken") || message.contains("must be at least") || message.contains("expired")) {
            status = HttpStatus.BAD_REQUEST; // 400 - For client input/logic errors
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }

        // Return the simple error message back to the frontend
        return ResponseEntity
                .status(status)
                .body(message);
    }
}