package com.edu.spring.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * @author Rohit Muneshwar
 * @created on 2/27/2026
 *
 *
 */
@Service
@Slf4j
public class AdvancedRagProcessingService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AdvancedRagProcessingService(ChatClient.Builder chatClientBuilder, ChatOptions chatOptions,
                                        VectorStore vectorStore){
        this.chatClient = chatClientBuilder.defaultOptions(chatOptions)
                .defaultAdvisors(new SimpleLoggerAdvisor()).build();
        this.vectorStore = vectorStore;
    }

    /**
     *  Advanced RAG flow phases:
     *  Pre retrieval phase - happens before fetching document using QueryTransformer  and QueryExpander.
     *  Retrieval phase - we fetch document using DocumentRetriever(use VectorStoreDocumentRetriever).
     *  DocumentJoiner(optional) - If multiple data sources are used, it will merges the data.
     *  Generation phase using QueryAugumenter interface - ContextualQueryAugumenter class.
     *
     *  To translate the query from one lang to another lang we use TranslationQueryTransformer.
     * @param query
     * @return
     */
    public String getAnswer(String query) {
        var advisor = RetrievalAugmentationAdvisor.builder()
                // pre retrieval phase
                .queryTransformers(
                        RewriteQueryTransformer.builder().chatClientBuilder(this.chatClient.mutate().clone()).build(),
                        // translate the query from one lang to another lang
                        TranslationQueryTransformer.builder().chatClientBuilder(this.chatClient.mutate().clone()).targetLanguage("english").build()
                        )
                .queryExpander(MultiQueryExpander.builder().chatClientBuilder(this.chatClient.mutate().clone()).numberOfQueries(3).build())
                // retrieval phase
                .documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).topK(3).similarityThreshold(0.3).build())
                // document joiner - optional
                .documentJoiner(new ConcatenationDocumentJoiner())
                // Generation phase using QueryAugumenter interface - ContextualQueryAugumenter class
                .queryAugmenter(ContextualQueryAugmenter.builder().build())
                .build();
        return chatClient.prompt().user(query)
                .advisors(advisor)
                .call().content();
    }
}
