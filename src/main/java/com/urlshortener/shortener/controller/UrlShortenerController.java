package com.urlshortener.shortener.controller;

import com.urlshortener.shortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    /**
     * API endpoint to create a new short URL.
     * We'll accept the long URL as a request parameter.
     * e.g., POST http://localhost:8080/api/shorten?longUrl=https://...
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<String> shortenUrl(@RequestParam String longUrl) {
        String shortCode = urlShortenerService.shortenUrl(longUrl);
        // We'll return the full short URL to the user
        String shortUrl = "http://localhost:8080/r/" + shortCode; 
        return ResponseEntity.ok(shortUrl);
    }

    /**
     * The main redirect endpoint.
     * This catches requests to the root path with a short code.
     * e.g., GET http://localhost:8080/aB3xY9k
     */
    @GetMapping("/r/{shortCode}")
    public void redirectToLongUrl(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        String longUrl = urlShortenerService.getLongUrl(shortCode);
        
        // Send an HTTP 302 Redirect response to the user's browser
        response.sendRedirect(longUrl);
    }

    /**
     * Exception handler for when a short code is not found.
     * This catches the RuntimeException we threw in the service.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleNotFound(RuntimeException ex) {
        // Return a 404 Not Found response
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
}