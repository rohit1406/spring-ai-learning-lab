# AI Agents And Model Context Protocol (MCP)
## AI Agents
It is a small piece of code that uses an AI models to understand the goal and decide what to do, instead of just replying the text

#### chatbots(answer question) vs                          AI Agent(does some work)
1. just answer question             1. Understand the goal and decide what to do
2. No actions                       2. call java methods
3. one response                     3. Multi step thinking
4. "Here is answer"                 4. "Here is the result after doing some logic"

Whenever user asks any query
1. AI understand the goal
2. AI decides it needs some evaluation
3. AI calls some tools automatically (tools calling)
4. Tool return result
5. AI uses the result to answer

AI AGENT = think + act + respond

### Steps

1. Create springboot appl with web, azure ai, data jpa, h2, lombok dependency

2. Configure db and azure api keys in application.properties

3. Create entity class
````
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
@Id
private Integer id;
private String name;
private Integer age;
private String address;
private Double salary;
}
````
4. Create repo interface
````
public interface EmployeeRepository extends JpaRepository<Employee, Integer>{

}
````
5. Create service
````
@Service
public class EmployeeService {

              @Autowired
              EmployeeRepository empRepo;
             
              public List<Employee> getAllEmployees() {
                             return empRepo.findAll();
              }
             
              public Employee getEmployeeById(Integer id) {
                             return empRepo.findById(id).get();
              }
             
              public Employee updateEmployeeById(Employee emp,Integer id) {
                             Employee e=empRepo.findById(id).get();
                             if(!ObjectUtils.isEmpty(emp.getAddress()))
                                           e.setAddress(emp.getAddress());
                             if(!ObjectUtils.isEmpty(emp.getAge()))
                                           e.setAge(emp.getAge());
                             if(!ObjectUtils.isEmpty(emp.getName()))
                                           e.setName(emp.getName());
                             if(!ObjectUtils.isEmpty(emp.getSalary()))
                                           e.setSalary(emp.getSalary());
                             return empRepo.save(e);
              }
             
              public Employee save(Employee e) {
                             return empRepo.save(e);
              }
             
              public void deleteEmployeeById(Integer id) {
                             empRepo.deleteById(id);
              }
}
````

6. Create controller
````
@RestController
@RequestMapping("/employee")
public class EmployeeController {

              @Autowired
              EmployeeService empService;
             
              @PostMapping("/save")
              public Employee save(@RequestBody Employee e) {
                             return empService.save(e);
              }
             
              @GetMapping("/getAll")
              public List<Employee> getAllEmployee(){
                             return empService.getAllEmployees();
              }
             
              @GetMapping("/getEmployee/{id}")
              public Employee getEmployeeById(@PathVariable("id") Integer id){
                             return empService.getEmployeeById(id);
              }
             
              @PutMapping("/updateEmployee/{id}")
              public Employee updateEmployee(@RequestBody Employee e,@PathVariable("id") Integer id) {
                             return empService.updateEmployeeById(e, id);
              }
             
              @DeleteMapping("/deleteEmployee/{id}")
              public String deleteEmployeeById(@PathVariable("id") Integer id){
                             empService.deleteEmployeeById(id);
                             return "Deleted Successfully";
              }

}
````
7. Start the appl, and insert data and check the endpoints

8. Create the tool to enhance how AI Models interact with external tool
````
@Component
public class AITools {

              @Autowired
              EmployeeService empService;
 
              @Tool(description = "Retrieves information about an existing Employee, such as the employee name, age, address and salary and Employee class")
              public Employee getEmployeeDetail(int id) {
                             return empService.getEmployeeById(id);
              }
             
              @Tool(description = "Retrieves information of all Employee, such as the employee name, age, address and salary and Employee class")
              public List<Employee> getAllEmployee() {
                             return empService.getAllEmployees();
              }
             
              @Tool(description = "Update information of an employee such as the employee name, age, address and salary and Employee class")
              public Employee updateEmployee(@RequestBody Employee emp, int id) {
                             return empService.updateEmployeeById(emp, id);
              }
             
              @Tool(description = "Retreives the id from the text and delete the employee record from the database")
              public void deleteEmployee(int id) {
                             empService.deleteEmployeeById(id);
              }
}
````
9. Create AIService to call the tools
````
@Service
public class AIService {

