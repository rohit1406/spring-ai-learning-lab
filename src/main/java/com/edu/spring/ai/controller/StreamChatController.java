package com.edu.spring.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.edu.spring.ai.service.StreamChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author Rohit Muneshwar
 * @created on 2/18/2026
 *
 *
 */
@Tag(name = "Lab6: Streaming Response")
@RestController
@RequestMapping("/chat")
public class StreamChatController {
    private final StreamChatService streamChatService;
    public StreamChatController(StreamChatService streamChatService){
        this.streamChatService = streamChatService;
    }

    @Operation(summary = "stream the response from LLM", description = """
            The response is asynchronous and non-blocking when streaming.
            User sends the request - we build the prompt and send it to the model - Instead of waiting for the full response, the model starts sending small chunk of data as they are generated.
            """)
    @GetMapping("/stream")
    public ResponseEntity<Flux<String>> streamChat(@RequestParam(name = "query", defaultValue = "Tell me the new features of Java.") String query){
        return ResponseEntity.ok(streamChatService.getResponse(query));
    }

    @Operation(summary = "remembers multiple users chat history", description = """
            The response is asynchronous and non-blocking when streaming.
            To make multiple user maintain their own chat history it uses user-id field in the header.
            Uses in-memory storage which vanishes after server restart.
            """)
    @GetMapping("/user-aware")
    public ResponseEntity<Flux<String>> userAwareChat(@RequestParam(name = "query", defaultValue = "Who are you?") String query,
                                                   @RequestHeader(name = "user-id") String userId){
        return ResponseEntity.ok(streamChatService.getMessage(userId, query));
    }

    @Operation(summary = "remembers multiple users chat history", description = """
            The response is asynchronous and non-blocking when streaming.
            To make multiple user maintain their own chat history it uses user-id field in the header.
            Uses Persistent storage as a chat message storage.
            """)
    @GetMapping("/persistent/user-aware")
    public ResponseEntity<Flux<String>> getPersistenceAwareMessage(@RequestParam(name = "query", defaultValue = "Who are you?") String query,
                                                      @RequestHeader(name = "user-id") String userId){
        return ResponseEntity.ok(streamChatService.getPersistenceAwareMessage(userId, query));
    }

}
