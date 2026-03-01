package com.edu.spring.ai.service;

import com.edu.spring.ai.advisor.TokenCalculatorAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Rohit Muneshwar
 * @created on 2/12/2026
 *
 * The ChatClient remembers the conversation history in this service.
 *  * Since we use PromptMemoryAdvisor which uses ChatMemory
 *  * and it uses implementation class MessageWindowChatMemory to decide how many msg to store.
 *  * For storing the msg it uses ChatMemoryRepository
 *  * and it uses implementation class InMemoryChatMemoryRepository which creates
 *  * ConcurrentHashMap and store 20 msgs.
 *
 *
 */
@Service
public class PromptService {
    private final ChatClient chatClient;

    /**
     *
     * TokenCalculatorAdvisor: This custom advisor is to log our prompt before sending request to LLM and after getting response
     * SafeGuardAdvisor: block the call to the model provider if the user input contains any of the sensitive words, in that case it will print default_failure_response message
     * default_failure_response is "I'm unable to respond to that due to sensitive content. Could we rephrase or discuss something else?"
     * ChatMemory (default impl class is MessageWindowChatMemory) - stores/manages last few chat messages in memory
     */
    public PromptService(ChatClient.Builder chatClientBuilder, ChatOptions chatOptions){
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(30)
                .build();
        PromptChatMemoryAdvisor promptChatMemoryAdvisor = PromptChatMemoryAdvisor.builder(chatMemory).build();
        this.chatClient = chatClientBuilder.defaultOptions(chatOptions)
                .defaultAdvisors(promptChatMemoryAdvisor, new TokenCalculatorAdvisor(),
                        new SafeGuardAdvisor(List.of("games", "songs", "movies")))
                .build();
    }

    /**
     * Answers the users query based on coding concept asked.
     * Notice advisor spec generation here.
     * It remembers the conversation history.
     * @param msg
     * @return
     */
    public String processRequest(String conversationId, String msg) {
        return chatClient.prompt()
                .advisors(advisor -> advisor.param("conversationId", conversationId))
                .user(msg)
                .call().content();
    }
}
