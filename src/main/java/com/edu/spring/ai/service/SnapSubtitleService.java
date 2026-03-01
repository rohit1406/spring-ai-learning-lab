package com.edu.spring.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Rohit Muneshwar
 * @created on 3/1/2026
 *
 *
 */
@Service
@Slf4j
public class SnapSubtitleService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    @Value("classpath:/prompts/snap-subtitle-sm.st")
    Resource systemMessage;
    @Value("classpath:/prompts/snap-subtitle-um.st")
    Resource userMessage;

    public SnapSubtitleService(ChatClient.Builder chatClientBuilder, ChatOptions chatOptions,
                               ChatMemory chatMemory, VectorStore vectorStore,
                               QuestionAnswerAdvisor questionAnswerAdvisor){
        chatClient = chatClientBuilder.defaultOptions(chatOptions)
                .defaultAdvisors(new SimpleLoggerAdvisor(), // log request and response from LLM
                        MessageChatMemoryAdvisor.builder(chatMemory).build() // remembers chat history up to 20 messages
                )
                .build();
        this.vectorStore = vectorStore;
        this.questionAnswerAdvisor = questionAnswerAdvisor;
    }

    public String snapToPosition(String userContext) {
        List<Document> searchResult = vectorStore.similaritySearch(SearchRequest.builder().query(userContext).topK(2).build());
        List<String> searchedText = searchResult.stream().map(Document::getText).toList();
        log.info("Search Result from vector db: {}", searchResult.stream().map(Document::getText).toList());
        ChatClientResponse response = chatClient.prompt()
                .user(um -> um.text(userMessage).param("context", userContext))
                .system(sm -> sm.text(systemMessage).param("documents", searchedText))
                .advisors(questionAnswerAdvisor)
                .call().chatClientResponse();
        String result = response.chatResponse().getResult().getOutput().getText();
        log.info("Answer from LLM: {}", result);
        return result;
    }
}
