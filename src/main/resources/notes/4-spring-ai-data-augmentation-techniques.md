# Data Augmentation Techniques
To get actual data from LLM
## Stuffing of prompt
- Adding our own data to the context

#### Steps
- Create play.st
````
Use the following pieces of context to answer the question at the end. If you don't know the answer just say "I'm sorry but I don't know the answer to that".

{context}

Question: {question}
````

- Create sports.txt
````
Archery, athletics, badminton, basketball , basketball 3×3, boxing, canoe slalom, canoe sprint, road cycling, cycling track, mountain biking, BMX freestyle, BMX racing, equestrian, fencing, football, golf, artistic gymnastics, rhythmic gymnastics, trampoline, handball, hockey, judo, modern pentathlon, rowing, rugby, sailing, shooting, table tennis, taekwondo, tennis, triathlon, volleyball, beach volleyball, diving, marathon swimming, artistic swimming, swimming, water polo, weightlifting,wrestling,breaking, sport climbing, skateboarding, and surfing.
````

- Create endpoint
````
@Value("classpath:/prompts/play.st")
private Resource promptResource1;

              @Value("classpath:/docs/sports.txt")
              private Resource contextResource;
 
    @GetMapping("/sports")
              public String getSporstInformation(@RequestParam(value="message", defaultValue="What sports are being included in the 2026 Olympics?")String message,
                                           @RequestParam(value="stuffit", defaultValue="false")boolean stuffit) {
               PromptTemplate pt=new PromptTemplate(promptResource1);
               Map<String,Object> map=new HashMap<>();
               map.put("question",message);
              
               if(stuffit) {
                             map.put("context", contextResource);
               } else {
                             map.put("context","");
               }
              
               Prompt p=pt.create(map);
               ChatResponse response=chatClient.call(p);
               return response.getResult().getOutput().getText();
              }
````

