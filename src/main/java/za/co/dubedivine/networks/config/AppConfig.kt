package za.co.dubedivine.networks.config

import org.springframework.cache.annotation.CacheConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@ComponentScan(basePackages = arrayOf("za.co.dubedivine.networks.threads"))
class AppConfig {
    @Bean
    fun taskExecutor(): ThreadPoolTaskExecutor {
        val pool: ThreadPoolTaskExecutor = ThreadPoolTaskExecutor()
        pool.corePoolSize = 5
        pool.maxPoolSize = 5
        pool.setWaitForTasksToCompleteOnShutdown(true)
        return pool
    }

}

