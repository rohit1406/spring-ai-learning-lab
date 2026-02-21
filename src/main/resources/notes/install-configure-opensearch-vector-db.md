# OpenSearch Vector Database

## Installation on Windows
Follow below link for the installation:
https://docs.opensearch.org/latest/install-and-configure/install-opensearch/windows/

- After zip extraction, you need to set env variables. Refer below names for that:
 ```` 
  OPENSEARCH_HOME=.\Downloads\opensearch-3.1.0
  OPENSEARCH_INITIAL_ADMIN_PASSWORD=H@ppyNemYear2026
  OPENSEARCH_JAVA_HOME=.\Downloads\opensearch-3.1.0\jdk
````
- add below line to OPENSEARCH_HOME\config\opensearch.yml to disable the security plugin.
````
plugins.security.disabled: true
````
- Then execute the installation bat file
````
cd to OPENSEARCH_HOME$ .\opensearch-windows-install.bat

# Upon successful start up, test the service using any of the below urls
# From the browser
http://localhost:9200/
http://localhost:9200/spring-ai-document-index/_doc
http://localhost:9200/_cat

# From cmd
curl.exe -X GET http://localhost:9200
curl.exe -X GET http://localhost:9200/_cat/plugins?v -u "admin:H@ppyNemYear2026"

# OR if security plugin not disabled
curl.exe -X GET https://localhost:9200/_cat/plugins?v -u "admin:H@ppyNemYear2026" --insecure


````

### Spring Boot Configurations
- Update pom file
````
<dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-opensearch</artifactId>
        </dependency>
<dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-advisors-vector-store</artifactId>
        </dependency>
````

- Configure api key and db info in application.properties
````
spring:
      #controls when to initialize schema - embedded, always, never
  #Spring AI will automatically create table spring_ai_chat_memory with columns
  ai:
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always
    vectorstore:
      # Installation: https://docs.opensearch.org/latest/install-and-configure/install-opensearch/windows/
      # vector_store table is auto created
      opensearch:
        uris: http://localhost:9200
        username: admin
        password: H@ppyNemYear2026
        index-name: spring-ai-document-index
        initialize-schema: true
        similarity-function: cosinesimil
      #pgvector:
       # initialize-schema: true


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