package com.urlshortener.shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickAnalyticsResponse {

    private String shortCode;
    private String longUrl;
    private long clickCount;
}