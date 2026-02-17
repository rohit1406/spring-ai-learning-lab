package me.ai.training.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Rohit Muneshwar
 */
@Configuration
@ConfigurationProperties(prefix = "spring.ai.cafe")
public class AiCafeConfig {
    private String apiKey;
    private String endpoint;
    private String apiVersion;
    private String model;
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
    public URI getUri(){
        try {
            return new URI(this.getEndpoint()+
                    this.getModel()
                    +"/chat/completions" +
                    "?api-version="+this.getApiVersion());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
