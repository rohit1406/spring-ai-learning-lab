package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.service.AiCafeChatModelService;
import me.ai.training.service.ChatService;
import me.ai.training.service.WeatherAIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rohit Muneshwar
 *
 * Controller class to consumer AI services provided by OpenAI platform.
 */
@Tag(name = "Lab4: Data Augmentation techniques", description = """
        This Lab demonstrates the usage of a Data Augmentation techniques when retrieving data from LLM.
        The techniques such as
         - Stuffing 
         - Tool Calling/Function Calling (Used in lower version of Spring AI - 1.0.6)
        """)
@RestController
public class DataAugmentationController {
    private final AiCafeChatModelService aiCafeChatModelService;
    private final WeatherAIService weatherAIService;
    private final ChatService chatService;

    public DataAugmentationController(AiCafeChatModelService aiCafeChatModelService,
                                      WeatherAIService weatherAIService,
                                      ChatService chatService){
        this.aiCafeChatModelService = aiCafeChatModelService;
        this.weatherAIService = weatherAIService;
        this.chatService = chatService;
    }

    @Operation(summary = "Stuffing", description = """
            This implementation demonstrates the usage of stuffing in getting data from LLM.
            In this, we add our own data to the context and get the results based on that data.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/sports")
    public String explainQueryFewShots(@RequestParam(value = "message", defaultValue = "What sports are being included in the 2026 Olympics?") String message,
                                       @RequestParam(value = "stuffit", defaultValue = "false")boolean stuffit){
        return aiCafeChatModelService.getSportsInformation(message, stuffit);
    }

    @Operation(summary = "Tool Calling/Function Calling (Used in lower versions of Spring AI - 1.0.6", description = """
            This implementation demonstrates the usage of Function calling in getting data from LLM.
            It makes LLM decides when to call Java methods(functions) instead of just returning plain text.
            It can also be used with real application logic (DB calls, API's, calculations).
            e.g.
            - search data from the internet
            - search the web when "web access" option is enabled
            - get current date/time
            - provide live weather/stock market information
            - access internal database
           Whenever LLM receives the request, it realize that to answer the question, it needs
           to call the tool. It informs SpringAI to invoke the tool. Spring AI will execute the
           tool and return the result to model. The model will return final response.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/weather")
    public String getWeatherInformation(@RequestParam(value = "query", defaultValue = "What is current temperature in Berlin?") String query){
        return weatherAIService.getWeatherInformation(query);
    }

    @Operation(summary = "Tool Calling/Function Calling (Used in lower versions of Spring AI - 1.0.6", description = """
            This implementation demonstrates the usage of Tool calling/Function calling in getting data from LLM.
            It makes LLM decides when to call Java methods(functions) instead of just returning plain text.
            It can also be used with real time application logic (DB calls, API's, calculations).
            e.g.
            - search data from the internet
            - search the web when "web access" option is enabled
            - get current date/time
            - provide live weather/stock market information
            - access internal database
            With Tool calling we can expose such functions.
            Purposes: information retrieval/action taking processes such as setting an alarm or sending an email.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "It returns response to the asked query",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(value = "/info")
    public String getInformation(@RequestParam(value = "msg", defaultValue = "What is current time in Berlin?") String msg){
        return chatService.getChat(msg);
    }
}
