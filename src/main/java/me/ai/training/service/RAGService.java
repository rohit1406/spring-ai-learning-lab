package me.ai.training.service;

import lombok.extern.slf4j.Slf4j;
import me.ai.training.util.DataUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Vector;


/**
 * @author Rohit Muneshwar
 * @created on 2/19/2026
 *
 *
 */
@Service
@Slf4j
public class RAGService {
    private final DataFeedService dataFeedService;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    @Value("classpath:/prompts/coding-assistant-via-rag-sm.st")
    private Resource codingAssistantSystemMessage;
    @Value("classpath:/prompts/coding-assistant-via-rag-um.st")
    private Resource codingAssistantUserMessage;
    public RAGService(ChatClient.Builder chatClientBuilder, ChatOptions chatOptions, ChatMemory chatMemory,
                      VectorStore vectorStore, DataFeedService dataFeedService){
        this.dataFeedService = dataFeedService;
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                //.defaultSystem("You are helpful coding assistant. You are good in coding.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()
                        //new SimpleLoggerAdvisor()
                        //new SafeGuardAdvisor(List.of("C++"))
                )
                .defaultOptions(chatOptions).build();
    }

    public void saveTestData(){
        dataFeedService.saveData(DataUtil.getData());
    }

    /**
     *  all similarity search and prompt building are done manually in this function.
     * - Build search request, writing system prompt
     * @param query
     * @return
     */
    public String getAnswer(String query) {
        // load data from vector db
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(3) // return me the top 3 results based on the similarity search
                .similarityThreshold(0.6) //filter the response. A value between 0.0 to 1.0, 1.0-exact match, 0.0-loose match, good range 0.5 to 0.7 to get balanced response
                .query(query) // user query
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        // extract text from list of documents
        List<String> documentStrings = documents.stream().map(Document::getText).toList();
        // context data will contain all relevant sentences separated by comma
        String contextData = String.join(",", documentStrings);
        log.info("Context data: {}", contextData);
        return chatClient.prompt()
                .system(sysSpec -> sysSpec.text(codingAssistantSystemMessage).param("documents", contextData))
                .user(usrSpec -> usrSpec.text(codingAssistantUserMessage).param("query", query))
                .call().content();
    }

    /**
     * QuestionAnswerAdvisor
     *    - It automatically runs similarity search on the vector db - retrieves relavant document - build the prompts - inject context - add instructions- send final prompt to LLM
     * @param query
     * @return
     */
    public String getAnswerQAAdvisor(String query) {
        return chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.5).build()).build())
                .user(usrSpec -> usrSpec.text(codingAssistantUserMessage).param("query", query))
                .call().content();
    }

    public String getAnswerRAAdvisor(String query) {
        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .topK(3).similarityThreshold(0.5)
                                .build()
                )
                .build();
        return chatClient.prompt()
                .advisors(ragAdvisor)
                .user(usrSpec -> usrSpec.text(codingAssistantUserMessage).param("query", query))
                .call().content();
    }
}
