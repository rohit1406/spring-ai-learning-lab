package me.ai.training.service;

import me.ai.training.tools.BankingTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Rohit Muneshwar
 * @created on 2/16/2026
 *
 *
 */
@Service
public class BankingAdvisorService {
    @Value("classpath:/prompts/bank-account-details.st")
    private Resource bankAccountDetailsPrompt;
    @Autowired
    private BankingTool bankingTool;
    private ChatClient chatClient;

    public BankingAdvisorService(ChatClient.Builder chatClientBuilder, ChatOptions chatOptions){
        this.chatClient = chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    public String getBankAccountDetails(String userId) {
        PromptTemplate promptTemplate = new PromptTemplate(bankAccountDetailsPrompt);
        Prompt prompt = promptTemplate.create(Map.of("customer_id", userId));
        return chatClient.prompt(prompt)
                .tools(bankingTool)
                .call().content();
    }
}