              private final ChatClient chatClient;
              private final AITools tools;
             
              public AIService(ChatClient.Builder chatClientBuilder, AITools tools) {
                             this.chatClient=chatClientBuilder.build();
                             this.tools=tools;
              }
 
              public String getAIResponse(String prompt, String system) {
                             String response=chatClient.prompt()
                                                                            .user(prompt)
                                                                            .tools(tools)
                                                                            .system(system)
                                                                            .call().content();
                             return response;
              }
}
````
10. Create controller to call AIService
````
@RestController
public class AIAgentController {

              @Autowired
              private AIService aiService;
 
              public String getMessage(@RequestParam String prompt,@RequestParam String system) {
                             return aiService.getAIResponse(prompt, system);
              }
}
````
11. Start the appl

http://localhost:1000/message?prompt=Share me the list of all employees&system=Act as a project assistance

http://localhost:1000/message?prompt=Share me the details of employee whose id is 2&system=Act as a project assistance


## MCP(Model Context Protocol)
- When we build spring boot appl and want to add AI capabilities, we need to use Spring AI. Spring AI helps springboot appl to interact with LLM. But we also want our appl to interact with external systems like db or data tools or production ready appl
- But ur appl cannot directly interact with external system, we can handle this communication using tool calling. But the problem is they are limited and not standardized
- MCP which defines a set of rules and regulations, so there will be common standard to interact with external system
- MCP is an open source protocol that allows AI powered appl to interact with external systems

https://github.com/modelcontextprotocol/servers

#### What MCP enables?
1. AI agents can use external system
2. They can integrate with service like Google calendar

#### Components
1. MCP Host
2. MCP Client
3. MCP Server

#### 2 layers
1. Data layer
2. Transport layer
   a. stdio transport - If u want communication between local processes then we should use stdio transport (ie) our mcp server and client are running on the same mc locally
   b. streamable transport - if ur client is local and  mcp server is remote then we use stremable http transport

### Steps

1. Create springboot appl with web, azure ai, mcp client dependency

2. Configure api key in application.properties

3. Create controller prg
````
@RestController
public class AIController {

              private ChatClient chatClient;
             
              public AIController(ChatClient.Builder chatClient) {
                             this.chatClient=chatClient.defaultAdvisors(new SimpleLoggerAdvisor()).build();
              }
 
              @PostMapping("/chat")
              public ResponseEntity<String> getAIResponse(@RequestParam String query) {
                             String str=chatClient.prompt(query).call().content();
                             return ResponseEntity.ok(str);
              }
}
````
4. Start the appl, run  http://localhost:1000/chat?query=create a file with name Test.java and write main method with Helloworld

It just display the response

5. Now we implement MCP host called Filesystem

#### Filesystem MCP Server
Node.js server implementing Model Context Protocol (MCP) for filesystem operations.

Features
Read/write files
Create/list/delete directories
Move files/directories
Search files
Get file metadata
Dynamic directory access control via Roots

- Now we configure FileSystem MCP server in application.properties

spring.ai.mcp.client.stdio.servers-configuration=classpath:servers.json

- Now we create servers.json which contain MCP server info
````
{
"mcpServers": {
"filesystem": {
"command":"C:\\Program Files\\nodejs\\npx.cmd",
"args": [
"-y",
"@modelcontextprotocol/server-filesystem",
"C:\\Spring\\mcp",
"C:\\Spring\\mcp_output"
]
}
}
}
````
when u start the appl, the appl reads application.properties and read servers.json where it will setup filesystem mcp server automatically

6. Start the appl

7. We want to use MCP client through ChatClient for that we use ToolCallbackProvider interface which represents tool callback instances for tools

````
public AIController(ChatClient.Builder chatClient, ToolCallbackProvider toolCallBack) {
this.chatClient=chatClient.defaultAdvisors(new SimpleLoggerAdvisor())
.defaultToolCallbacks(toolCallBack)
.build();
}
````

1. user enters a request "create a file"
2. LLM understand the intent of the request
3. The MCP Client looks at the list of tool exposed by MCP server, choose the correct tool based on the user query
4. A tool request is created
5. The request is sent to the MCP server using MCP protocol
6. MCP server execute the operation
7. The response is sent back to the client and shown to user

8. Start the appl

