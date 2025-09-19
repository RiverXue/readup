package com.xreadup.ai.config;

import com.xreadup.ai.service.AiToolService;
import com.xreadup.ai.service.TencentTranslateService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                // 移除Function Calling配置，简化架构
                .build();
    }
    
    // 移除Function Calling工具Bean
    // 如果后续需要，可以重新添加
}