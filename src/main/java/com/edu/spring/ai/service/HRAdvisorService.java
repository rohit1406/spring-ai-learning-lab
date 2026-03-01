package com.edu.spring.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Rohit Muneshwar
 * @created on 2/16/2026
 *
 *
 */
@Service
public class HRAdvisorService {
    @Value("classpath:/prompts/hr-assistant-system-message.st")
    private Resource hrAssistantSystemMessage;
    private ChatClient chatClient;

    public HRAdvisorService(ChatClient.Builder chatClientBuilder,
                            ChatOptions chatOptions, ChatMemory chatMemory){
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        SafeGuardAdvisor safeGuardAdvisor = new SafeGuardAdvisor(
                List.of("Family matters", "relationships", "hobbies", "interest", "personal finance",
                        "housing", "pets", "politics", "religion", "employee salary")
        );
        this.chatClient = chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(messageChatMemoryAdvisor, safeGuardAdvisor, new SimpleLoggerAdvisor())
                .build();
    }

    public String chatWithHr(String query) {
        return chatClient.prompt()
                .user(query)
                .system(hrAssistantSystemMessage)
                .call().content();
    }
}
