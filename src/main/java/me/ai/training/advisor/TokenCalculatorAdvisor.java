package me.ai.training.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * @author Rohit Muneshwar
 * @created on 2/13/2026
 *
 * Custom advisor to log tokens.
 *      - Whenever user send a request through our application to LLM, we know that LLM will charge based on the token for input and output
 *      - We create custom advisor that calculates and displays the total tokens consumed
 */
@Slf4j
public class TokenCalculatorAdvisor implements CallAdvisor, StreamAdvisor {
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        log.info("my TokenCalculatorAdvisor is called");
        log.info("Request: {}", chatClientRequest.prompt().getContents());
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        log.info("Response received from my TokenCalculatorAdvisor");
        log.info("Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
        log.info("Prompt Token: {}", chatClientResponse.chatResponse().getMetadata().getUsage().getPromptTokens());
        log.info("Completion Token: {}", chatClientResponse.chatResponse().getMetadata().getUsage().getCompletionTokens());
        log.info("Token Token: {}", chatClientResponse.chatResponse().getMetadata().getUsage().getTotalTokens());
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        log.info("inside TokenCalculatorAdvisor::adviseStream with request: {}", chatClientRequest);
        return streamAdvisorChain.nextStream(chatClientRequest);
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
