package com.edu.spring.ai.service;

import com.edu.spring.ai.advisor.TokenCalculatorAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author Rohit Muneshwar
 * @created on 2/18/2026
 *
 *
 */
@Service
public class SQLiteChatMessageService {
    private final ChatClient chatClient;
    public SQLiteChatMessageService(ChatClient.Builder chatClientBuilder,
                                    ChatOptions chatOptions, JdbcChatMemoryRepository jdbcChatMemoryRepository){
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(100).build();
        this.chatClient = chatClientBuilder.defaultOptions(chatOptions)
                .defaultAdvisors(new TokenCalculatorAdvisor(),
                        MessageChatMemoryAdvisor.builder(memory).build())
                .build();
    }

    public Flux<String> getMessage(String userId, String query){
        return chatClient.prompt(query)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, userId))
                .stream().content();
    }
}