http://localhost:1000/chat?query=Explain SpringAI concept with example, write them in a md file and save the file in C:\Spring\mcp_output\spring-ai.md


### Convert REST API appl to MCP server
1. Create springboot appl with web, lombok dependency

2. Configure api keys in application.properties

3. Create model class
````
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
private Long id;
private String name;
private String email;
}
````
4. Create controller
````
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

              @Autowired
              CustomerService service;
 
              @GetMapping("/{id}")
              public Customer getCustomerById(@PathVariable Long id) {
                             return service.getCustomerById(id);
              }
             
              @PostMapping
              public Customer createCustomer(@RequestBody Customer customer) {
                             return service.createCustomer(customer);
              }
}
````
5. Create service
````
@Service
public class CustomerService {

              public Customer getCustomerById(Long id) {
                             return new Customer(id,"Ram",ram@gmail.com);
              }
 
              public Customer createCustomer(Customer customer) {
                             return customer;
              }
}
````
6. Transform REST API to MCP tool
- Add MCP server dependency

- Now we transform traditional rest endpoint into MCP tool using @McpTool

````
@Component
public class CustomerMCPTool {

              @Autowired
              CustomerService service;
 
              @McpTool(name="get_customer_by_id", description = "Fetch customer details using customer id")
              public Customer getCustomerById(Long id) {
                             return service.getCustomerById(id);
              }
             
              @McpTool(name="create_customer",description="Create a new customer")
              public Customer createCustomer(Customer customer) {
                             return service.createCustomer(customer);
              }
}
````
7. Start the appl, run  http://localhost:2000/api/customers/1

8. MCP server is  http://localhost:2000/sse and communication happens over HTTP using streamed response

Create .vscode folder inside project - create mcp.json file
````
{
"servers":{
"customer-mcp": {
"url": http://localhost:2000/sse
}
}
}
````
- Start the server

- Now in copilot, we can fetch customer details based on id 1


9. MCP Interceptor - acts like a middleman. It sits between client request and tool execution allowing you to modify, validate or log the request and response
````
C:\Spring\SpringAI-MCP2>npx @modelcontextprotocol/inspector node build/index.js and run  http://localhost:6274/

URI:  http://localhost:2000/sse - click connect
````
Click Tools - list tools


### Wallet Payment API as MCP server

1. Create AI-MCP with web, lombok, mcp server dependency. Replace spring-ai-starter-mcp-server to spring-ai-starter-mcp-server-webmvc since we are using SSE
````
<dependency>
                                           <groupId>org.springframework.ai</groupId>
                                           <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
                             </dependency>
````
2. Create model class
````
public record Wallet(
Long walletId,
String customerName,
String currency,
double balance
) {}

public record PaymentRequest(
Long walletId,
double amount,
String currency,
String merchant
) {}

public record PaymentResponse(
String status,
String message,
double remainingBalance
) {}
````
3. Create Controller prg
````
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
 
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }
 
    @GetMapping("/{walletId}")
    public Wallet getWallet(@PathVariable Long walletId) {
        return walletService.getWallet(walletId);
    }
 
    @PostMapping("/pay")
    public PaymentResponse pay(@RequestBody PaymentRequest request) {
        return walletService.processPayment(request);
    }
}
````
4. Create service class
````
@Service
public class WalletService {

    private static final Map<Long, Wallet> WALLET_DB = new HashMap<>();
 
    static {
        WALLET_DB.put(1L, new Wallet(1L, "Roopa", "INR", 5000));
    }
 
    public Wallet getWallet(Long walletId) {
        return WALLET_DB.get(walletId);
    }
 
    public PaymentResponse processPayment(PaymentRequest request) {
 
        Wallet wallet = WALLET_DB.get(request.walletId());
 
        if (wallet == null) {
            return new PaymentResponse("FAILED", "Wallet not found", 0);
        }
 
        if (!wallet.currency().equalsIgnoreCase(request.currency())) {
            return new PaymentResponse("FAILED", "Invalid currency", wallet.balance());
        }
 
        if (wallet.balance() < request.amount()) {
            return new PaymentResponse("FAILED", "Insufficient balance", wallet.balance());
        }
 
        double updatedBalance = wallet.balance() - request.amount();
 
        WALLET_DB.put(
            wallet.walletId(),
            new Wallet(wallet.walletId(), wallet.customerName(), wallet.currency(), updatedBalance)
        );
 
        return new PaymentResponse(
            "SUCCESS",
            "Payment to " + request.merchant() + " completed",
            updatedBalance
        );
    }
}
````
5. Transform REST API to MCP server
- Add MCP server dependency
````
<dependency>
                                           <groupId>org.springframework.ai</groupId>
                                           <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
                             </dependency>
