package com.urlshortener.shortener.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity//tells jpa that this class should be mapped to a database table
@Table(name = "url_mappings")
@Getter//lombok annotation to generate getters for all fields
@Setter//lombok annotation to generate setters for all fields
@NoArgsConstructor//lombok annotation to generate a no-argument constructor
public class UrlMapping {

    @Id//tells jpa that this field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)//tells jpa to auto-generate the primary key value
    private long id;

    @Column(nullable = false, unique = true)//tells jpa that this field is a column in the database table, cannot be null, and must be unique
    private String shortCode;//shortcode=true because we can;t have two identical shortcodes

    @Column(nullable = false, length = 2048)//tells jpa that this field is a column in the database table, cannot be null, and has a maximum length of 2048 characters
    private String longUrl;
    // ... inside UrlMapping class ...
    @Column(nullable = false)
    private long clickCount = 0; // New field to track clicks
    
    @Column(nullable = true) // Nullable because not all links expire
private LocalDateTime expiresAt;
    // ... rest of the code ...

    public UrlMapping(String shortCode, String longUrl) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
    }
}