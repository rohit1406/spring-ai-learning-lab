package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.service.AiCafeChatModelService;
import me.ai.training.service.ChatService;
import me.ai.training.service.PromptService;
import me.ai.training.service.WeatherAIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Rohit Muneshwar
 *
 * Controller class to consumer AI services provided by OpenAI platform.
 *
 * Advisor
 *     - It works like a middleware or interceptor
 *     - whenever user sends a request as a prompt to chatclient. First it will goto Advisor(write some cross cutting concerns). Now advisor will send the query to LLM. Finally LLM will generate the response, after that we again call advisor and then we send final response to user
 *
 * Spring AI builtin advisors:
 * 1. SimpleLoggerAdvisor: to log our prompt before sending request to LLM and after getting response
 * 2. ChatMemoryAdvisor: manage conversation history in a chat memory store
 *  a. MessageChatMemoryAdvisor: enables conversation memory by retrieving previous chat message
 *      from ChatMemory and adding them to the prompt as(user/assistant role) before LLM is called.
 *  b. PromptChatMemoryAdvisor: enables conversation memory by retrieving previous chat message
 *      from ChatMemory and injecting them into prompt system text before the LLM is called.
 *  c. VectorStoreChatMemoryAdvisor: Retrieves memory from VectorStore and adds into the prompts system text
 *  d. QuestionAnswerAdvisor: uses a vector store to provide question answer capability implementing RAG
 *  e. RetrievalAugmentationAdvisor: Advisor that implements common RAG flows
 * 3. ReasoningAdvisor:
 *  a. RereadingAdvisor: improves LLM's reasoning by forcing the model to read the user question
 *      again before answering. It is based on ReReading(Re2) technique.
 *      why?
 *      1. Answer too quickly after a single pass
 *      2. Miss constraint, dates or wording
 * 4. ContentSafetyAdvisor
 *  a. SafeGuardAdvisor: prevent the model from generating harmful or inappropriate content
 *
 * Order Of Execution:
 *  - We can register multiple Advisors, in that case the execution order of the Advisor in the chain
 *  is determined by the getOrder() method.
 *  - Advisors with lower order values are executed first
 *  - The Advisor chain operates as a stack, first Advisor in the chain is the first to get process the request
 *  and last to process the response.
 *  - To control the execution order: use @Order(Ordered.HIGHEST_PRECEDENCE), @Order(Ordered.LOWEST_PRECEDENCE)
 *  - If multiple advisors are have same order value then execution order is not guaranteed
 *
 *  Memory in AI
 *     - LLM are stateless (ie) LLM doesnt remember anything about the previous interaction
 *     - LLM have no memory of previous conversation
 *     - the memory feature is implemented by the application level(chatgpt.copilot), not by the LLM itself
 *
 *  Spring AI provides API's to manage memory and pass conversation context to the model - 2 key interfaces.
 *  1. ChatMemory interface - represent what to store and how to manage memory. It does not store data itself.
 *          - keep the last n messages
 *          - keep messages within the time period
 *          - keep messages within token limit
 *      - MessageWindowChatMemory class - default implementation class of ChatMemory, it keeps last 20 messages
 *  2. ChatMemoryRepository interface - for storing and retrieving actual data
 *      - InMemoryChatMemoryRepository class - default implementation class of ChatMemoryRepository, stores the message in memory using ConcurrentHashMap, key is conversationId and value is list of messages
 *          - Messages are lost when the appl restarts
 *      - JdbcChatMemoryRepository - stores messages in db using JDBC
 *
 *  Advisors for Memory
 *      a. MessageChatMemoryAdvisor
 *      b. PromptChatMemoryAdvisor
 */
@Tag(name = "Lab5: Advisor and Chat Memory", description = """
        This Lab demonstrates the usage of advisors while retrieving data from LLM.
        It works like a middleware or interceptor.
         - Whenever user sends a request as a prompt to ChatClient.
         - First it will goto Advisor(write some cross cutting concerns).
         - Now advisor will send the query to LLM.
         - Finally LLM will generate the response, after that we again call advisor
         - and then we send final response to user.
         
         user -> ChatClient -> Advisor -> LLM -> Response -> Advisor -> user
         
         Memory in AI
               - LLM are stateless (ie) LLM doesn't remember anything about the previous interaction
               - LLM have no memory of previous conversation
               - the memory feature is implemented by the application level(chatgpt.copilot), not by the LLM itself
        """)
@RestController
@RequestMapping(value = "/chat")
public class ChatController {
    private final ChatService chatService;
    private final PromptService promptService;

    public ChatController(ChatService chatService,PromptService promptService){
        this.chatService = chatService;
        this.promptService = promptService;
    }

    @Operation(summary = "Get answer to the users coding query", description = """
            This implementation demonstrates the usage of UserMessage and SystemMessage from String Template(.st) for
            providing the system message, user context. It also remembers (in memory) history of chats. It shows how SimpleLoggerAdvisor, SafeGuardAdvisor and MessageChatMemoryAdvisor can be utilized while sending request to LLM.
            SimpleLoggerAdvisor: to log our prompt before sending request to LLM and after getting response
            SafeGuardAdvisor: block the call to the model provider if the user input contains any of the sensitive words, in that case it will print default_failure_response message
            default_failure_response is "I'm unable to respond to that due to sensitive content. Could we rephrase or discuss something else?".
            
            Since we use MessageChatMemoryAdvisor which uses ChatMemory
            and it uses implementation class MessageWindowChatMemory to decide how many msg to store.
            For storing the msg it uses ChatMemoryRepository
            and it uses implementation class InMemoryChatMemoryRepository which creates
            ConcurrentHashMap and store 20 messages.
            
            e.g. Prompt1: What is prime number? Explain it's logic.
                 Prompt2: Write a Java Program for it.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/")
    public ResponseEntity<String> getAssistance(@RequestParam(value = "msg", required = true) String msg){
        return ResponseEntity.ok(chatService.getAssitance(msg));
    }

    @Operation(summary = "Get answer to the users coding query", description = """
            This implementation demonstrates the usage of Memory Advisor.
            It remembers (in memory) history of chats. It shows how TokenCalculatorAdvisor (custom advisor), SafeGuardAdvisor and PromptChatMemoryAdvisor can be utilized while sending request to LLM.
            TokenCalculatorAdvisor: This custom advisor is to log our prompt before sending request to LLM and after getting response
            SafeGuardAdvisor: block the call to the model provider if the user input contains any of the sensitive words, in that case it will print default_failure_response message
            default_failure_response is "I'm unable to respond to that due to sensitive content. Could we rephrase or discuss something else?".
            
            Since we use PromptChatMemoryAdvisor which uses ChatMemory
            and it uses implementation class MessageWindowChatMemory to decide how many msg to store.
            For storing the msg it uses ChatMemoryRepository
            and it uses implementation class InMemoryChatMemoryRepository which creates
            ConcurrentHashMap and store 20 msgs.
            
            e.g. Prompt1: My name is Rohit Muneshwar.
                 Prompt2: What is your name?
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping(value = "/{conversationId}")
    public String processRequest(@PathVariable String conversationId,
                                                 @RequestBody String message){
        return promptService.processRequest(conversationId, message);
    }
}
