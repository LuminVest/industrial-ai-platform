package com.example.industrialai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")// 允许所有来源
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")// 所有方法
                        .allowedHeaders("*")// 允许所有请求头
                        .allowCredentials(true)// 允许Cookie/认证信息（上传必备）
                        .maxAge(3600);// 预检请求缓存1小时
            }
        };
    }
}