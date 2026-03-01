package com.edu.spring.ai.service;

import lombok.extern.slf4j.Slf4j;
import com.edu.spring.ai.config.AiCafeConfig;
import com.edu.spring.ai.dto.Message;
import com.edu.spring.ai.dto.Request;
import com.edu.spring.ai.dto.Response;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Rohit Muneshwar
 * AiCafeUsingRestTemplateService: uses RestTemplate to interact with Aicafe
 */
@Service
@Slf4j
public class AiCafeUsingRestTemplateService {
    private final AiCafeConfig aiCafeConfig;
    private final RestTemplate restTemplate;
    public AiCafeUsingRestTemplateService(AiCafeConfig aiCafeConfig){
        this.aiCafeConfig = aiCafeConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Asks AiCafe to create a joke based on information provided in message using RestTemplate
     * @param message
     * @return
     */
    public Response createJoke(String message){
        IO.println("Using RestTemplate for getting response from AiCafe");
        Request request = createRequest(message);
        HttpHeaders headers = new HttpHeaders();
        headers.add("api-key", aiCafeConfig.getApiKey());
        headers.add("Content-Type", "application/json");
        ResponseEntity<Response> response = restTemplate.exchange(aiCafeConfig.getChatUrl(), HttpMethod.POST, new HttpEntity<>(request, headers), Response.class);
        return response.getBody();
    }

    /**
     * Asks AiCafe to create a joke based on information provided in message using RestTemplate
     * @param message
     * @return
     */
    public String getResponseUsingVectorDb(String message){
        IO.println("Using RestTemplate for getting response from AiCafe");
        String request = """
                {
                  "input": [
                    "What is React?"
                  ]
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.add("api-key", aiCafeConfig.getApiKey());
        headers.add("Content-Type", "application/json");
        ResponseEntity<String> response = restTemplate.exchange(aiCafeConfig.getEmbeddingUri(), HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
        log.info("Embedding response from custom rest calling: {}", response.getBody());
        return response.getBody();
    }

    private Request createRequest(String jokeAbout) {
        Request request = new Request();
        Message message = new Message();
        message.setContent(jokeAbout);
        request.setMessages(List.of(message));
        return request;
    }
}
