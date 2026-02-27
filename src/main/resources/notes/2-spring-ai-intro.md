# Spring AI
## Introduction

- It is a project helping Java developers to build intelligent springboot appl with AI

### Features
1. Vendor agnostic
   - support all major LLM with the same code
   - supports different models of vendors with same code
   - vendor agnostic vector store support - RAG(Retrival Augumented Generation) - so that we can get latest data

2. Seamless integration to spring ecosystem
   - Easy integration of AI into your existing spring/springboot appl
   - Simplify AI integration with autoconfiguration

3. Multimodality
   - supports different forms of input (ie) it can be text, image, audio, video

1. Create springboot project with web, azure open ai dependency

2. Configure api keys in application.properties
````
spring:
    ai:
       openai:
           api-key: ${API_KEY_OPENAI}
           embedding:
               options:
                   # this is required for RAG
                   deployment-name: text-embedding-3-small
           chat:
               options:
                   model: gpt-4.1
````
Set key in env variable
- Right click project 
- Run as - Run configuration 
- Env tab 
- Add new values

3. Create controller

- ChatClient interface communicate with LLM model
````
@RestController
public class ChatController {

        private ChatClient chatClient;
        
        public ChatController(ChatClient.Builder chatClientBuilder) {
                this.chatClient=chatClientBuilder.build();
        }

        @GetMapping("/jokes")
        public String generate(@RequestParam(value="message",defaultValue="Tell me a dad joke about computers") String message) {
                return chatClient.prompt(message).call().content();
        }
}
````
- Instead of ChatClient we can also use ChatModel
````
@RestController
public class ChatController {

        //private ChatClient chatClient;
        
        /*public ChatController(ChatClient.Builder chatClientBuilder) {
                this.chatClient=chatClientBuilder.build();
        }*/
        
        @Autowired
        private AzureOpenAiChatModel chatClient;

        @GetMapping("/jokes")
        public String generate(@RequestParam(value="message",defaultValue="Tell me a dad joke about computers") String message) {
                return chatClient.call(message);
        }
}
````

### ChatClient
- It is a helper class (technically it is an interface) that makes it super easy to talk with AI models
- It hides all internal complexity like
  1. making raw HTTP calls
  2. handling base URL's
  3. parsing json
  4. token handling
  5. formatting the request
  6. formatting the response
- return response: chatClient.prompt(message).call().content();

#### why?
1. very easy to use
2. support multiple modes - sync or blocking, async or nonblocking, streaming
3. prompts can be build in many styles
4. Support different message types
   - user message
   - system message
   - assistant message
   - tool/function message
5. Dynamic placeholder - you can pass variables to prompt at runtime
6. Prompt options
    - model selection
    - temperature setting

### Temperature setting in AI
- It control how creative or predicatable the model answers should be
1. Low temperature (0.0 to 0.3) 
   - predicatable and accurate
   - correct answer, deterministic output, coding help, calculations, documentation

   - e.g. 
   Prompt: Explain Springboot - temperature(0.1) - gives very professional, structured and accurate explanation

2. Medium temperature(0.4 to 0.6) 
   - Balanced output
   - general chat, balanced creativity, accuracy

3. High temperature(0.7 to 1.5) 
   - Creative and diverse
   - stories, poems, marketing copy, creative rewriting
   - Prompt: write a poem about Springboot - temperature(1.2) - give fun and imaginative poem about springboot

- code generation(0.0 - 0.2)
- Technical explanation(0.1 - 0.3)
- Chat 0.4 - 0.6
- Creative writing 0.8 - 1.2
- brainstorming 1.2 - 1.5


### ChatModel
- Below ChatClient there is another API - ChatModel API
- ChatModel represents a specific chat model provider (eg) Open AI, AzureOpenAI, Huggingface etc
- Each provider has its own implementation like OpenAiChatModel, AzureOpenAiChatModel, OllamaChatModel, MistralChatModel, AnthropicChatModel etc

- We do not directly use ChatModel in our appl
- ChatClient internally uses ChatModel
- ChatClient is the abstraction that gives us convenience to interact with AI
- ChatModel is lower level layer then ChatClient

#### Flow:
- When we create the project, 
- we use a controller where our request comes, 
- inside the controller first it will call ChatClient. 
- So ChatClient internally uses ChatModel API because this  lower level layer then ChatClient. 
- Each ChatModel has its own implementation (ie) AzureOpenAiChatModel which interact with LLM to get the response

  - Controller
  - uses
    ChatClient
  - uses
    ChatModel
  - provides implementation based on vendor
    AzureOpenAiChatModel
  - calls
    LLM (Azure Open AI)
  - returns
    Response - back to ChatClient - Controller - end user


