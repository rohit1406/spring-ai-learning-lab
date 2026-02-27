# RAG
## RAG with multiple pdf/doc files

#### Steps
1. Create springboot project with web, azure ai, lombok, pgvector, tikadocumentreader dependency

2. Configure pdf files in  docs folder inside resources

3. Configure db and api key info in application.properties
````
spring.ai.vectorstore.pgvector.initialize-schema=true

spring.datasource.url=jdbc:postgresql://localhost:5432/test1
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
4. Create a class to read both pdf and insert into same table
````
@Service
public class PdfIngestionService {

              private final VectorStore vectorStore;
             
              public PdfIngestionService(VectorStore vectorStore) {
                             this.vectorStore = vectorStore;
              }
             
              public void ingest(Resource pdf, String documentId, String tenantId) {
                             TikaDocumentReader reader=new TikaDocumentReader(pdf);
                            
                             List<Document> documents=reader.get();
                            
                             //convert document into chunks
                             //defaultChunksize - target size of each text chunk in size
                             //minChunkSizeChars - min size of each text chunk in char
                             //minChunkLenghtToEmbed - min length of chunk to be included
                             //maxNumChunks - max no of chunks to generate from a text
                             //keepSeparator - whether to keep separator
                             TokenTextSplitter splitter=new TokenTextSplitter(800, 350, 5, 10000, true);
                             List<Document> document=splitter.apply(documents);
                            
                             //add metadata for filtering
                             List<Document> enriched=document.stream()
                                                          .map(d->new Document(d.getText(),Map.of("document_id",documentId,"tenant_id",tenantId)))
                                                          .toList();
                            
                             vectorStore.add(enriched);
                            
              }
}
````
5. Load the pdf file at time of startup
````
@Component
public class DataLoader {

              @Autowired
              PdfIngestionService service;
 
              @PostConstruct
              public void load() {
                             service.ingest(new ClassPathResource("docs/HR_Policy.pdf"), "PDF_1", "HR_TEAM");
                             service.ingest(new ClassPathResource("docs/Finance_Policy.pdf"), "PDF_2", "FINANCE_TEAM");
              }
}
````
6. Advisor configuration
````
@Configuration
public class AIConfig {

              @Bean
              QuestionAnswerAdvisor qaAdvisor(VectorStore vectorStore) {
                             return QuestionAnswerAdvisor.builder(vectorStore)
                                                                              .searchRequest(SearchRequest.builder().topK(5).similarityThreshold(0.4).build())
                                                                              .build();
              }

}
````
7. Create controller prg

````
@RestController
@RequestMapping("/chat")
public class ChatController {

              private final ChatClient chatClient;
             
              public ChatController(ChatModel chatModel, QuestionAnswerAdvisor qaAdvisor) {
                             this.chatClient=ChatClient.builder(chatModel)
                                                                            .defaultAdvisors(qaAdvisor)
                                                                            .build();
              }
 
              @PostMapping
              public String chat(@RequestParam String question,
                                           @RequestParam String documentId,
                                           @RequestHeader("X-TENANT")String tenantId) {
                             return chatClient.prompt()
                                                                   .user(question)
                                                                   .advisors(a->a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "document_id=="+documentId+"AND tenant_id=="+tenantId)).call().content();
              }
}
````
8. Start the appl

9. Run POST request,  
````
http://localhost:1111/chat?question=Do travel expenses require approval?&documentId=PDF_2
````
### Now we want to perform keyword search based on particular word

````
@Service
public class PdfIngestionService {

              private final VectorStore vectorStore;
             
              public PdfIngestionService(VectorStore vectorStore) {
                             this.vectorStore = vectorStore;
              }
             
              public void ingest(Resource pdf, String documentId, String tenantId) {
                             TikaDocumentReader reader=new TikaDocumentReader(pdf);
                            
                             List<Document> documents=reader.get();
                            
                             //convert document into chunks
                             //defaultChunksize - target size of each text chunk in size
                             //minChunkSizeChars - min size of each text chunk in char
                             //minChunkLenghtToEmbed - min length of chunk to be included
                             //maxNumChunks - max no of chunks to generate from a text
                             //keepSeparator - whether to keep separator
                             TokenTextSplitter splitter=new TokenTextSplitter(800, 350, 5, 10000, true);
                             List<Document> document=splitter.apply(documents);
                            
                             //add metadata for filtering
                             /*List<Document> enriched=document.stream()
                                                          .map(d->new Document(d.getText(),Map.of("document_id",documentId,"tenant_id",tenantId)))
                                                          .toList();*/
                            
                             List<Document> enriched=document.stream().map(d -> {
                                           String text=d.getText().toLowerCase();
                                           String topic="GENERAL";
                                          
                                           if(text.contains("leave")) {
                                                          topic="LEAVE";
                                           }else if(text.contains("expense")) {
                                                          topic="EXPENSE";
                                           }else if(text.contains("tax")) {
                                                          topic="TAX";
                                           }else if(text.contains("code of conduct")) {
                                                          topic="CONDUCT";
                                           }
                                          
                                           return new Document(d.getText(),Map.of("document_id",documentId,"tenant_id",tenantId,"topic",topic));
                             }).toList();
                            
                             vectorStore.add(enriched);
                            
              }
}
````
11. Create endpoint for keyword search
````
@GetMapping("/search")
public String searchByKeyword(@RequestParam String word,
@RequestParam String documentId,
@RequestHeader("X-TENANT")String tenantId) {

                             String filter="document_id=='"+documentId+"'"+" AND tenant_id=='"+tenantId+"'"+" AND topic!='GENERAL'";
                            
                             return chatClient.prompt()
                                      .system("""
                                                           Return only the exact matching policy statements from the retrieved documents.
                                                           Do not add new information. If nothing matches, respond exactly: No matching policy found.
                                                           """)
                                      .user("policy related to "+word)
                                      .advisors(a->a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION,filter))
                                      .call().content();
                            
              }
