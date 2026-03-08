package com.edu.spring.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * @author Rohit Muneshwar
 */
@Configuration
public class ApplicationConfig {
    @Bean
    RestClient restClient(){
        return RestClient.create();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder){
        return builder.build();
    }

}
