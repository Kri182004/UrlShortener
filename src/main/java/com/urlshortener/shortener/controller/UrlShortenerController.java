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
    /**
     * API endpoint to create a new short URL.
     * Accepts longUrl and optional customCode.
     * e.g., POST http://localhost:8080/api/shorten?longUrl=https://...&customCode=xyz
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<String> shortenUrl(
            @RequestParam String longUrl,
            @RequestParam(required = false) String customCode) { // 1. ADD NEW PARAMETER
        
        // 2. PASS BOTH PARAMETERS to the updated service method.
        String shortCode = urlShortenerService.shortenUrl(longUrl, customCode);

        // 3. Ensure the service response is handled correctly
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
    /**
     * Exception handler to catch service errors (like code not found or already taken)
     * and return a user-friendly 400 or 404 response.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleServiceException(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (message.contains("not found")) {
            status = HttpStatus.NOT_FOUND; // 404
        } else if (message.contains("already taken") || message.contains("must be at least")) {
            status = HttpStatus.BAD_REQUEST; // 400 - For user input errors
        } else {
            // For general unexpected errors (Internal Server Error)
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred on the server.");
        }

        return ResponseEntity
                .status(status)
                .body(message);
    }
}