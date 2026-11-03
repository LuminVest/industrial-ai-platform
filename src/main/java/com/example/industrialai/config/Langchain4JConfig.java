package com.example.industrialai.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Langchain4JConfig {
    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String qwenApiKey;

    @Bean
    public QwenChatModel qwenWebChatModel() {
        return QwenChatModel.builder()
                .apiKey(qwenApiKey)
                .modelName("qwen-plus")
                .temperature(0.7f)
                .maxTokens(2048)
                .enableSearch(false)
                .build();
    }
}
