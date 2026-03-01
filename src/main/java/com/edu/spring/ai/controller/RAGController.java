package com.edu.spring.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.edu.spring.ai.service.AdvancedRagProcessingService;
import com.edu.spring.ai.service.FileProcessingRagService;
import com.edu.spring.ai.service.RAGService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * @author Rohit Muneshwar
 * @created on 2/18/2026
 *
 *
 */
@Tag(name = "Lab7: RAG", description = """
        Note: You can use /data-feed api to feed the sample test data to vector database.
        """)
@RestController
@RequestMapping("/rag")
public class RAGController {
    private final RAGService ragService;
    private final FileProcessingRagService fileProcessingRagService;
    private final AdvancedRagProcessingService advancedRagProcessingService;
    public RAGController(RAGService ragService, FileProcessingRagService fileProcessingRagService,
                         AdvancedRagProcessingService advancedRagProcessingService){
        this.ragService = ragService;
        this.fileProcessingRagService = fileProcessingRagService;
        this.advancedRagProcessingService = advancedRagProcessingService;
    }

    @Operation(summary = "Simple RAG flow: Get the answer to the asked query using data stored in vector table.", description = """
            Sample data is already stored in vector table using '/save-test-data'.
            This endpoint answers the query based on that data only using similarity search capability of a vector database. 
            If answer to the query is not found then it returns response like that. 
            In this example all similarity search and prompt building are done manually.
            - Build search request, writing system prompt
            """)
    @GetMapping("/answer")
    public String getAnswer(@RequestParam(name = "q", defaultValue = "What is Java?") String query){
        return ragService.getAnswer(query);
    }

    @Operation(summary = "RAG flow: Get the answer to the asked query using data stored in vector table using QuestionAnswerAdvisor", description = """
            Sample data is already stored in vector table using '/save-test-data'.
            This endpoint answers the query based on that data only using similarity search capability of a vector database. 
            If answer to the query is not found then it returns response like that. 
            In this example, QuestionAnswerAdvisor is used:
            It automatically runs similarity search on the vector db - retrieves relavant document - build the prompts - inject context - add instructions- send final prompt to LLM
            """)
    @GetMapping("/answer-query")
    public String getAnswerWithAdvisor(@RequestParam(name = "q", defaultValue = "What is Java?") String query){
        return ragService.getAnswerQAAdvisor(query);
    }

    @Operation(summary = "RAG flow: Get the answer to the asked query using data stored in vector table using RetrievalAugumentationAdvisor", description = """
            Sample data is already stored in vector table using '/save-test-data'.
            This endpoint answers the query based on that data only using similarity search capability of a vector database. 
            If answer to the query is not found then it returns response like that. 
            This endpoint uses Advanced RAG flow.
            
            The difference between Simple vs Advanced RAG flow is as below:
            Simple RAG flow - using manually, QuestionAnswerAdvisor
               - When user sends a query to our Spring boot appl, we do not send the query directly to LLM
            1. We fetch context from our db which is stored inside vector db
            2. we perform similarity search on vector db
            3. The top relavant document are retrieved
            4. we merge this context with user query
            5. we send along with prompt to LLM,
            6. LLM return the response
            
            
            Advanced RAG using RetrievalAugumentationAdvisor
                - It supports multiple phase RAG flow to implement RAG based appl
            
            1. Pre-retrieval
            2. Retrieval
            3. Post retreival
            4. Generation
            """)
    @GetMapping("/answer-coding-doubt")
    public String getAnswerWithAdvisor2(@RequestParam(name = "q", defaultValue = "What is Java?") String query){
        return ragService.getAnswerRAAdvisor(query);
    }

    @Operation(summary = "RAG flow: Get the answer to the asked query using data stored in vector table using QuestionAnswerAdvisor", description = """
            Sample data is already stored in vector table using '/save-employee-project-test-data'. If not present then trigger that endpoint.
            Possible values for document-id and tenant-id are: DOC1:ORG, DOC2:ORG, DOC3:ORG.
            
            This endpoint answers the query based on that data only using similarity search capability of a vector database. 
            If answer to the query is not found then it returns response like that. 
            In this example, QuestionAnswerAdvisor is used:
            It automatically runs similarity search on the vector db - retrieves relavant document - build the prompts - inject context - add instructions- send final prompt to LLM
            """)
    @GetMapping("/answer-from-document")
    public String getAnswerWithSpecificDocumentId(@RequestParam(name = "question", defaultValue = "Who works in department alpha?") String query,
                                                  @RequestParam(name="document-id") String documentId,
                                                  @RequestHeader(name="X-TENANT") String tenantId){
        return fileProcessingRagService.getAnswerFromDocument(query, documentId, tenantId);
    }

    @Operation(summary = "RAG flow (look through multiple files): Get the answer to the asked query from multiple linked files using data stored in vector table", description = """
            Sample data is already stored in vector table using '/save-employee-project-test-data'. If not present then trigger that endpoint.
            Possible values for document-id and tenant-id are: DOC1:ORG, DOC2:ORG, DOC3:ORG.
            
            It follows the references given in the document to perform further searches in the linked documents.
            
            This endpoint answers the query based on that data only using similarity search capability of a vector database. 
            If answer to the query is not found then it returns response like that. 
            In this example, QuestionAnswerAdvisor is used:
            It automatically runs similarity search on the vector db - retrieves relavant document - build the prompts - inject context - add instructions- send final prompt to LLM
            """)
    @GetMapping("/multi-hop-search")
    public Map<String, Object> getAnswerUsingMultihopSearches(@RequestParam(name = "question", defaultValue = "Who works in department alpha?") String query,
                                                              @RequestParam(name="start-document-id") String startDocumentId,
                                                              @RequestHeader(name="X-TENANT") String tenantId,
                                                              @RequestParam(defaultValue = "3") int maxHops,
                                                              @RequestParam(defaultValue = "5") int topK,
                                                              @RequestParam(defaultValue = "0.3") double threshold){
        return fileProcessingRagService.getAnswerWithMultihopSearch(query, startDocumentId, tenantId, maxHops, topK, threshold);
    }

    @Operation(summary = "Advanced RAG flow: Get the answer to the asked query using data stored in vector table using Advanced RAG flow", description = """
            Sample data is already stored in vector table using '/save-test-data'. If not present then trigger that endpoint.
            """)
    @GetMapping("/search")
    public String getAnswerUsingAdvancedRagFlow(@RequestParam(name = "question", defaultValue = "What is class in Java?") String query){
        return advancedRagProcessingService.getAnswer(query);
    }
}
