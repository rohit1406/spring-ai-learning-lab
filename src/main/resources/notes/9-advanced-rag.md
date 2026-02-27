# RAG
## Advanced RAG

#### Simple RAG
- Whenever user sends query to our appl
  1. Feed the details into vector db
  2. we fetch context from our db
  3. We perform similarity search on vector db
  4. return top relavant document
  5. merge this context with user query
  6. then we send enriched prompt to LLM
  7. LLM will process and return the response

we have implemented in
1. Manually
2. QuestionAnswerAdvisor

#### Advanced RAG
- Using RetrievalAugmentationAdvisor - multiple phases
  1. User request - Pre retreival
  2. Retrieval
  3. Post retreival
  4. Generation

### 1. Pre retreival phase
- happens before fetching any document from vector store db using QueryTransformer interface(modifies the user query before retreival) and QueryExpander interface

- why?
- Because user query can be messy, it can contains too long(500 - 1000 words), missing keyword, contain irrelavant details, in wrong language

- OueryTransformer implementation classes
1. RewriteQueryTransformer class - rewrite or optimize the user query

   - eg:
   - user query: I'm studying Java. What is Multithreading?
   - Rewritten: Defination of Multithreading

2. TranslationQueryTransformer class - translates queries from one language to another lang
3. CompressionQueryTransformer

QueryExpander implementation class
1. MultiQueryExpander class - used to generate multiple version of same query for more robust retrieval

### 2. Retreival phase
    - once the query is transformed, retreival happens

Components in this phase
1. DocumentRetriever interface - VectorStoreDocumentRetriever class which performs similarity search

2. DocumentJoiner interface - used when you fetch documents from multiple sources like vector db, api, web, sql db
    - Joiners will merge results, remove duplicates and produce one clean document using ConcatenationDocumentJoiner class

3. Post retrieval phase
   - After retrieval, we process the fetched document using DocumentPostProcessor interface which includes cleaning, reordering, removing duplicates, remove irrelavant chunks,compresing long document
   - After post processing, the data is clean and sent to LLM

4. Generation
    - In this phase, the query and context are merged, enrich with final prompt using QueryAugmenter interface with implemented ContextualQueryAugmenter  class


Simple RAG = user Query -> retrieval -> send to llm
Advanced RAG = user query -> pre retrieval -> retrieval -> post retrieval -> generation

Spring AI provides 4 interface
1. QueryTransformer
2. DocumentRetriever, DocumentJoiner
3. DocumentPostProcessor
4. QueryAugumenter

#### Steps
1. Create springboot project with web, azure ai, lombok, PGVector, spring ai rag dependency

2. Configure db, api keys in application.properties

3. Create helper class
````
public class Helper {


    public static List<String> getData(){
        return List.of("Java is a platform-independent, object-oriented programming language.",
                "JVM converts Java bytecode into machine code at runtime.",
                "JDK includes the compiler (javac) and JVM tools.",
                "Java supports garbage collection to manage memory automatically.",
                "A class is a blueprint, while an object is an instance of that class.",
                "public static void main(String[] args) is the entry point of a Java program.",
                "Java supports multi-threading using the Thread class or Runnable interface.",
                "synchronized keyword ensures thread safety.",
                "Spring Boot simplifies Java backend development with auto-configuration.",
                "Hibernate ORM maps Java objects to database tables.",
                "JPA is a specification; Hibernate is an implementation.",
                "Java 8 introduced Lambda expressions and Streams API.",
                "The Optional class avoids null pointer exceptions.",
                "In Spring, @Autowired injects dependencies automatically.",
                "Spring Security handles authentication and authorization.",
                "Spring Data JPA provides repository interfaces for database queries.",
                "Microservices in Spring Boot often use Eureka for service discovery.",
                "Spring Cloud Config provides centralized configuration.",
                "Spring Boot applications typically run on an embedded Tomcat server.",
                "REST APIs in Java use @RestController and @RequestMapping.");
    }

}
````
4. Create controller prg
````
@RestController
public class ChatController {

              private ChatClient chatClient;
              private VectorStore vectorStore;
             
              public ChatController(ChatClient.Builder builder,ChatMemory chatMemory,VectorStore vectorStore) {
                             MessageChatMemoryAdvisor messageChatMemoryAdvisor=MessageChatMemoryAdvisor.builder(chatMemory).build();
              this.chatClient=builder.defaultAdvisors(messageChatMemoryAdvisor,new SimpleLoggerAdvisor(),new SafeGuardAdvisor(List.of("Servlets","Struts")))
                                                                         .defaultOptions(AzureOpenAiChatOptions.builder().temperature(0.3).maxTokens(200).build())
                                                                         .build();
                             this.vectorStore=vectorStore;
              }
 
