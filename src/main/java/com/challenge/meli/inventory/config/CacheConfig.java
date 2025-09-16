package com.challenge.meli.inventory.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats());
        return manager;
    }
}

