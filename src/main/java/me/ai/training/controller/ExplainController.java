package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.enums.PromptingTypes;
import me.ai.training.service.AiCafeChatModelService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @author Rohit Muneshwar
 *
 * Controller class to consumer AI services provided by OpenAI platform.
 */
@Tag(name = "Lab3: Type of Prompting", description = """
        This Lab demonstrates the usage of different types of Prompting techniques.
        Zero shot prompting, Few shot prompting, Chain of Thoughts(CoT) prompting.
        """)
@RestController
public class ExplainController {
    private final AiCafeChatModelService aiCafeChatModelService;

    public ExplainController(AiCafeChatModelService aiCafeChatModelService){
        this.aiCafeChatModelService = aiCafeChatModelService;
    }

    @Operation(summary = "Zero shot prompting", description = """
            This implementation demonstrates the usage of Zero shot prompting.
            Here, you ask the model to do a task without giving any examples. The model relies
            only on the pretrained data.
            When to use?
             - task is simple or common
             - you want quick results
             - No strict output format required
            e.g. Explain REST Api in simple terms.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/explain1")
    public String explainQueryZeroShot(@RequestParam(value = "message", required = false) String message){
        return aiCafeChatModelService.explainQuery(message, PromptingTypes.ZERO_SHOT);
    }

    @Operation(summary = "Few shot prompting", description = """
            This implementation demonstrates the usage of Few shot prompting.
            Here, you provide few examples of input and output and then ask the model to do similar task.
            When to use?
             - consistent format
             - domain specific
            e.g.
            You are an AI that generated Java interview questions.
           Example:
           Topic: OOP
           Question: What is encapsulation in Java?

           Example:
           Topic: Collections
           Question: What is ArrayList in Java?

           Now generate a question for:
           Topic: Multithreading
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/explain2")
    public String explainQueryFewShots(@RequestParam(value = "message", required = false) String message){
        return aiCafeChatModelService.explainQuery(message, PromptingTypes.FEW_SHOT);
    }

    @Operation(summary = "Chain of Thoughts prompting", description = """
            This implementation demonstrates the usage of Chain of Thought (CoT) prompting.
            Here, You ask the model to show its reasoning steps, not just the final answer.
            When to use?
             - logical reasoning
             - debugging
             - Math/Decision making
            e.g. An application is slow during peak hours. Think step by step and identify possible causes.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/explain3")
    public String explainQueryChainOfThoughts(@RequestParam(value = "message", required = false) String message){
        return aiCafeChatModelService.explainQuery(message, PromptingTypes.CHAIN_OF_THOUGHT);
    }
}