- Based on the dependency we have provided, the ChatModel will be automatically changed and the object will be created.
- Based on the starter project it will load AzureOpenAiAutoConfiguration,
- now it will call AzureOpenAiChatModel 
- bean where it will be containing AzureOpenAiChatProperties class 
- which will contain all the properties specified in application.properties 
- and return ChatModel

### Prompts in AI
- It serve as the foundation for the language based inputs that guide an AI model to produce specific output

- Effective communication 
- Three roles
    - user (actual query)
    - system (defines behaviour)
    - assistant (response from AI)

- Prompt class represents a prompt used in AI model requests. A prompt consists of one or more messages and additional chat options.
  1. UserMessage 
     - represent actual input/question/instruction
     - eg:
        User: Explain DI in Spring
  2. SystemMessage 
     - Defines the purpose, behaviour, tone and role of the AI
     - eg:
        System: You are a senior Java and Springboot developer. Explain everything with clarity
  3. AssiantMessage 
     - represent previous response given by the AI
     - eg: DI allows you to delegate object creation to Spring

````
@RestController
public class PromptController {

        @Autowired
        AzureOpenAiChatModel chatClient;

        /*@GetMapping("/")
        public String message() {
                return chatClient.call(new Prompt("Tell me a joke about Springboot")).getResult().getOutput().getText();
        }*/
        
        //with prompt messages
        /*@GetMapping("/")
        public String message() {
                var system=new SystemMessage("You are a senior Java and Springboot expert. Be precise and pragmatic");
                
                var assistant=new AssistantMessage("Dependency Injection lets Spring manage object wiring via the container");
                
                var user=new UserMessage("Give me a concise, real world example of DI in Springboot");
                
                var prompt=new Prompt(List.of(system,assistant,user));
                
                return chatClient.call(prompt).getResult().getOutput().getText();
        }*/
        
        //with prompt options
        @GetMapping("/")
        public String message() {
                
                AzureOpenAiChatOptions options=AzureOpenAiChatOptions.builder()
                                                .temperature(0.7)
                                                .maxTokens(100)
                                                .topP(0.9)
                                                .build();
                
                var system=new SystemMessage("You are a senior Java and Springboot expert. Be precise and pragmatic");
                
                var assistant=new AssistantMessage("Dependency Injection lets Spring manage object wiring via the container");
                
                var user=new UserMessage("Give me a concise, real world example of DI in Springboot");
                
                var prompt=new Prompt(List.of(system,assistant,user));
                
                return chatClient.call(prompt).getResult().getOutput().getText();
        }
}
````

#### In case if we want to add options to all endpoints

1. Global options via application.yml
````
spring:
ai:
azure:
openai:
chat:
options:
temperature: 0.7
max-tokens: 100
top-p: 0.9
frequency-penalty: 0.5
presence-penalty: 0.3
````
2. Create global chatoptions bean
````
@Configuration
public class AiConfig {

@Bean
public AzureOpenAiChatOptions chatOptions() {
return AzureOpenAiChatOptions.builder()
.temperature(0.7)
.maxTokens(100)
.topP(0.9)
.frequencyPenalty(0.4)
.presencePenalty(0.2)
.build();
}
}
````
````
@Autowired
AzureOpenAiChatOptions chatOptions;

        @GetMapping("/")
        public String message() {
                
                var system=new SystemMessage("You are a senior Java and Springboot expert. Be precise and pragmatic");
                
                var assistant=new AssistantMessage("Dependency Injection lets Spring manage object wiring via the container");
                
                var user=new UserMessage("Give me a concise, real world example of DI in Springboot");
                
                var prompt=new Prompt(List.of(system,assistant,user),chatOptions);
                
                return chatClient.call(prompt).getResult().getOutput().getText();
        }
````

#### Order of execution
System(beharior/roles) -> Assistant(previous context) -> User messages(your query)


---

## 🌟 Developer/Contributor
Name: Rohit Shamrao Muneshwar  
Email: rohit.muneshwar1406@gmail.com  
LinkedIn Profile: [Click Here](https://www.linkedin.com/in/rohit-muneshwar-a9079258/)  
Other Github repositories: [Click Here](https://github.com/rohit1406?tab=repositories)

---