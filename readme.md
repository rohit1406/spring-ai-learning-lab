# About this Project
The following Spring AI concepts are implemented in this repository.
1. Integration of Spring AI into Spring Boot application
2. Exposing REST APIs for taking user's input as a prompt, make a call to OpenAI for processing the request and get the output from OpenAI to send it back to the User.
3. Use of UserMessage, AssistantMessage and SystemMessage while generating prompts
4. Use of String Templates (.st) for dynamic prompting
5. Output Convertors to convert the output received from OpenAI and display it to the user. In this demonstration ListOutputConvertor, MapOutputConvertor and BeanOutputConvertors are used.
6. Types of Prompting are demonstrated as well: Zero Shot Prompting, Few Shot Prompting and Chain of Thought (CoT) prompting
7. Data Augmentation Techniques such as Stuffing, Tool Calling/Function calling (used in lower versions of spring ai - 1.0.6) are utilized while retrieving data from LLM.
8. Use of ChatMemory: to remember the conversation history and form the context with it to answer the upcoming questions from the user.
9. Use of Advisors: to intercept the request
10. RAG using manual search request creation, QuestionAnswerAdvisor, RetrievalAugmentationAdvisor

# Assignments Implemented
1. Create a SpringBoot service using SpringAI that takes dynamic user input to generate a customized travel itinerary. Participants must demonstrate how to use PromptTemplate to manage dynamic variables and a System Message to define the Persona.
2. You are building a Personal Banking Advisor using SpringAI. The advisor must be able to retrieve the user's current balance from a database (imagine we have a database with all customer details) using tool calling mechanism.
3. You are developing an AI Advisor for a corporate HR department. The advisor must meet 3 requirements:
    - It must remember the users previous questions to handle followup queries
    - All raw prompts and model responses must be logged for internal review
    - It must block any questions related to internal salary data or non-work topics



### Prerequisites to Run the application
To run the application you need to set environment variables 
- **API_KEY_OPENAI** - subscription Key from [OpenAI](https://api.openai.com/) api provider.
- **WEATHER_API_KEY** - api key from the [weather api provider](http://api.weatherapi.com/v1).
 
- [OpenSearch installation for Vector database](https://docs.opensearch.org/latest/install-and-configure/install-opensearch/windows/)

### Endpoints Exposed
Start the server locally and refer to [Swagger API](http://localhost:8080/swagger-ui/index.html) for the endpoint details and technical documentation.

### Some Useful links
[Configure H2 in-memory database in Spring Boot](https://medium.com/@madhurajayashanka/h2-database-on-spring-boot-a-beginners-guide-8cc49c27e83c)

[How Spring Boot initializes data on successful start up](https://medium.com/@AlexanderObregon/running-sql-scripts-automatically-at-spring-boot-startup-62ae2dd03087)

[Spring Data JPA](https://medium.com/@bshiramagond/jpa-with-spring-boot-a-comprehensive-guide-with-examples-e07da6f3d385)

[Configure SQLite](https://medium.com/@AlexanderObregon/using-spring-boot-with-sqlite-for-lightweight-apps-6c7624a0f438)


