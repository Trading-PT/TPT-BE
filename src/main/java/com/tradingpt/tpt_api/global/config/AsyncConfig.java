package com.tradingpt.tpt_api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(4);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("mail-");
        exec.initialize();
        return exec;
    }

    /**
     * 채점 전용 스레드풀
     */
    @Bean(name = "gradingExecutor")
    public Executor gradingExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(4); // 동시에 여러 채점 가능
        exec.setMaxPoolSize(8);
        exec.setQueueCapacity(200);
        exec.setThreadNamePrefix("grading-");
        exec.initialize();
        return exec;
    }
}
