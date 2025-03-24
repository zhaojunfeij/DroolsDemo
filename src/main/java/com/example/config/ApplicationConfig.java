package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 应用配置类
 */
@Configuration
public class ApplicationConfig {

    /**
     * 创建RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 