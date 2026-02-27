# Advisors

### Introduction
- It works like a middleware or interceptor
- whenever user sends a request as a prompt to chatclient. First it will goto Advisor(write some cross cutting concerns). Now advisor will send the query to LLM. Finally LLM will generate the response, after that we again call advisor and then we send final response to user

#### Flow
- user - chatclient - Advisor - LLM - Response - Advisor - user

#### Steps
1. Create springboot project with web, azure ai dependency

2. Configure api key in application.properties

3. Create prompts inside resource folder

system-message.st
````
You are a helpful coding assistant, Explain the concept in detail with example
````
user-message.st
````
Explain the coding concepts: {concept}
````

4. Create controller
````
@RestController
@RequestMapping
public class ChatController {

              @Value("classpath:/prompts/system-message.st")
              private Resource systemMessage;
             
              @Value("classpath:/prompts/user-message.st")
              private Resource userMessage;
             
              private ChatClient chatClient;
 
              public ChatController(ChatClient.Builder builder) {
                             this.chatClient = builder.build();
              }
 
              @GetMapping("/chat")
              public ResponseEntity<String> getMessage(@RequestParam(value="msg",required=true)String query) {
                             String str=chatClient.prompt()
                                                                       .system(system->system.text(this.systemMessage))
                                                                       .user(user->user.text(this.userMessage).param("concept", query))
                                                                       .call()
                                                                       .content();
                             return ResponseEntity.ok(str);
              }
}
````
5. Start the appl, run
````
http://localhost:2000/chat?msg=json
````

### Spring AI builtin Advisor

1. SimpleLoggerAdvisor 
- to log our request prompt before sending into LLM abd response

2. ChatMemoryAdvisor 
- manage conversation history in a chat memory store
  - MessageChatMemoryAdvisor
    - enables conversation memory by retrieving previous chat message from ChatMemory and adding them to the prompt as(user/assistant role) before LLM is called

  - PromptChatMemoryAdvisor
    - enables conversation memory by retrieving previous chat message from ChatMemory and injecting them into prompt system text before the LLM is called

  - VectorStoreChatMemoryAdvisor
    - Retrieves memory from VectorStore and adds into the prompts system text

  - QuestionAnswerAdvisor
    - uses a vector store to provide question answer capability implementing RAG

  - RetrievalAugumentationAdvisor
    - Advisor that implements common RAG  flows

3. Reasoning Advisor
   - ReReading Advisor
     - improves LLM's reasoning by forcing the model to read the user question again before answering. It is based on ReReading(Re2) technique

#### why?
1. Answer too quickly after a single pass
2. Miss constraint, dates or wording


4. Content Safety Advisor
    - SafeGuardAdvisor
      - prevent the model from generating harmful or inappropriate content


#### Some Points
1. We can multiple advisor, the execution order of the advisor in the chain is determined by getOrder()
2. Advisor with lower order values are executed first
3. The advisor chain operates as a stack, first advisor in the chain is the first to process the request and last to process the response
4. To control execution order using
   @Order(Ordered.HIGHEST_PRECEDENCE), @Order(Ordered.LOWEST_PRECEDENCE)
5. If multiple advisors are have same order value thrn execution order is not guaranteed
6. Now we use SimpleLoggerAdvisor to log all the request and response on console

````
public ChatController(ChatClient.Builder builder) {
//this.chatClient = builder.build();
this.chatClient=builder.defaultAdvisors(new SimpleLoggerAdvisor()).build();
}
````
7. In application.properties define the logging level
````
logging.level.org.springframework.ai.chat.client.advisor=DEBUG
````
8. Start the appl, run
````
http://localhost:2000/chat?msg=json
````
9. We implement SafeGuardAdvisor which block the call to the model provider if the user input contains any of the sensitive words, in that case it will print default_failure_response message
````
public ChatController(ChatClient.Builder builder) {
//this.chatClient = builder.build();
this.chatClient=builder
.defaultAdvisors(new SimpleLoggerAdvisor(),new SafeGuardAdvisor(List.of("games","movies","songs")))
.build();
}
````
10. Start the appl, run  
````
http://localhost:2000/chat?msg=how to play card games

http://localhost:2000/chat?msg=explain about avatar movies
````
It will display "I'm unable to respond to that due to sensitive content. Could we rephrase or discuss something else?"


## Memory in AI
- LLM are stateless (ie) LLM doesnt remember anything about the previous interaction
- LLM have no memory of previous conversation

#### Why does copilot or chatgpt seems to remember?
- copilot and chatgpt are application build on top of the LLM, and it wraps around the wmodel
- When you interact with copilot or chatgpt we are not directly interacting with LLM. So chatgpt app manages the memory, stores your conversation history and sends the relavant context back to the LLM with each request
- So the memory feature is implemented by the application level(chatgpt.copilot), not by the LLM itself


Spring AI provides API's to manage memory and pass conversation context to the model - 2 key interfaces

1. ChatMemory interface - represent what to store and how to manage memory, it maintains
    - keep the last n messages
    - keep messages within the time period
    - keep messages within token limit
      It does not store data itself

- MessageWindowChatMemory class - default implementation class of ChatMemory, it keeps last 20 messages

2. ChatMemoryRepository interface - for storing and retrieving actual data
- findConversationIds()
- findByConversationId()
- saveAll()
- deleteByConversationId()

