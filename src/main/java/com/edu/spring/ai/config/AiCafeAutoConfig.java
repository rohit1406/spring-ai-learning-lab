package com.edu.spring.ai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
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
@ConditionalOnBooleanProperty(prefix = "spring.ai.cafe", name = "enabled")
@Data
@Slf4j
public class AiCafeAutoConfig {
    @Autowired
    AiCafeProperties aiCafeProperties;

    @Bean
    OpenAiApi openAiApi(){
        log.info("Creating custom OpenAiApi bean: {}",aiCafeProperties.getCompletionPath());
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", aiCafeProperties.getApiKey());
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        EmbeddingListHttpMessageConverter embeddingListHttpMessageConverter = new EmbeddingListHttpMessageConverter();
        JsonMapper jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .build();
        RestClient.Builder builder = RestClient.builder()
                .configureMessageConverters(c -> c.addCustomConverter(new JacksonJsonHttpMessageConverter(jsonMapper)).addCustomConverter(embeddingListHttpMessageConverter));
        return OpenAiApi.builder()
                .apiKey(aiCafeProperties.getApiKey())
                .baseUrl(aiCafeProperties.getBaseUrl())
                .completionsPath(aiCafeProperties.getCompletionPath())
                .embeddingsPath(aiCafeProperties.getEmbeddingPath())
                .headers(headers)
                .restClientBuilder(builder)
                .build();
    }

    @Bean
    OpenAiEmbeddingModel openAiEmbeddingModel(OpenAiApi openAiApi){
        return new OpenAiEmbeddingModel(openAiApi);
    }
}
