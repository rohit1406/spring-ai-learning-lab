package me.ai.training.config;

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
import org.springframework.web.util.UriComponentsBuilder;
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
public class AiCafeConfig {
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


    @Bean
    @ConditionalOnBooleanProperty(prefix = "spring.ai.cafe", name = "enabled")
    OpenAiApi openAiApi(){
        log.info("Creating custom OpenAiApi bean: {}",this.getCompletionPath());
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", this.getApiKey());
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
                .apiKey(this.getApiKey())
                .baseUrl(this.getBaseUrl())
                .completionsPath(this.getCompletionPath())
                .embeddingsPath(this.getEmbeddingPath())
                .headers(headers)
                .restClientBuilder(builder)
                .build();
    }

    @Bean
    OpenAiEmbeddingModel openAiEmbeddingModel(OpenAiApi openAiApi){
        return new OpenAiEmbeddingModel(openAiApi);
    }
}
