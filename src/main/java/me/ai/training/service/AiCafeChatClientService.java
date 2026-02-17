package me.ai.training.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * @author Rohit Muneshwar
 * AiCafeChatClientService: uses Spring AI's ChatClient to interact with OpenAI
 */
@Service
public class AiCafeChatClientService {
    // communicates with LLM model: using ChatClient or ChatModel
    private final ChatClient chatClient;
    public AiCafeChatClientService(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Asks AiCafe to create a joke based on information provided in message using ChatClient.
     * @param message
     * @return
     */
    public String createJoke(String message){
        IO.println("Using ChatClient for getting response from AiCafe");
        return chatClient.prompt(message)
                        .call()
                        .content();
    }
}
