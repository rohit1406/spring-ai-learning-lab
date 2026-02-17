package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.dto.Author;
import me.ai.training.service.AiCafeChatModelService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Rohit Muneshwar
 *
 * Controller class to consumer AI services provided by OpenAI platform.
 * This class demonstrates the usage of interacting with AiCafe via Spring AI's ChatModel.
 */
@Tag(name = "Lab2: Prompt Templates and Output Converters", description = """
        This Lab demonstrates the usage of String Templates for dynamic prompting and Output Converters (List, Map, Bean).
        """)
@RestController
public class AIController {
    private final AiCafeChatModelService aiCafeChatModelService;

    public AIController(AiCafeChatModelService aiCafeChatModelService){
        this.aiCafeChatModelService = aiCafeChatModelService;
    }

    /**
     * Get popular youtubers list as per genre.
     *
     * This implementation demonstrates the usage of Prompt Templates by using placeholders in the String Template prompt.
     * String template prompts can be provided in .st file or directly as an input string to PromptTemplate class.
     *
     * @param genre
     * @return
     */
    @Operation(summary = "Get popular youtubers list as per genre.", description = """
            This implementation demonstrates the usage of Prompt Templates by using placeholders in the String Template prompt.
            String template prompts can be provided in .st file or directly as an input string to PromptTemplate class.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                    It returns response to the asked query.
                    This implementation demonstrates the usage of Prompt Templates by using placeholders in the String Template prompt.
                    String template prompts can be provided in .st file or directly as an input string to PromptTemplate class.
                    """,
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/popular")
    public String findPopularYoutubers(@RequestParam(value = "genre", defaultValue = "tech") String genre){
        return aiCafeChatModelService.getPopularYoutubers(genre);
    }

    /**
     * Get Destinations for the country.
     *
     * This implementation demonstrates the usage of ListOutputConverter to structure the response from LLM.
     *
     * @param country
     * @return
     */
    @Operation(summary = "Get Destinations for the country", description = """
            This implementation demonstrates the usage of ListOutputConverter to structure the response from LLM.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping(value = "/dest", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getDestinations(@RequestParam(value = "country", defaultValue = "India") String country){
        return aiCafeChatModelService.getCountryDestinations(country);
    }

    /**
     * Get authors with link to their social media platform.
     *
     * This implementation demonstrates the usage of MapOutputConverter to structure the response from LLM.
     *
     * @param author
     * @return
     */
    @Operation(summary = "Get authors with link to their social media platform", description = """
            This implementation demonstrates the usage of MapOutputConverter to structure the response from LLM.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping(value = "/authors/{author}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getAuthors(@PathVariable(value = "author") String author){
        return aiCafeChatModelService.getAuthors(author);
    }

    /**
     * Get Books written by the author.
     *
     * This implementation demonstrates the usage of BeanOutputConverter to structure the response from LLM.
     *
     * @param author
     * @return
     */
    @Operation(summary = "Get Books written by the author", description = """
            This implementation demonstrates the usage of BeanOutputConverter to structure the response from LLM.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = Author.class)))
    })
    @GetMapping(value = "/byauthor", produces = MediaType.APPLICATION_JSON_VALUE)
    public Author getBooksByAuthor(@RequestParam(value = "author", defaultValue = "Chetan Bhagat") String author){
        return aiCafeChatModelService.getBooksAuthor(author);
    }
}
