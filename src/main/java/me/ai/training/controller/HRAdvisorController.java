package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.service.HRAdvisorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rohit Muneshwar
 * @created on 2/16/2026
 *
 *
 */
@Tag(name = "Assignments")
@RestController
@RequestMapping("/hr-advisor")
public class HRAdvisorController {
    private final HRAdvisorService hrAdvisorService;
    public HRAdvisorController(HRAdvisorService hrAdvisorService){
        this.hrAdvisorService = hrAdvisorService;
    }

    @Operation(summary = "HR Advisor", description = """
            You are developing an AI Advisor for a corporate HR department. 
            The advisor must meet 3 requirements:
                - It must remember the users previous questions to handle followup queries
                - All raw prompts and model responses must be logged for internal review
                - It must block any questions related to internal salary data or non-work topics
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "on successful response generation",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/")
    public String chatWithHr(@RequestParam(name = "query", defaultValue = "What is your name?") String query){
        return hrAdvisorService.chatWithHr(query);
    }
}
