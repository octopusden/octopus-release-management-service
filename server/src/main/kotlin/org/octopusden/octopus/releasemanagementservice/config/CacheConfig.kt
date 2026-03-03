package org.octopusden.octopus.releasemanagementservice.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {
    @Bean
    fun cacheManager(
        @Value("\${cache.detailed-component.ttl-minutes:10}")
        ttlMinutes: Long,
        @Value("\${cache.detailed-component.maximum-size:1000}")
        maximumSize: Long
    ): CacheManager {
        val cacheManager = CaffeineCacheManager("detailedComponentCache")
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
        )
        return cacheManager
    }
}