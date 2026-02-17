package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.service.BankingAdvisorService;
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
@RequestMapping("/banking-advisor")
public class PersonalBankingAdvisorController {
    private final BankingAdvisorService bankingAdvisorService;
    public PersonalBankingAdvisorController(BankingAdvisorService bankingAdvisorService){
        this.bankingAdvisorService = bankingAdvisorService;
    }

    @Operation(summary = "Personal Banking Advisor", description = """
            You are building a Personal Banking Advisor using SpringAI.
            The advisor must be able to retrieve the user's current balance from
            a database (imagine we have a database with all customer details)
            using tool calling mechanism.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "on successful response generation",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/")
    public String getBankAccountDetails(@RequestParam(name = "userId") String userId){
        return bankingAdvisorService.getBankAccountDetails(userId);
    }
}
