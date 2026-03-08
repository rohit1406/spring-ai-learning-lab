package com.edu.spring.ai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

/**
 * @author Rohit Muneshwar
 */
@Configuration
@ConfigurationProperties(prefix = "spring.ai.cafe")
@Data
@Slf4j
public class AiCafeProperties {
    private String baseUrl;
    private String apiKey;
    private String completionPath;
    private String embeddingPath;
    private String embeddingDeploymentName;
    private String chatDeploymentName;
    private String apiVersion;
    private String model;
    public URI getChatUrl(){
        try {
            return new URI(this.getBaseUrl()+this.getChatDeploymentName());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public URI getEmbeddingUri(){
        try{
            return new URI(this.getBaseUrl()+this.getEmbeddingPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