````
12. Start the appl and run
````
http://localhost:1111/chat/search?word=days&documentId=PDF_2
````

## MultiHop RAG
- Vector db dosent jump between pdf by itself when pdf have relationship

- pdf1 - pdf2 - pdf3

- Those relationship must be modeled as metadata

- chained retrieval
- Search PDF_1 - Read metadata - find refernce to PDF_2 - Search PDF_2 - Read metadata - find refernce to PDF_3 - Search PDF_3 - combine result - send LLM - user will get response

- This concept is called multi hop RAG
````
Vector db - semantic search on embedding
Metadata - navigation
your code - orchestration
````

#### Steps
1. Create springboot project with web, azure ai, lombok, pgvector, tikadocumentreader dependency

2. Configure pdf files in  docs folder inside resources

3. Configure db and api key info in application.properties
````
spring.ai.vectorstore.pgvector.initialize-schema=true

spring.datasource.url=jdbc:postgresql://localhost:5432/test1
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

4. Create a class to read both pdf and insert into same table
````
   @Service
   public class PdfIngestionService {

              private final VectorStore vectorStore;
             
              public PdfIngestionService(VectorStore vectorStore) {
                             this.vectorStore = vectorStore;
              }
             
              public void ingest(Resource pdf, String documentId, String tenantId) {
                             TikaDocumentReader reader=new TikaDocumentReader(pdf);
                            
                             List<Document> documents=reader.get();
                            
                             //convert document into chunks
                             //defaultChunksize - target size of each text chunk in size
                             //minChunkSizeChars - min size of each text chunk in char
                             //minChunkLenghtToEmbed - min length of chunk to be included
                             //maxNumChunks - max no of chunks to generate from a text
                             //keepSeparator - whether to keep separator
                             TokenTextSplitter splitter=new TokenTextSplitter(800, 350, 5, 10000, true);
                             List<Document> document=splitter.apply(documents);
                            
                             //Document level reference to metadata
                             List<String> references=switch(documentId) {
                             case "PDF_1" -> List.of("PDF_2");
                             case "PDF_2" -> List.of("PDF_3");
                             default -> List.of();
                             };
                            
                             List<Document> enriched = document.stream().map(d-> {
                                           Map<String,Object> meta=new HashMap<>();
                                           meta.put("document_id",documentId);
                                           meta.put("tenant_id",tenantId);
                                           meta.put("references",references);
                                          
                                           return new Document(d.getText(),meta);
                             }).toList();
                            
                             vectorStore.add(enriched);
                            
              }
}
````

````
@Component
public class DataLoader {

              @Autowired
              PdfIngestionService service;
 
              @PostConstruct
              public void load() {
                             service.ingest(new ClassPathResource("docs/HR_Policy.pdf"), "PDF_1", "ORG_POLICY");
                             service.ingest(new ClassPathResource("docs/Finance_Policy.pdf"), "PDF_2", "ORG_POLICY");
                             service.ingest(new ClassPathResource("docs/Tax_Policy.pdf"), "PDF_3", "ORG_POLICY");
              }
}
````

6. Advisor configuration
````
@Configuration
public class AIConfig {

              @Bean
              QuestionAnswerAdvisor qaAdvisor(VectorStore vectorStore) {
                             return QuestionAnswerAdvisor.builder(vectorStore)
                                                                              .searchRequest(SearchRequest.builder().topK(5).similarityThreshold(0.4).build())
                                                                              .build();
              }

}
````
7. Create MultiHopSearchService to write multi hop retrieval logic
````
@Service
public class MultiHopSearchService {

              private final VectorStore vectorStore;
             
              public MultiHopSearchService(VectorStore vectorStore) {
                             this.vectorStore = vectorStore;
              }
 