              @GetMapping("/chat")
              public ResponseEntity<String> getChat(@RequestParam(value="q",required=true)String query) {
                             String str=chatClient.prompt().user(query).call().content();
                             return ResponseEntity.ok(str);
              }
}
````
5. Start the appl

6. First we define RetrievalAugumentationAdvisor
````
@GetMapping("/chat")
public ResponseEntity<String> getChat(@RequestParam(value="q",required=true)String query) {
var advisor=RetrievalAugmentationAdvisor.builder().build();

                             String str=chatClient.prompt()
                                                          .advisors(advisor)
                                                          .user(query).call().content();
                             return ResponseEntity.ok(str);
              }
````
7. Pre retrieval phase - happens before fetching document using QueryTransformer  and QueryExpander

1. RewriteQueryTransformer
2. TranslationQueryTransformer
3. CompressionQueryTransformer

````
@GetMapping("/chat")
public ResponseEntity<String> getChat(@RequestParam(value="q",required=true)String query) {
var advisor=RetrievalAugmentationAdvisor.builder()
.queryTransformers(RewriteQueryTransformer.builder().chatClientBuilder(chatClient.mutate().clone()).build())
.queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClient.mutate().clone()).numberOfQueries(3).build())
.build();

                             String str=chatClient.prompt()
                                                          .advisors(advisor)
                                                          .user(query).call().content();
                             return ResponseEntity.ok(str);
              }
````

8. Retrieval phase - we fetch document using DocumentRetriever(use VectorStoreDocumentRetriever)
````
@GetMapping("/chat")
public ResponseEntity<String> getChat(@RequestParam(value="q",required=true)String query) {
var advisor=RetrievalAugmentationAdvisor.builder()
.queryTransformers(RewriteQueryTransformer.builder().chatClientBuilder(chatClient.mutate().clone()).build())
.queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClient.mutate().clone()).numberOfQueries(3).build())
.documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).topK(3).similarityThreshold(0.3).build())
.build();

                             String str=chatClient.prompt()
                                                          .advisors(advisor)
                                                          .user(query).call().content();
                             return ResponseEntity.ok(str);
              }
````
- DocumentJoiner(optional) - If multiple data sources are used, it will merges the data
````
@GetMapping("/chat")
public ResponseEntity<String> getChat(@RequestParam(value="q",required=true)String query) {
var advisor=RetrievalAugmentationAdvisor.builder()
.queryTransformers(RewriteQueryTransformer.builder().chatClientBuilder(chatClient.mutate().clone()).build())
.queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClient.mutate().clone()).numberOfQueries(3).build())
.documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).topK(3).similarityThreshold(0.3).build())
.documentJoiner(new ConcatenationDocumentJoiner())
.build();

                             String str=chatClient.prompt()
                                                          .advisors(advisor)
                                                          .user(query).call().content();
                             return ResponseEntity.ok(str);
              }
````
8. Generation phase using QueryAugumenter interface - ContextualQueryAugumenter class

````
@GetMapping("/chat")
public ResponseEntity<String> getChat(@RequestParam(value="q",required=true)String query) {
var advisor=RetrievalAugmentationAdvisor.builder()
.queryTransformers(RewriteQueryTransformer.builder().chatClientBuilder(chatClient.mutate().clone()).build())
.queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClient.mutate().clone()).numberOfQueries(3).build())
.documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).topK(3).similarityThreshold(0.3).build())
.documentJoiner(new ConcatenationDocumentJoiner())
.queryAugmenter(ContextualQueryAugmenter.builder().build())
.build();

                             String str=chatClient.prompt()
                                                          .advisors(advisor)
                                                          .user(query).call().content();
                             return ResponseEntity.ok(str);
              }
````
9. Start the appl, run  http://localhost:1111/chat?q=what is Java?

10. To translate the query from one lang to another lang we use TranslationQueryTransformer
````
@GetMapping("/chat")
public ResponseEntity<String> getChat(@RequestParam(value="q",required=true)String query) {
var advisor=RetrievalAugmentationAdvisor.builder()
.queryTransformers(RewriteQueryTransformer.builder().chatClientBuilder(chatClient.mutate().clone()).build(),
TranslationQueryTransformer.builder().chatClientBuilder(chatClient.mutate().clone()).targetLanguage("english").build())
.queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClient.mutate().clone()).numberOfQueries(3).build())
.documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).topK(3).similarityThreshold(0.3).build())
.documentJoiner(new ConcatenationDocumentJoiner())
.queryAugmenter(ContextualQueryAugmenter.builder().build())
.build();

                             String str=chatClient.prompt()
                                                          .advisors(advisor)
                                                          .user(query).call().content();
                             return ResponseEntity.ok(str);
              }
````

 