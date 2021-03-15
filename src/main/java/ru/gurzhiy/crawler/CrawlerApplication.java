package ru.gurzhiy.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
public class CrawlerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }



    // для запуска на стороннем контейнере Tomcat
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CrawlerApplication.class);
    }
}
