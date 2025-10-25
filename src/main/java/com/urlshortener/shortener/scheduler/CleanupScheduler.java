// In src/main/java/com/urlshortener/shortener/scheduler/CleanupScheduler.java

package com.urlshortener.shortener.scheduler;

import com.urlshortener.shortener.repository.UrlMappingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CleanupScheduler {

    private final UrlMappingRepository urlMappingRepository;

    public CleanupScheduler(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    /**
     * Runs every hour (3,600,000 milliseconds) to find and delete expired links.
     */
    @Scheduled(fixedRate = 3600000) 
    public void removeExpiredLinks() {
        // Calls the custom query method we added to the repository
        int deletedCount = urlMappingRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        
        // This line will print to your console every hour if links are deleted
        System.out.println("CleanupScheduler: Removed " + deletedCount + " expired links."); 
    }
}