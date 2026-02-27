package me.ai.training.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Rohit Muneshwar
 * @created on 2/25/2026
 *
 *
 */
@Service
@Slf4j
public class FileProcessingRagService {
    private final ChatClient chatClient;
    private final MultiHopSearchRagService multiHopSearchRagService;

    public FileProcessingRagService(ChatClient.Builder chatClientBuilder, ChatOptions chatOptions,
                                    QuestionAnswerAdvisor questionAnswerAdvisor, MultiHopSearchRagService multiHopSearchRagService){
        this.chatClient = chatClientBuilder.defaultOptions(chatOptions)
                .defaultAdvisors(questionAnswerAdvisor).build();
        this.multiHopSearchRagService = multiHopSearchRagService;
    }

    /**
     * This endpoint uses FILTER EXPRESSION to get the answer from the documents using document id and tenant id.
     * @param query
     * @param documentId
     * @param tenantId
     * @return
     */
    public String getAnswerFromDocument(String query, String documentId, String tenantId) {
        String queryFilter = "document_id=='" + documentId + "' AND tenant_id=='" + tenantId + "' AND topic!='RESTRICTED'";
        String systemPrompt = """
                Return only the exact matching policy statements from the retrieved documents.
                Do not add new information. If nothing matches, respond exactly: No matching policy found.
                """;
        return this.chatClient.prompt()
                .user(query)
                .system(systemPrompt)
                .advisors(c -> c.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, queryFilter))
                .call().content();
    }

    /**
     * It performs chained search through chain of documents.
     * @param query
     * @param documentId
     * @param tenantId
     * @return
     */
    public Map<String, Object> getAnswerWithMultihopSearch(String query, String documentId, String tenantId, int maxHops, int topK, double threshold) {
        return multiHopSearchRagService.multiHopSearch(query, documentId, tenantId, maxHops, topK, threshold);
    }
}
