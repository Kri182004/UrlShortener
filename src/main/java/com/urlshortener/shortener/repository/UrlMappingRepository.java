package com.urlshortener.shortener.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.urlshortener.shortener.model.UrlMapping;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
  Optional<UrlMapping> findByShortCode(String shortCode);
  boolean existsByShortCode(String shortCode);//
  @Transactional
    @Modifying // Tells JPA this query modifies data (deletes)
    @Query("DELETE FROM UrlMapping m WHERE m.expiresAt < :currentTime")
    int deleteByExpiresAtBefore(@Param("currentTime") LocalDateTime currentTime);
  /**"Hey database, does ANY row exist in the 'url_mappings' table where the 'short_code' column is equal to 'aB3xY9k'?"

It will then return a simple true (if it finds one) or false (if it doesn't).

This is much more efficient than our other method (findByShortCode)
 when you only need to check for existence, 
 because it doesn't waste time pulling all the data (like the long URL) 
 from the database. 
 It just gets a yes/no answer. */
  //By naming the method this way, Spring Data JPA automatically
  // understands we want to search the UrlMapping table for a row where the shortCode column matches the string we provide.
  //This is a modern Java feature that handles cases where a result might not exist (i.e., we search for a short code that isn't in our database).
  // It's a cleaner way to avoid NullPointerException
//we are extending JpaRepository to get basic CRUD operations for free
//This automatically gives us methods like save(), findById(), findAll(), and delete()
}