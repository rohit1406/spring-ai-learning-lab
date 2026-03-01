package com.edu.spring.ai.service;

import com.edu.spring.ai.advisor.TokenCalculatorAdvisor;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author Rohit Muneshwar
 * @created on 2/18/2026
 *
 * ChatMemory uses MessageWindowChatMemory which stores default 20msgs,
 * it contains reference of ChatMemoryRepository which default implementation is
 * InMemoryChatMemoryRepository which uses ConcurrentHashmap to stores msg in memory where
 * key=conversationid, value=List of messages.
 * Drawback
 *   - Messages are stored in memory, so once appl restarted all msg will be lost
 *
 * Chat Conversation using JdbcChatMemoryRepository
 *       - Previously we use InMemoryChatMemoryRepository which stores message in memory and once appl restarted all msg will be lost
 *       - If we want to store converation history permanently in db using JdbcChatMemoryRepository
 */
@Service
public class StreamChatService {
    @Value("classpath:/prompts/coding-assistant-system-message.st")
    private Resource systemMessage;
    @Value("classpath:/prompts/coding-assistant-user-message.st")
    private Resource userMessage;
    @Autowired
    private InMemoryChatMessageService inMemoryChatMessageService;
    @Autowired
    private SQLiteChatMessageService sqLiteChatMessageService;
    private final ChatClient chatClient;
    public StreamChatService(ChatClient.Builder chatClientBuilder,
                             ChatOptions chatOptions, ChatMemory chatMemory,
                             JdbcChatMemoryRepository jdbcChatMemoryRepository){
        this.chatClient = chatClientBuilder.defaultOptions(chatOptions)
                .defaultAdvisors(new TokenCalculatorAdvisor())
                .build();
    }

    /**
     * Streaming response in Spring AI - asynchronous and non blocking
     * - User sends the request - we build the prompt and send it to the model - Instead of waiting for the full response, the model starts sending small chunk of data as they are generated.
     *
     * @param query
     * @return
     */
    public @Nullable Flux<String> getResponse(String query) {
        return chatClient.prompt()
                .system(this.systemMessage)
                .user(msg -> msg.text(this.userMessage).param("concept", query))
                .stream().content();
    }

    /**
     * How multiple users can interact with LLM ?
     * Problems
     * 1. Our appl interacts with LLM to get response
     * 2. Users of our appl is not limited to one person
     *
     * For example, if user1 and user2 are using appl, each should have their own messages history maintained, but now we can only maintain one message history.
     *
     * ChatMemory uses MessageWindowChatMemory which stores default 20msgs, it contains reference of ChatMemoryRepository which default implementation is InMemoryChatMemoryRepository which uses ConcurrentHashmap to stores msg in memory where
     *   key=conversationid, value=List of messages
     *
     * By default conversationId is "default", so all messages goes under same key. To maintain session for multiple user we have to use separate conversationid for each user and pass it as a header so each session created for each user.
     *
     * Drawback
     *   - Messages are stored in memory, so once appl restarted all msg will be lost
     * @param query
     * @return
     */
    public @Nullable Flux<String> getMessage(String userId, String query) {
        return inMemoryChatMessageService.getMessage(userId, query);
    }

    /**
     * Chat Conversation using JdbcChatMemoryRepository
     *       - Previously we use InMemoryChatMemoryRepository which stores message in memory and once appl restarted all msg will be lost
     *       - If we want to store converation history permanently in db using JdbcChatMemoryRepository
     * @param query
     * @return
     */
    public @Nullable Flux<String> getPersistenceAwareMessage(String userId, String query) {
        return sqLiteChatMessageService.getMessage(userId, query);
    }
}
