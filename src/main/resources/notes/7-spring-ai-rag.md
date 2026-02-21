# RAG
### RAG(Retrieval Augumented Generation)
- What it means?
  - Retrieval - fetch the most relavant info from ur db based on user query
  - Augumented - combine the retrived info with the users prompt to create context
  - Generation - send this augumented prompt to the LLM which generates the response using provided context


- used to handle huge amount of data
- First we prepare the data by storing ur data in vector db, because it allows similarity search

#### Steps
1. We feed company data into vector db
2. when a user sends a query, ur appl retrieves similar data from db using similarity search
3. retrieved data combined with user query and send to LLM
4. LLM uses this context to generate the accurate response
5. ur appl returns the response to user

#### Why?
1. Limited memory
2. Private data
3. Cost efficiency
4. Accuracy
5. Dynamic update

#### Vector Database
- vector is simply an array of numbers which is represented as matrix
- Vector can represent any kind of info like text or image or audio or video - converted into mathematical form, these numeric array is called embedding
- Vector db are unstructured data stores in embedding and perform similarity search using cosine similarity or Euclidean distance
- We take large document, break it into chunks, convert those chunks into vector called embeddings and store them into database
- Spring AI supports multiple vector db like chrome, Maria db, cassandra, Neo4j, Oracle, PGVector, Pinecone, Redis
- We use Postgressql which by default provide by PGvector plugin to store data as vectors
- You can also use [OpenSearch](install-configure-opensearch-vector-db.md) instead of Postgres PGvector.

1. Install postgressql, we use pgvector to store vector

2. Create springboot project with web, azure ai, pgvector, lombok, vector store advisor dependency(which provides advisor related to rag like VectorStoreChatMemoryAdvisor, QuestionAnswerAdvisor)
````
<dependency>
                                           <groupId>org.springframework.ai</groupId>
                                           <artifactId>spring-ai-advisors-vector-store</artifactId>
                             </dependency>
````
3. Configure api key and db info in application.properties
````
#when we start appl it will create table automatically
spring.ai.vectorstore.pgvector.initialize-schema=true

spring.datasource.url=jdbc:postgresql://localhost:5432/test
spring.datasource.username=postgres
spring.datasource.password=root
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

server.port=1111

spring.ai.azure.openai.api-key=
spring.ai.azure.openai.endpoint=
spring.ai.azure.openai.chat.options.deployment-name=gpt-4.1
spring.ai.azure.openai.embedding.options.deployment-name=text-embedding-3-small

logging.level.org.springframework.ai.chat.client.advisor=DEBUG
````
4. Create helper class which is similar to comma separated document
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

5. Create service class to feed all data into db

````
@Service
public class ChatService {

              @Autowired
              private VectorStore vectorStore;
 
              public void saveData(List<String> list) {
                             List<Document> documentList=list.stream().map(Document::new).toList();
                             vectorStore.add(documentList);
              }
}
````
6. Start the appl, by default it creates a table vector_store with id(uuid), content(text),metadata(text as json), embedding(text in vector format)

7. To test the appl, we write simple test case
````
@SpringBootTest
class AiragApplicationTests {

              @Autowired
              private ChatService chatService;
 
              @Test
              void saveDataToVectorDB() {
                             System.out.println("saving into vector db");
                             chatService.saveData(Helper.getData());
                             System.out.println("Data saved successfully");
                            
              }

}
````
8. Run the test case, we can data is inserted into vector_store table

9. Create prompts folder with system message and user message

system-message.st
````
You are a coding assistant.Explain concepts clearly with examples

Answer only from the DOCUMENTS section. If something is not in DOCUMENTS, reply with "This query is not in my database"

DOCUMENTS:
{documents}
````
user-message.st
````
User query:
{query}
````
10. Create controller prg
````
@RestController
@Slf4j
public class ChatController {

              @Value("classpath:/prompts/user-message.st")
              private Resource userMessage;
             
              @Value("classpath:/prompts/system-message.st")
              private Resource systemMessage;
             
              private ChatClient chatClient;
             
              private VectorStore vectorStore;
             