## Function calling (used in lower version of spring AI - 1.0.6)
- It makes LLM decide when to call Java methods(functions) instead of just returning plain text.
- used with real appl logic(DB calls, API's, calculations)

#### Why?
- Without function calling, LLM can generate only plain text, it cannot query db, call rest api, perform secure business logic
- LLM decide which function to invoke and execute the function safely

#### Steps
1. Create springboot project with web, azure ai, lombok dependency

2. Configure api key, weather api key in application.properties
````
spring.weather.api.base.uri = http://api.weatherapi.com/v1
spring.weather.api.key = e700ba48f0694e9da32164847261102
````

3. We define request and response in Weather class
````
public class Weather {
public record Request(String city) { }
public record Response(Location location, Current current) {}
public record Location(String name, String country) {}
public record Current(String temp_c) {}
}
````

4. Create class which make call to external api (ie) weatherapi and get actual response based on Weather class
````
@Service
public class WeatherServiceBuilder {

              @Value("${spring.weather.api.base.uri}")
              private String weatherURI;
             
              @Value("${spring.weather.api.key}")
              private String weatherKey;
             
              //call weatherapi endpoint
              private RestClient restClient=RestClient.create();
             
              public Weather.Response getWeather(String city){
                             return restClient.get()
                                                                   .uri(builder -> builder
                                                                          .path("/current.json")
                                                                          .queryParam("key",weatherKey)
                                                                          .queryParam("q", city)
                                                                          .build())
                                                                 .retrieve()
                                                                 .body(Weather.Response.class);
              }
}
````

5. Integrate service class with LLM, create a class that implements Function functional interface
````
@Service
public class WeatherService implements Function<Weather.Request, Weather.Response>{

              @Autowired
              WeatherServiceBuilder serviceBuilder;
             
              @Override
              public Response apply(Request t) {
                             return serviceBuilder.getWeather(t.city());
              }

}
````

6. We have to register this function in config class
````
@Configuration
public class Config {

              @Bean
              @Description("Get the weather of the city")
              Function<Weather.Request,Weather.Response> currentWeather() {
                             return new WeatherService();
              }
}
````

7. We have to utilize this function in our LLM, so for that we create another service
````
@Service
public class AIService {

              @Autowired
              ChatModel chatModel;
 
              public ChatResponse getWeatherInfo(String query) {
                             UserMessage msg=new UserMessage(query);
                             return chatModel.call(new Prompt(msg,AzureOpenAiChatOptions.builder().withFunction("currentWeather").build()));
              }
}
````

8. Create controller prg
````
@RestController
public class WeatherController {

              @Autowired
              AIService service;
 
              @PostMapping("/query")
              public Map<String,String> getWeatherDetails(@RequestParam String query) {
                             return Map.of("response",service.getWeatherInfo(query).getResult().getOutput().getContent());
              }
}
````

9. Start the appl


## Tool calling
- we can allow LLM to access any external api's, external system or external data sources to fetch real time or accurate data

#### Usages
- Fetch data from internet
- search the web when "web access" options is enabled
- Get current date/time
- Provide live weather info, live stock market info
- Access internal db

#### Steps
1. Create springboot project with web, azure ai,lombok dependency

2. Configure api key in application.properties

3. Create config class
````
@Configuration
public class Config {

              @Bean
              public ChatClient chatClient(ChatClient.Builder builder) {
                             return builder.build();
              }
}
````

4. Create controller
````
@RestController
@RequestMapping("/chat")
public class ChatController {

              @Autowired
              ChatService service;
 
              @GetMapping
              public String getInfo(@RequestParam String msg) {
                             return service.getChat(msg);
              }
}
````

5. Create service
````
@Service
public class ChatService {

              private ChatClient chatClient;
             
              public ChatService(ChatClient chatClient) {
                             this.chatClient = chatClient;
              }
 
              public String getChat(String msg) {
                             return chatClient.prompt().user(msg).call().content();
              }
}
````

6. Start the appl, run  
````
http://localhost:3000/chat?msg=provide logic of prime number

run  http://localhost:3000/chat?msg=what is todays date?

http://localhost:3000/chat?msg=what is weather right now?
````

#### LLM normally cannot answer correctly because thet dont have realtime data. But with tool calling we can expose
- A function that return todays date
- A function that return the weather
- A function that queries the db
- A function that calls any external API
- A function that triggers business logic in ur system

#### Two main purpose
1. Information retrieval - fetching info such as current date, weather info
2. Action taking - like setting an alarm, creating a table in a db, inserting data, sending email

#### Two ways to use tool calling
1. Declarative approach - using @Tool
2. Programmatic approach


- We create a tool that return current date and time using @Tool
- @Tool - used to describe the tool
- description - description of the tool
- @ToolParam - to pass parameter to the tools

````
public class SimpleTools {

//information tool
@Tool(description = "Get the current date and time in users zone")
public String getCurrentDateTime() {
return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();             
}
}
````

- Attach the tool to the ChatClient inside service prg
````
public String getChat(String msg) {
return chatClient.prompt()
.tools(new SimpleTools())
.user(msg)
.call()
.content();
}
````
- Start the appl, run 
````
http://localhost:3000/chat?msg=what is the date of tomorrow?

http://localhost:3000/chat?msg=what was the date last sunday?

http://localhost:3000/chat?msg=My date of birth of 1986. What is my current age?
````

#### Flow
- Whenever LLM receives the request, 
- it realize that to answer the question, it needs to call the tool. 
- It informs SpringAI to invoke the tool. 
- Spring AI will execute the tool 
- and return the result to model. 
- The model will return final response
````
@Slf4j
public class SimpleTools {

              //information tool
              @Tool(description = "Get the current date and time in users zone")
              public String getCurrentDateTime() {
                             return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();             
              }
             
              //action tool
              @Tool(description = "Set the alarm for given time")
              public void setAlarm(@ToolParam(description = "Time in ISO-8601 format")String time) {
                             var dateTime=LocalDateTime.parse(time,DateTimeFormatter.ISO_DATE_TIME);
                             log.info("Set the alarm for given time.{}",dateTime);
              }
             
             
              //returnDirect - whether the tool result should be returned directly to the client instead of sending it back to LLM to compose the final response
              //default is false (ie) first it goes to LLM
              @Tool(name="fx_rate",description = "Get current FX rate from base to quote (USD to INR)", returnDirect = true)
              public String getRate(@ToolParam(description = "Base currency code USD",required = true)String base,
                                           @ToolParam(description = "Quote currency code INR", required=true)String quote) {
                             return String.format("Rate %s%s=%.4f",base,quote,85.2345);
              }
}
````
- Start the appl,
````
http://localhost:3000/chat?msg=set the alarm for today 9pm

http://localhost:3000/chat?msg=what is the FX rate USD TO INR?
````

#### When?
1. Database operation
2. Sending emails
3. Weather API
4. Stock market tools

To access weather api
1. Configure weather api key in application.properties
````
app.weather.api-key=b7565f85a8924271ae2145425242511
````
2. Calling the actual weather api using RestClient in Config class
````
@Configuration
public class Config {

              @Bean
              public ChatClient chatClient(ChatClient.Builder builder) {
                             return builder.build();
              }
             
              @Bean
              public RestClient weatherRestClient() {
                             return RestClient.builder().baseUrl(http://api.weatherapi.com/v1).build();
              }
}
````
3. Create WeatherTool logic
````
@Service
public class WeatherTool {

              private RestClient restClient;
 
              public WeatherTool(RestClient restClient) {
                             this.restClient = restClient;
              }
 
    @Value("${app.weather.api-key}")
    private String weatherKey;
 
              @Tool(description = "Get weather information of given city")
              public String getWeather(@ToolParam(description = "city of which we want to get weather information")String city) {
                             var response=restClient.get()        
                                                                 .uri(builder -> builder.path("/current.json").queryParam("key", weatherKey).queryParam("q", city).build())
                                                                 .retrieve()
                                                                 .body(new ParameterizedTypeReference<Map<String,Object>>() {
                                                                        });
                             return response.toString();
              }
}
````
4. Configure WeatherTool in service prg
````
@Service
public class ChatService {

              private ChatClient chatClient;
              private WeatherTool weatherTool;
             
              public ChatService(ChatClient chatClient,WeatherTool weatherTool) {
                             this.chatClient = chatClient;
                             this.weatherTool=weatherTool;
              }
 
              public String getChat(String msg) {
                             return chatClient.prompt()
                                                                  .tools(new SimpleTools(),weatherTool)
                                                                  .user(msg)
                                                                  .call()
                                                                  .content();
              }
}
````
5. Start the appl, run  
````
http://localhost:3000/chat?msg=what is the current temperature in London?
````


````
              @Tool(description = "Fetch employee info based on id")
              public String getInfo(@ToolParam(description = "id of emp")Integer id) {
                             //logic to Restclient to call controller on other appl
              }
````
- Get weather of chennai

---

## 🌟 Developer/Contributor
Name: Rohit Shamrao Muneshwar  
Email: rohit.muneshwar1406@gmail.com  
LinkedIn Profile: [Click Here](https://www.linkedin.com/in/rohit-muneshwar-a9079258/)  
Other Github repositories: [Click Here](https://github.com/rohit1406?tab=repositories)

---