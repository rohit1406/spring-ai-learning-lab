package me.ai.training.config;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.common.OpenAiApiClientErrorException;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rohit Muneshwar
 * @created on 2/24/2026
 *
 *
 */
@Configuration
public class AiConfig {
    @Bean
    QuestionAnswerAdvisor qaAdvisor(VectorStore vectorStore){
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(5).similarityThreshold(0.3).build())
                .build();
    }

    @Bean
    OpenAiAudioApi openAiAudioApi(@Value("${spring.ai.openai.audio.transcription.base-url}")
                                  String endpoint,
                                  @Value("${spring.ai.openai.api-key}")
                                  String apiKey){
        return new OpenAiAudioApi.Builder()
                .baseUrl(endpoint)
                .apiKey(apiKey)
                .build();
    }
}