- InMemoryChatMemoryRepository class - default implementation class of ChatMemoryRepository, stores the message in memory using ConcurrentHashMap, key is conversationId and value is list of messages
    - Messages are lost when the appl restarts

- JdbcChatMemoryRepository - stores messages in db using JDBC


#### Advisors for Memory
a. MessageChatMemoryAdvisor
b. PromptChatMemoryAdvisor


- Implement chat memory so that it will maintain the conversation history using MessageChatMemoryAdvisor
````
public ChatController(ChatClient.Builder builder,ChatMemory chatMemory) {
MessageChatMemoryAdvisor chatMemoryAdvisor=MessageChatMemoryAdvisor.builder(chatMemory).build();
this.chatClient=builder
.defaultAdvisors(chatMemoryAdvisor,new SimpleLoggerAdvisor(),new SafeGuardAdvisor(List.of("games","movies","songs")))
.defaultSystem("You are helpful coding assistant. You are good in coding")
.defaultOptions(AzureOpenAiChatOptions.builder().temperature(0.3).maxTokens(200).build())
.build();
}
````
- Since we use MessageChatMemoryAdvisor which uses ChatMemory 
- and it uses implementation class MessageWindowChatMemory to decide how many msg to store. 
- For storing the msg it uses ChatMemoryRepository 
- and it uses implementation class InMemoryChatMemoryRepository 
- which creates ConcurrentHashMap and store 20 msgs


12. Start the appl run  
````
http://localhost:2000/chat?msg=what is prime number? explain its logic

http://localhost:2000/chat?msg=write the program in Java
````

#### Implement PromptChatMemoryAdvisor - send previous conversation history in the prompt
````
@RestController
@RequestMapping("/chat")
public class PromptController {

              private ChatClient chatClient;
 
              public PromptController(ChatClient.Builder builder) {
                             ChatMemory chatMemory=MessageWindowChatMemory.builder()
                                                                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                                                                        .maxMessages(30)
                                                                        .build();
              this.chatClient=builder.defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build()).build();
              }
 
              @PostMapping("/{conversationId}")
              public String home(@PathVariable String conversationId, @RequestBody String message) {
                             return chatClient.prompt()
                                                                   .advisors(advisor -> advisor.param("conversationId", conversationId))
                                                                   .user(message)
                                                                   .call().content();
              }
}
````
14. Start the appl run  
````
http://localhost:2000/chat/conv1 and in body "My name is Tim"

http://localhost:2000/chat/conv1 and in body "what is my name?"
````

### Custom Advisor
- Whenever user send a request through our appl to LLM, we know that LLM will charge based on the token for input and output
- We create custom advisor that calculates and displays the total tokens consumed

- Create separate class which implements CallAdvisor and StreamAdvisor interface and override
    - adviceCall() - logic of advisor
    - adviceStream() - used for streaming programing
    - getName()- return name of advisor
    - getOrder() - return execution order

````
@Slf4j
public class TokenPrintAdvisor implements CallAdvisor,StreamAdvisor {

              @Override
              public String getName() {
                             // TODO Auto-generated method stub
                             return this.getClass().getName();
              }
 
              @Override
              public int getOrder() {
                             // TODO Auto-generated method stub
                             return 0;
              }
 
              @Override
              public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                           StreamAdvisorChain streamAdvisorChain) {
                             Flux<ChatClientResponse> chatClientResponse=streamAdvisorChain.nextStream(chatClientRequest);
                             return chatClientResponse;
              }
 
              @Override
              public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
                             log.info("My Token Print advisor is called:");
                             log.info("Request: "+chatClientRequest.prompt().getContents());
                            
                             ChatClientResponse chatClientResponse=callAdvisorChain.nextCall(chatClientRequest);
                            
                             log.info("Response received from the Token advisor");
                             log.info("Response: "+chatClientResponse.chatResponse().getResult().getOutput().getText());
                             log.info("Prompt Token: "+chatClientResponse.chatResponse().getMetadata().getUsage().getPromptTokens()); //input token
                             log.info("Completion Token: "+chatClientResponse.chatResponse().getMetadata().getUsage().getCompletionTokens()); //output token
                             log.info("Total Token: "+chatClientResponse.chatResponse().getMetadata().getUsage().getTotalTokens()); //totaltoken=prompt token+completion token
                            
                             return chatClientResponse;
              }

}
````
- Call custom  advisor in ChatClient
````
public ChatController(ChatClient.Builder builder) {
//this.chatClient = builder.build();
this.chatClient=builder
.defaultAdvisors(new TokenPrintAdvisor(),new SafeGuardAdvisor(List.of("games","movies","songs")))
.defaultSystem("You are helpful coding assistant. You are good in coding")
.defaultOptions(AzureOpenAiChatOptions.builder().temperature(0.3).maxTokens(200).build())
.build();
}
````

- Start the appl, run
````
http://localhost:2000/chat?msg=java new features
````


---

## 🌟 Developer/Contributor
Name: Rohit Shamrao Muneshwar  
Email: rohit.muneshwar1406@gmail.com  
LinkedIn Profile: [Click Here](https://www.linkedin.com/in/rohit-muneshwar-a9079258/)  
Other Github repositories: [Click Here](https://github.com/rohit1406?tab=repositories)

---