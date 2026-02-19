package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.service.DataFeedService;
import me.ai.training.service.StreamChatService;
import me.ai.training.util.DataUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
    private final DataFeedService dataFeedService;
    public RAGController(DataFeedService dataFeedService){
        this.dataFeedService = dataFeedService;
    }

    @Operation(summary = "save data to Vector Store", description = """
            Use this method to ONLY save the test data to vector store
            """)
    @GetMapping("/save-test-data")
    public ResponseEntity<String> saveTestData(){
        try {
            dataFeedService.saveData(DataUtil.getData());
            return ResponseEntity.ok("Data saved successfully");
        }catch(Exception ex){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Data could not be saved: "+ex.getMessage());
        }
    }

}
