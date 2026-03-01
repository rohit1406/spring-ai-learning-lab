package com.edu.spring.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.edu.spring.ai.service.TravelItineraryService;
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
@Tag(name = "Practice: Assignments")
@RestController
@RequestMapping("/travel-itinerary")
public class TravelItineraryController {
    private final TravelItineraryService travelItineraryService;
    public TravelItineraryController(TravelItineraryService travelItineraryService){
        this.travelItineraryService = travelItineraryService;
    }

    @Operation(summary = "Get Travel Itinerary", description = """
            Create a SpringBoot service using SpringAI that takes dynamic user input
            to generate a customized travel itinerary. Participants must demonstrate how to use
            a PromptTemplate to manage dynamic variables and a System message to define the persona.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "on successful response generation",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/")
    public String getTravelItinerary(@RequestParam(name = "source", defaultValue = "Pune") String source,
                                                       @RequestParam(name = "destination", defaultValue = "Mumbai") String destination){
        return travelItineraryService.createTravelItinerary(source, destination);
    }
}
