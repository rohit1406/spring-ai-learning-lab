package com.edu.spring.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rohit Muneshwar
 * @created on 3/8/2026
 *
 *
 */
@Configuration
@ConfigurationProperties(prefix = "spring.ai.custom.vectorstore.opensearch")
@Data
public class VectorStoreProperties {
    String uris;
    String username;
    String password;
    String indexName;
    boolean initializationSchema;
    String similarityFunction;
}
