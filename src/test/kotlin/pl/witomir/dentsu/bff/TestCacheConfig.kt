package pl.witomir.dentsu.bff

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestCacheConfig {
    @Bean
    fun cacheManager(): CacheManager = NoOpCacheManager()
}
