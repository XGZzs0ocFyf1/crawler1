package ru.gurzhiy.crawler.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.gurzhiy.crawler.model.Pair;

import java.util.Queue;


@Configuration
public class BasicConfiguration {

    @Value("${maxThreadPoolSize}")
    private int maxThreadPoolSize;

    //    executor для поисковика
    @Bean("crawlerPool")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("CrawlerPool-");
        executor.initialize();
        return executor;
    }



}
