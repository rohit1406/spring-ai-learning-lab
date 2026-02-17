package me.ai.training.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author Rohit Muneshwar
 * @created on 2/16/2026
 *
 *
 */
@Service
public class TravelItineraryService {
    @Value("classpath:/prompts/travel-itinerary-user-message.st")
    private Resource travelUserMessage;
    @Value("classpath:/prompts/travel-itinerary-system-message.st")
    private Resource travelSystemMessage;
    private ChatClient chatClient;

    public TravelItineraryService(ChatClient.Builder chatClientBuilder, ChatOptions chatOptions){
        this.chatClient = chatClientBuilder.defaultOptions(chatOptions).build();
    }

    public String createTravelItinerary(String source, String destination) {
        return chatClient.prompt()
                .system(travelSystemMessage)
                .user(msg -> msg.text(travelUserMessage)
                        .param("source", source)
                        .param("destination", destination))
                .call().content();
    }
}
