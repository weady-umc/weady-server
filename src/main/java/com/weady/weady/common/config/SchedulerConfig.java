package com.weady.weady.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class SchedulerConfig {

    @Value("${scheduler.thread-pool.core-size}")
    private int corePoolSize;
    @Value("${scheduler.thread-pool.max-size}")
    private int maxPoolSize;
    @Value("${scheduler.thread-pool.queue-capacity}")
    private int queueCapacity;

    @Bean(name = "weatherTaskExecutor")
    public Executor weatherTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Weather-");
        executor.initialize();
        return executor;
    }
}