````
- Convert wallet api to mcp tool using @McpTool
````
@Component
public class WalletMcpTools {

              @Autowired
              WalletService service;
 
              @McpTool(name="get_wallet_details",description = "Fetch wallet details using wallet id")
              public Wallet getWallet(Long walletId) {
                             return service.getWallet(walletId);
              }
             
              @McpTool(name="initiate_wallet_payment",description = "Initiate a wallet payment after validating balance and currency")
              public PaymentResponse pay(PaymentRequest request) {
                             return service.processPayment(request);
              }
}
````
6. Enable mcp server in application.yml
````
spring:
ai:
mcp:
server:
enabled: true
name: wallet-mcp-server
version: 1.0.0
````
7. Start the appl
````
{
"servers": {
"wallet-mcp-server" : {
"url": http://localhost:1000/sse
}
}
}
````

### MCP Server and MCP client with Spring AI using SSE(Server Side Events) model

1. Create AI-MCPClient project web, azure ai, mcp client dependency, replace spring-ai-starter-mcp-client to spring-ai-starter-mcp-client-webmvc since we are using SSE

2. Configure api key in application.properties
````
spring.ai.mcp.client.sse.connections.my-mcp-server.url=http://localhost:3001
spring.ai.mcp.client.toolcallback.enabled=true
````
3. Create controller prg
````
@RestController
public class ChatController {

              private final ChatClient chatClient;
             
              public ChatController(ChatClient.Builder chatClient, ToolCallbackProvider tools) {
                             this.chatClient=chatClient.defaultSystem("Please prioritize context information for answering queries. Give short, concise and to the point answer")
                                                                            .defaultToolCallbacks(tools)
                                                                            .build();
              }
 
              @GetMapping("/chat")
              public String chat(@RequestParam String query) {
                             PromptTemplate pt=new PromptTemplate(query);
                             Prompt p=pt.create();
                             ChatClient.CallResponseSpec res=chatClient.prompt(p).call();
                             return res.content();
              }
}
````
1. Create AI-MCPServer project with mcp server dependency, replace spring-ai-starter-mcp-server to spring-ai-starter-mcp-server-webmvc since we are using SSE
````
<dependency>
                                           <groupId>org.springframework.ai</groupId>
                                           <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
                             </dependency>
````

2. Configure mcp server info in application.properties
````
server.port=3000

spring.ai.mcp.server.name=my-mcp-server
spring.ai.mcp.version=1.0.0

logging.level.org.springframework.ai=DEBUG
````
3. Create a tool
````
@Service
public class StockService {

              @Tool(description ="Get stock price for company")
              public String getStockPrice(String companyName) {
                             if(companyName.equalsIgnoreCase("hcl"))
                    return "The stock price of HCL is Rs 500. Valuations are attractive now";
                             if(companyName.equalsIgnoreCase("TCS"))
                                           return "The stock price of HCL is Rs 1000. Valuations are expensive";
                             else
                                           return "I dont have the information about the company";
              }
}
````
4. To make MCP to understand the tools we need to use ToolCallbackProvider intf
````
@SpringBootApplication
public class AiMcpServerApplication {

              public static void main(String[] args) {
                             SpringApplication.run(AiMcpServerApplication.class, args);
              }
 
              @Bean
              public ToolCallbackProvider stockTools(StockService service) {
                             return MethodToolCallbackProvider.builder().toolObjects(service).build();
              }
}
````
5. Start MCP server appl

6. Start MCP client appl

Run  http://localhost:3000/chat?query=what is the stock price of HCL, should I buy it now?



---

## 🌟 Developer/Contributor
Name: Rohit Shamrao Muneshwar  
Email: rohit.muneshwar1406@gmail.com  
LinkedIn Profile: [Click Here](https://www.linkedin.com/in/rohit-muneshwar-a9079258/)  
Other Github repositories: [Click Here](https://github.com/rohit1406?tab=repositories)

---