              public Map<String,Object> multiHopSearch(String query,String startDocumentId,String tenantId,int maxHops,int topK, double threshold) {
                            
                             //store result of each hop which contains documentid, tenantid, next reference
                             List<Map<String,Object>> hops=new ArrayList<>();
                            
                             Set<String> visited=new LinkedHashSet<>();
                            
                             //set the entry document
                             String currentDocId=startDocumentId;
                            
                             //stops when maxHop is reached
                             for(int hop=1;hop<=maxHops && currentDocId != null; hop++) {
                                           if(visited.contains(currentDocId))
                                                          break;
                                           visited.add(currentDocId);
                                          
                                           String filter="document_id=='"+currentDocId+"'"+" AND tenant_id=='"+tenantId+"'";
                                          
                                           String hopQuery=(hop==1)?"refer to policy":query;
                                          
                                           List<Document> retreived=vectorStore.similaritySearch(SearchRequest.builder()
                                                                             .query(hopQuery)
                                                                             .topK(topK)
                                                                             .similarityThreshold(hop==1?0.15:threshold)
                                                                             .filterExpression(filter).build());
                                          
                                           Set<String> nextRef=new LinkedHashSet<>();
                                          
                                           for(Document d: retreived) {
                                                          Object refs=d.getMetadata().get("references");
                                                          if(refs instanceof List<?> list) {
                                                                        for(Object o:list) {
                                                                                      if(o!=null)
                                                                                                    nextRef.add(o.toString());
                                                                        }
                                                          }
                                           }
                                          
                                           Map<String,Object> hopResult=new LinkedHashMap<String, Object>();
                                           hopResult.put("hop", hop);
                                           hopResult.put("documentId", currentDocId);
                                           hopResult.put("queryUsed", hopQuery);
                                           hopResult.put("snippets", retreived.stream().map(Document::getText).toList());
                                           hopResult.put("nextReferences", nextRef);
                                          
                                           hops.add(hopResult);
                                           currentDocId=nextRef.stream().findFirst().orElse(null);
                             }
                             return Map.of("query",query,"startDocumentId",startDocumentId,"tenantId",tenantId,"visited",visited,"hops",hops);
              }
}
````
8. Create controller
````
@RestController
@RequestMapping("/chat")
public class ChatController {

              private final ChatClient chatClient;
              private final MultiHopSearchService multiHopSearchService;
             
              public ChatController(ChatModel chatModel, QuestionAnswerAdvisor qaAdvisor,MultiHopSearchService multiHopSearchService) {
                             this.chatClient=ChatClient.builder(chatModel)
                                                                            .defaultAdvisors(qaAdvisor)
                                                                            .build();
                             this.multiHopSearchService=multiHopSearchService;
              }
             
              @GetMapping("/search/multi")
              public Map<String,Object> multiHop(@RequestParam String q,@RequestParam String startDocumentId,
                                           @RequestHeader("X-TENANT")String tenantId,
                                           @RequestParam(defaultValue="3")int maxHops,
                                           @RequestParam(defaultValue="5")int topK,
                                           @RequestParam(defaultValue="0.3")double threshold) {
                             return multiHopSearchService.multiHopSearch(q, startDocumentId, tenantId, maxHops, topK, threshold);
              }

}
````
9. Start the appl
````
http://localhost:1111/chat/search/multi?q=tax%20rules&startDocumentId=PDF_1

Response is as follows:
{
"tenantId": "ORG_POLICY",
"hops": [
{
"hop": 1,
"documentId": "PDF_1",
"queryUsed": "refer to policy",
"snippets": [
"HR Policy Document\nLeave Policy:\nEmployees are entitled to 20 days of paid leave per year.\nLeave requests must be approved by the reporting manager.\nCode of Conduct:\nAll employees must adhere to company ethics and compliance policies.\nReference: For expenses and reimbursement rules, see Finance_policy.pdf (PDF_2)."
],
"nextReferences": [
"PDF_2"
]
},
{
"hop": 2,
"documentId": "PDF_2",
"queryUsed": "tax rules",
"snippets": [
"Finance Policy Document\nExpense Policy:\nAll expenses must be approved and submitted within 30 days.\nTravel expenses require prior approval.\nTax Compliance:\nAll financial operations must comply with statutory regulations.\nReference: For detailed statutory tax rules, see Tax_policy.pdf (PDF_3)."
],
"nextReferences": [
"PDF_3"
]
},
{
"hop": 3,
"documentId": "PDF_3",
"queryUsed": "tax rules",
"snippets": [
"Tax Policy Document\nStatutory Regulations:\nAll financial operations must comply with statutory regulations.\nRecords must be retained as per local statutory requirements."
],
"nextReferences": []
}
],
"visited": [
"PDF_1",
"PDF_2",
"PDF_3"
],
"startDocumentId": "PDF_1",
"query": "tax rules"
}
````

---

## 🌟 Developer/Contributor
Name: Rohit Shamrao Muneshwar  
Email: rohit.muneshwar1406@gmail.com  
LinkedIn Profile: [Click Here](https://www.linkedin.com/in/rohit-muneshwar-a9079258/)  
Other Github repositories: [Click Here](https://github.com/rohit1406?tab=repositories)

---