package za.co.dubedivine.networks.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@ComponentScan(basePackages = arrayOf("za.co.dubedivine.networks.threads"))
//https://www.baeldung.com/spring-data-mongodb-gridfs
//class AppConfig(val mon: MongoDbFactory) {
class AppConfig {
    @Bean
    fun taskExecutor(): ThreadPoolTaskExecutor {
        val pool = ThreadPoolTaskExecutor()
        pool.corePoolSize = 3
        pool.maxPoolSize = 3
        pool.setWaitForTasksToCompleteOnShutdown(true)
        return pool
    }
}

