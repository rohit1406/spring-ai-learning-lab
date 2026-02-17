package me.ai.training.service;

import me.ai.training.config.AiCafeConfig;
import me.ai.training.dto.Message;
import me.ai.training.dto.Request;
import me.ai.training.dto.Response;
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
        ResponseEntity<Response> response = restTemplate.exchange(aiCafeConfig.getUri(), HttpMethod.POST, new HttpEntity<>(request, headers), Response.class);
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