              public ChatController(ChatClient.Builder builder, ChatMemory chatMemory,VectorStore vectorStore) {
                             MessageChatMemoryAdvisor messageChatMemoryAdvisor=MessageChatMemoryAdvisor.builder(chatMemory).build();
              this.chatClient=builder.defaultAdvisors(messageChatMemoryAdvisor,new SimpleLoggerAdvisor(),new SafeGuardAdvisor(List.of("Spring")))
                                                                         .defaultSystem("You are helpful coding assistant. You are good in coding")
                                                                         .defaultOptions(AzureOpenAiChatOptions.builder().temperature(0.3).maxTokens(200).build())
                                                                         .build();
                             this.vectorStore=vectorStore;
              }
             
              @GetMapping("/chat")
              public ResponseEntity<String> chat(@RequestParam(value="q",required=true)String query) {
                            
                             //load data from vector db
                             SearchRequest searchRequest=SearchRequest.builder()
                                                              .topK(3)   //how many top result to return(3 or 5)
                                                              .similarityThreshold(0.6) //filter the response. A value between 0.0 to 1.0, 1.0-exact match, 0.0-loose match, good range 0.5 to 0.7 to get balanced response
                                                              .query(query)  //user query
                                                              .build();
                            
                             List<Document> documents=vectorStore.similaritySearch(searchRequest);
                            
                             //extract text from list of documents
                             List<String> documentList=documents.stream().map(Document::getText).toList();
                            
                             //contextdata will contain all relavent sentences separated by comma
                             String contextData=String.join(",", documentList);
                            
                             log.info("Context data:{}",contextData);
                            
                             String str=chatClient.prompt()
                                                                       .system(system->system.text(this.systemMessage).param("documents", contextData))
                                                                       .user(user->user.text(this.userMessage).param(query, query))
                                                                       .call()
                                                                       .content();
                            
                             return ResponseEntity.ok(str);
              }
}
````
11. Start the appl


Previously all similarity search and prompt building are done manually.
- Build search request, writing system prompt
  Instead Spring AI provides with 2 advisor
1. QuestionAnswerAdvisor
2. RetrievalAugumentationAdvisor

### QuestionAnswerAdvisor
- It automatically runs similarity search on the vector db - retrieves relavant document - build the prompts - inject context - add instructions- send final prompt to LLM
````
@RestController
@Slf4j
public class ChatController {

              @Value("classpath:/prompts/user-message.st")
              private Resource userMessage;
             
              //@Value("classpath:/prompts/system-message.st")
              //private Resource systemMessage;
             
              private ChatClient chatClient;
             
              private VectorStore vectorStore;
             
              public ChatController(ChatClient.Builder builder, ChatMemory chatMemory,VectorStore vectorStore) {
                             MessageChatMemoryAdvisor messageChatMemoryAdvisor=MessageChatMemoryAdvisor.builder(chatMemory).build();
              this.chatClient=builder.defaultAdvisors(messageChatMemoryAdvisor,new SimpleLoggerAdvisor(),new SafeGuardAdvisor(List.of("Spring")))
                                                                        // .defaultSystem("You are helpful coding assistant. You are good in coding")
                                                                         .defaultOptions(AzureOpenAiChatOptions.builder().temperature(0.3).maxTokens(200).build())
                                                                         .build();
                             this.vectorStore=vectorStore;
              }            
             
              @GetMapping("/chat")
              public ResponseEntity<String> chat(@RequestParam(value="q",required=true)String query) {
                             String str=chatClient.prompt()
                                                                       .advisors(QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.5).build()).build())
                                                                       .user(user->user.text(this.userMessage).param("query",query)).call().content();
                 return ResponseEntity.ok(str);                        
              }
}
````

#### Simple RAG flow - using manually, QuestionAnswerAdvisor
- When user sends a query to our Spring boot appl, we do not send the query directly to LLM
1. We fetch context from our db which is stored inside vector db
2. we perform similarity search on vector db
3. The top relavant document are retrieved
4. we merge this context with user query
5. we send along with prompt to LLM,
6. LLM return the response


#### Advanced RAG using RetrievalAugumentationAdvisor
- It supports multiple phase RAG flow to implement RAG based appl

1. Pre-retrieval
2. Retrieval
3. Post retreival
4. Generation

