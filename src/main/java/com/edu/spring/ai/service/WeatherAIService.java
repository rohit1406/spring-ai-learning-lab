package com.edu.spring.ai.service;

import com.edu.spring.ai.tools.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Rohit Muneshwar
 * @created on 2/11/2026
 *
 *
 */
@Service
public class WeatherAIService {
    @Autowired
    OpenAiChatModel chatModel;

    @Autowired
    OpenAiChatOptions chatOptions;
    @Autowired
    WeatherTool weatherTool;

    public String getWeatherInformation(String query){
        UserMessage userMessage = new UserMessage(query);
        Prompt prompt = new Prompt(userMessage, chatOptions);
        return ChatClient.create(chatModel).prompt(prompt)
                .tools(weatherTool).call().content();
    }
}
