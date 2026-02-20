package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.service.RAGService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author Rohit Muneshwar
 * @created on 2/18/2026
 *
 *
 */
@Tag(name = "Lab7: RAG")
@RestController
@RequestMapping("/rag")
public class RAGController {
    private final RAGService ragService;
    public RAGController(RAGService ragService){
        this.ragService = ragService;
    }

    @Operation(summary = "save data to Vector Store", description = """
            Use this method to ONLY save the test data to vector store
            """)
    @GetMapping("/save-test-data")
    public ResponseEntity<String> saveTestData(){
        try {
            ragService.saveTestData();
            return ResponseEntity.ok("Data saved successfully");
        }catch(Exception ex){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Data could not be saved: "+ex.getMessage());
        }
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

    @Operation(summary = "Simple RAG flow: Get the answer to the asked query using data stored in vector table using QuestionAnswerAdvisor", description = """
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

    @Operation(summary = "Advanced RAG flow: Get the answer to the asked query using data stored in vector table using RetrievalAugumentationAdvisor", description = """
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
}
