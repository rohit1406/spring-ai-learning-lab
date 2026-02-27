# Streaming and Multi-user access to chat history
Streaming response in Spring AI - asynchronous and non blocking
- User sends the request 
- we build the prompt and send it to the model 
- Instead of waiting for the full response, the model starts sending small chunk of data as they are generated

#### Steps
- In ChatController, create endpoint
````
@GetMapping("/streamchat")
public ResponseEntity<Flux<String>> streamChat(@RequestParam(value="msg",required=true)String query) {
Flux<String> str=chatClient.prompt()
.system(system->system.text(this.systemMessage))
.user(user->user.text(this.userMessage).param("concept", query))
.stream()
.content();
return ResponseEntity.ok(str);
}
````
- If u need more details abt response then we can use Flux<ChatResponse> or Flux<ChatClientResponse>

- stream() will call adviseStream() to generate response asynchronously

- Start the appl, run  
````
http://localhost:2000/streamchat?msg=java%20new%20features
````

#### why?
1. Non blocking - user dosent wait for the entire response
2. Better UI
3. Scalable - works well for large response or slow models


### How multiple users can interact with LLM ?
- Previously we store messages using MessageWindowChatMemory 
- which internally uses InMemoryChatMemoryRepository 
- which stores messages in the memory temporarily, 
- once appl stops the msg will be deleted

Problems
1. Our appl interacts with LLM to get response
2. Users of our appl is not limited to one person

For example, if user1 and user2 are using appl, each should have their own messages history maintained, but now we can only maintain one message history.

- ChatMemory uses MessageWindowChatMemory which stores default 20msgs, 
- it contains reference of ChatMemoryRepository 
- which default implementation is InMemoryChatMemoryRepository 
- which uses ConcurrentHashmap to stores msg in memory where
- key=conversationid, value=List of messages

- By default conversationId is "default", so all messages goes under same key. 
- To maintain session for multiple user we have to use separate conversationid for each user 
- and pass it as a header so each session created for each user

````
@GetMapping("/chat")
public ResponseEntity<String> getMessage(@RequestParam(value="msg",required=true)String query,@RequestHeader("userId") String userId) {
String str=chatClient.prompt()
.advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
.system(system->system.text(this.systemMessage))
.user(user->user.text(this.userMessage).param("concept", query))
.call()
.content();
return ResponseEntity.ok(str);
}
````
- Start the appl, run  
````
http://localhost:2000/chat?msg=my name is Gim, in headers
  key: userId   value: Gimuser

http://localhost:2000/chat?msg=my name is Lim, in headers
key: userId   value: Limuser

http://localhost:2000/chat?msg=What is my name?, in headers
key: userId   value: Gimuser
````
- We maintain separate session for each user

Drawback
- Messages are stored in memory, so once appl restarted all msg will be lost


#### Chat Conversation using JdbcChatMemoryRepository
- Previously we use InMemoryChatMemoryRepository which stores message in memory and once appl restarted all msg will be lost
- If we want to store converation history permanently in db using JdbcChatMemoryRepository

1. Add dependency in pom.xml
````
      <dependency>
                                           <groupId>org.springframework.ai</groupId>
                                           <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
                             </dependency>
                             <dependency>
                                           <groupId>com.mysql</groupId>
                                           <artifactId>mysql-connector-j</artifactId>
                                           <version>8.4.0</version>
                             </dependency>
````
2. Configure db info in application.properties
````
spring.datasource.url=jdbc:mysql://localhost:3306/demo1
spring.datasource.username=root
spring.datasource.password=root

#controls when to initialize schema - embedded, always, never
spring.ai.chat.memory.repository.jdbc.initialize-schema=always
````
3. Start the appl, Spring AI will automatically create table spring_ai_chat_memory with columns
````
mysql> desc spring_ai_chat_memory;
+-----------------+------------------------------------------+------+-----+---------+-------+
| Field           | Type                                     | Null | Key | Default | Extra |
+-----------------+------------------------------------------+------+-----+---------+-------+
| conversation_id | varchar(36)                              | NO   | MUL | NULL    |       |
| content         | text                                     | NO   |     | NULL    |       |
| type            | enum('USER','ASSISTANT','SYSTEM','TOOL') | NO   |     | NULL    |       |
| timestamp       | timestamp                                | NO   |     | NULL    |       |
+-----------------+------------------------------------------+------+-----+---------+-------+
4 rows in set (0.01 sec)
````

- To customize the storing of message, since by default it stores only 20 msg
````
              @Bean
              public ChatMemory chatMemory(JdbcChatMemoryRepository repo) {
                             return MessageWindowChatMemory.builder()
                                                          .chatMemoryRepository(repo).maxMessages(100).build();
                                                         
              }
````

Drawbacks
1. works well for limited message
2. For smaller use case for storing limited msg in db


---

## 🌟 Developer/Contributor
Name: Rohit Shamrao Muneshwar  
Email: rohit.muneshwar1406@gmail.com  
LinkedIn Profile: [Click Here](https://www.linkedin.com/in/rohit-muneshwar-a9079258/)  
Other Github repositories: [Click Here](https://github.com/rohit1406?tab=repositories)

---