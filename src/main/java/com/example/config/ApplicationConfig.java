package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 应用配置类
 */
@Configuration
public class ApplicationConfig {

    /**
     * 创建RestTemplate Bean
     * 配置了连接超时和读取超时
     */
    @Bean
    public RestTemplate restTemplate() {
        // 创建请求工厂并设置超时
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 连接超时5秒
        factory.setReadTimeout(5000);     // 读取超时5秒
        
        // 使用自定义工厂创建RestTemplate
        return new RestTemplate(factory);
    }
} 