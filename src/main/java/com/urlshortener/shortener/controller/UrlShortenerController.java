package com.urlshortener.shortener.controller;

import com.urlshortener.shortener.dto.ClickAnalyticsResponse;
import com.urlshortener.shortener.model.UrlMapping; // Added for Analytics endpoint
import com.urlshortener.shortener.repository.UrlMappingRepository; // Added for dependency injection and Analytics endpoint
import com.urlshortener.shortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional; // Added for Analytics endpoint
import java.util.stream.Collectors;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final UrlMappingRepository urlMappingRepository; // Field added for Analytics endpoint

    // Corrected Constructor (Initializes both Service and Repository)
    public UrlShortenerController(UrlShortenerService urlShortenerService,
                                  UrlMappingRepository urlMappingRepository) {
        this.urlShortenerService = urlShortenerService;
        this.urlMappingRepository = urlMappingRepository;
    }

    /**
     * API endpoint to create a new short URL.
     * Handles both required (longUrl) and optional (customCode) parameters.
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<String> shortenUrl(
            @RequestParam String longUrl,
            @RequestParam(required = false) String customCode) {
        
        // Calls the service with both URL and optional custom code
        String shortCode = urlShortenerService.shortenUrl(longUrl, customCode);

        // Returns the full short URL using the /r/ prefix
        String shortUrl = "http://localhost:8080/r/" + shortCode;
        return ResponseEntity.ok(shortUrl);
    }

    /**
     * The main redirect endpoint.
     * Maps to GET http://localhost:8080/r/{shortCode}
     */
    @GetMapping("/r/{shortCode}")
    public void redirectToLongUrl(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        // The service layer handles finding the URL and incrementing the click counter
        String longUrl = urlShortenerService.getLongUrl(shortCode);
        
        // Sends HTTP 302 Redirect to the client's browser
        response.sendRedirect(longUrl);
    }

    /**
     * API endpoint to retrieve the click count for a specific short code (Analytics).
     * e.g., GET http://localhost:8080/api/r/aB3xY9k/clicks
     */
    /**
     * API endpoint to retrieve the click count for a specific short code (Analytics).
     * Now returns a clean JSON object.
     * e.g., GET http://localhost:8080/api/r/aB3xY9k/clicks
     */
    @GetMapping("/api/r/{shortCode}/clicks")
    public ResponseEntity<ClickAnalyticsResponse> getClickCount(@PathVariable String shortCode) {
        
        // Try to find the URL mapping using the repository
        Optional<UrlMapping> mappingOptional = urlMappingRepository.findByShortCode(shortCode);
        
        if (mappingOptional.isEmpty()) {
            // Re-throw exception, which will be caught by the general exception handler
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
    /**
     * API endpoint to retrieve ALL short links for the dashboard list view.
     * e.g., GET http://localhost:8080/api/all-links
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
        } else if (message.contains("already taken") || message.contains("must be at least")) {
            status = HttpStatus.BAD_REQUEST; // 400 - For client input/logic errors
        } else {
            // For general, unexpected errors
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }

        // Return the simple error message back to the frontend
        return ResponseEntity
                .status(status)
                .body(message);
    }
}