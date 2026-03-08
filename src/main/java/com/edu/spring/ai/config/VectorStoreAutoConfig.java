package com.edu.spring.ai.config;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.opensearch.OpenSearchVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;

/**
 * @author Rohit Muneshwar
 * @created on 3/8/2026
 *
 *
 */
@Configuration
@ConditionalOnBooleanProperty(prefix = "spring.ai.custom.vectorstore", name = "enabled")
public class VectorStoreAutoConfig {
    @Autowired
    VectorStoreProperties vectorStoreProperties;

    @Bean
    public OpenSearchClient openSearchClient(){
        try {
            final HttpHost host = HttpHost.create("http://localhost:9200");
            final BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
            basicCredentialsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials(vectorStoreProperties.getUsername(), vectorStoreProperties.getPassword().toCharArray()));
            ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(host);
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider));
            return new OpenSearchClient(builder.build());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public VectorStore vectorStore(OpenSearchClient openSearchClient, EmbeddingModel embeddingModel){
        return OpenSearchVectorStore.builder(openSearchClient, embeddingModel)
                .index(vectorStoreProperties.getIndexName())
                .similarityFunction(vectorStoreProperties.getSimilarityFunction())
                .initializeSchema(vectorStoreProperties.isInitializationSchema())
                .build();
    }

    // This can be any EmbeddingModel implementation
    @Bean
    public EmbeddingModel embeddingModel() {
        // TODO: configure embedding model here
        //return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
        return null;
    }
}