- To work with RetrievalAugumentationAdvisor we need spring-ai-rag dependency
````
<dependency>
                                           <groupId>org.springframework.ai</groupId>
                                           <artifactId>spring-ai-rag</artifactId>
                             </dependency>
````
2. create endpoint for advanced rag
````
              @GetMapping("/chat")
              public ResponseEntity<String> chat(@RequestParam(value="q",required=true)String query) {
                             var advisor=RetrievalAugmentationAdvisor.builder()
                                                                    .documentRetriever(VectorStoreDocumentRetriever.builder()
                                                                                          .vectorStore(vectorStore)
                                                                                          .topK(3)
                                                                                          .similarityThreshold(0.5).build())
                                                                    .build();
                             String str=chatClient.prompt()
                           .advisors(advisor)
                           .user(user->user.text(this.userMessage).param("query",query)).call().content();
       
                             return ResponseEntity.ok(str);  
              }
````

### Handling multiple documents in vector database
- with 3 PDF documents you usually do NOT need 3 different tables.In most RAG systems, the best default design is:

- One vector “table/collection/index” for all chunks store which PDF each chunk came from as metadata (e.g., documentId, fileName, source, page, chunkId).
- At query time, you can filter (WHERE-like) by that metadata when needed
- This is exactly what many vector systems and RAG guides recommend: store embeddings + metadata together so you can do vector similarity search + metadata filtering.
````
{
"id": "pdf1_p12_c3",
"vector": [ ...embedding... ],
"text": "chunk text ...",
"metadata": {
"document_id": "pdf1",
"file_name": "Policy.pdf",
"page": 12,
"chunk": 3,
"tenant": "projectA",
"category": "policy"
}
}
````

You call:
````
chatClient.prompt()  
.user(question)  
.advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION,      "document_id == 'PDF_1' AND tenant_id == 'HR_TEAM'"  ))  
.call();
````

At this point:
- LLM is NOT called yet
- Advisor intercepts the request


What QuestionAnswerAdvisor does internally
QuestionAnswerAdvisor is not a search engine. It does three orchestration steps:

Step A 
- Build a SearchRequest 
- It creates a SearchRequest object containing:

    - query → user question text
    - topK → number of chunks (e.g. 5)
    - similarityThreshold (optional)
    - filterExpression → your security rules

Think of it as:
SearchRequest {  query = "What is the leave policy?"  
topK = 5  
filter = "document_id == 'PDF_1' AND tenant_id == 'HR_TEAM'"}

Step B 
- Call the VectorStore The advisor then calls:

- vectorStore.similaritySearch(searchRequest);

- Now control moves to PgVectorStore.

3- What PgVectorStore does  This is where real work happens.
Step 1 – Generate query embedding
PgVectorStore calls the Azure OpenAI Embedding model:

"What is the leave policy?"↓
[0.0123, -0.4421, ..., 0.9981]

-This vector represents the question
-Same dimension as stored embeddings (e.g. 1536)

Step 2 – Translate filter expression → SQL

Your filter:

document_id == 'PDF_1' AND tenant_id == 'HR_TEAM'

is converted to PostgreSQL JSON filtering:

metadata ->> 'document_id' = 'PDF_1'AND metadata ->> 'tenant_id' = 'HR_TEAM'


Step 3 – Execute ONE SQL query (this is the core)
PgVectorStore runs one SQL query, conceptually like this:

SELECT  id,  content,  metadata,  
embedding <=> :query_embedding AS distance
FROM vector_store
WHERE metadata ->> 'document_id' = 'PDF_1'  
AND metadata ->> 'tenant_id' = 'HR_TEAM'
ORDER BY embedding <=> :query_embedding
LIMIT 5;

Key points:

WHERE = access control + document filtering
<=> = pgvector cosine distance
ORDER BY = similarity ranking
LIMIT = top‑K

✅ PostgreSQL does everything
✅ Spring AI just issued the query

4. How pgvector computes “similarity”
   embedding <=> query_embedding means:

Compute cosine distance between two vectors
Lower distance = more similar

Internally:

pgvector uses HNSW or IVFFlat index
Only a subset of rows is scanned
Very fast even with large datasets
 
