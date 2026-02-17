package me.ai.training.service;

import me.ai.training.tools.SimpleTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Rohit Muneshwar
 * @created on 2/12/2026
 *
 * The ChatClient remembers the conversation history in this service.
 * Since we use MessageChatMemoryAdvisor which uses ChatMemory
 * and it uses implementation class MessageWindowChatMemory to decide how many msg to store.
 * For storing the msg it uses ChatMemoryRepository
 * and it uses implementation class InMemoryChatMemoryRepository which creates
 * ConcurrentHashMap and store 20 msgs.
 *
 */
@Service
public class ChatService {
    private final ChatClient chatClient;

    @Value("classpath:/prompts/coding-assistant-system-message.st")
    private Resource codingAssistantSystemMessage;

    @Value("classpath:/prompts/coding-assistant-user-message.st")
    private Resource codingAssistantUserMessage;

    /**
     * SimpleLoggerAdvisor: to log our prompt before sending request to LLM and after getting response
     * SafeGuardAdvisor: block the call to the model provider if the user input contains any of the sensitive words, in that case it will print default_failure_response message
     * default_failure_response is "I'm unable to respond to that due to sensitive content. Could we rephrase or discuss something else?"
     * ChatMemory (default impl class is MessageWindowChatMemory) - stores/manages last few chat messages in memory
     */
    public ChatService(ChatClient.Builder chatClientBuilder, ChatOptions chatOptions,
                       ChatMemory chatMemory){
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.chatClient = chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(messageChatMemoryAdvisor, new SimpleLoggerAdvisor(),
                        new SafeGuardAdvisor(List.of("games", "songs", "movies")))
                .build();
    }

    /**
     * Integration of SimpleTool in the ChatClient.
     * @param msg
     * @return
     */
    public String getChat(String msg) {
        IO.println("via tool calling");
        return chatClient.prompt(msg)
                .tools(new SimpleTool()) // notice here registration of tool
                .call().content();
    }

    /**
     * Answers the users query based on coding concept asked.
     * It uses UserMessage and SystemMessage in the ChatClient.
     * Notice how those messages are created and submitted along with the prompt.
     * @param msg
     * @return
     */
    public String getAssitance(String msg) {
        return chatClient.prompt()
                .system(sys -> sys.text(this.codingAssistantSystemMessage)) // notice here generation of message
                .user(usr -> usr.text(this.codingAssistantUserMessage).param("concept", msg)) // notice here generation of message
                .call().content();
    }
}
