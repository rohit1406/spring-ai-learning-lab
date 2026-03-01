package com.edu.spring.ai.service;

import com.edu.spring.ai.dto.Author;
import com.edu.spring.ai.enums.PromptingTypes;
import com.edu.spring.ai.util.PromptMessages;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rohit Muneshwar
 * AiCafeChatModelService: uses Spring AI's OpenAI ChatModel to interact with OpenAI
 */
@Service
public class AiCafeChatModelService {
    // communicates with LLM model: using ChatClient or ChatModel
    private final OpenAiChatModel chatModel;
    private final ChatOptions chatOptions;

    // .st extention stands for String Template
    // Parameterized prompt with placeholders, passing prompt at runtime
    @Value("classpath:/prompts/popular.st")
    private Resource promptResource;

    @Value("classpath:/prompts/play.st")
    private Resource sportsPromptResource;

    @Value("classpath:/docs/sports.txt")
    private Resource sportsContextResource;

    public AiCafeChatModelService(OpenAiChatModel chatModel, ChatOptions chatOptions){
        this.chatOptions = chatOptions;
        this.chatModel = chatModel;
    }

    /**
     * Asks AiCafe to create a joke based on information provided in message using OpenAI ChatModel.
     *
     * This method demonstrates the usage of various type of messages as below:
     * System message: define the purpose, behavior, tone or role of AI model
     * Assistant message: represent some previous response as an assistance to the AI
     * User message: represent actual input/question/instruction to AI
     *
     * Order of execution:
     * System(behavior/roles) -> Assistant(previous context) -> User messages(your query)
     *
     * @param message
     * @return
     */
    public String askMeAnything(String message){
        IO.println("Using ChatModel for getting response from AiCafe");
        //return callModelWithText(message);
        //return callModelWithPrompt(message);
        return callModelWithPromptMessages(message);
    }

    private String callModelWithPromptMessages(String message){
        // define the purpose, behavior, tone or role of AI model
        var systemMsg = new SystemMessage("You are a senior Java and Springboot expert. Be precise and pragmatic");
        // represent some previous response as an assistance to the AI
        var assistantMsg = new AssistantMessage("Dependency Injection lets Spring manage object wiring via the container.");
        // represent actual input/question/instruction to AI
        var userMsg = new UserMessage(message);
        /**
         * Order of execution:
         * System(behavior/roles) -> Assistant(previous context) -> User messages(your query)
         */
        var prompt = new Prompt(List.of(systemMsg, assistantMsg, userMsg), chatOptions);
        return chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();
    }

    private String callModelWithPrompt(String message){
        return chatModel.call(new Prompt(message))
                .getResult()
                .getOutput()
                .getText();
    }

    private String callModelWithText(String message){
        return chatModel.call(message);
    }

    public String getPopularYoutubers(String genre) {
        //Prompt prompt = getPopularYoutubersPromptFromHardcodedMessageTemplate(genre);
        Prompt prompt = getPopularYoutubersPromptFromStringTemplate(genre);
        return chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();
    }

    private Prompt getPopularYoutubersPromptFromStringTemplate(String genre) {
        IO.println("fetching popular youtubers with genre "+ genre+" using string template");
        PromptTemplate promptTemplate = new PromptTemplate(promptResource);
        return promptTemplate.create(Map.of("genre", genre), chatOptions);
    }

    private Prompt getPopularYoutubersPromptFromHardcodedMessageTemplate(String genre) {
        IO.println("fetching popular youtubers with genre "+ genre);
        String message = """
                List most popular Youtubers in {genre} along with their current subscribers count.
                If you don't know the answer, just say 'I don't know'.
                """;
        // Parameterized prompt with placeholders, passing prompt at runtime
        PromptTemplate promptTemplate = new PromptTemplate(message);
        return promptTemplate.create(Map.of("genre", genre), chatOptions);
    }

    /**
     * Get the destinations for the country from LLM.
     *
     * This implementation demonstrates the usage of PromptTemplates.
     * It also uses OutputConverter to convert the response from LLM to List using ListOutputConvertor.
     *
     * @param country
     * @return
     */
    public List<String> getCountryDestinations(String country) {
        var message = """
                Please give me a list of destinations for the country {country}.
                If you dont know the answer just say "I dont know".
                {format}
                """;
        ListOutputConverter outputConverter = new ListOutputConverter(new DefaultConversionService());
        PromptTemplate promptTemplate = new PromptTemplate(message);
        Prompt prompt = promptTemplate.create(
                            Map.of("country", country,"format", outputConverter.getFormat()),
                            chatOptions);
        ChatResponse chatResponse = chatModel.call(prompt);
        return outputConverter.convert(chatResponse.getResult().getOutput().getText());
    }

    /**
     * Get the author details from LLM.
     *
     * This implementation demonstrates the usage of PromptTemplates.
     * It also uses OutputConverter to convert the response from LLM to List using MapOutputConvertor.
     *
     * @param author
     * @return
     */
    public Map<String, Object> getAuthors(String author) {
        var message = """
                Generate list of links for the author {author}.
                Include the author name as the key and any social network links as the value. 
                If you dont know the answer just say "I dont know".
                {format}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(message);
        MapOutputConverter outputConverter = new MapOutputConverter();
        Prompt prompt = promptTemplate.create(
                Map.of("author", author, "format", outputConverter.getFormat()),
                chatOptions
        );
        ChatResponse chatResponse = chatModel.call(prompt);
        return outputConverter.convert(chatResponse.getResult().getOutput().getText());
    }

    /**
     * Get the author with books written by him from LLM.
     *
     * This implementation demonstrates the usage of PromptTemplates.
     * It also uses OutputConverter to convert the response from LLM to List using BeanOutputConvertor.
     *
     * @param author
     * @return
     */
    public Author getBooksAuthor(String author) {
        var message = """
                Generate a list of books written by the author {author}. 
                If you arent positive that the book belong to this author please don't include it.
                {format}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(message);
        BeanOutputConverter<Author> outputConverter = new BeanOutputConverter<>(Author.class);
        Prompt prompt = promptTemplate.create(
                Map.of("author", author, "format", outputConverter.getFormat()),
                chatOptions
        );
        ChatResponse chatResponse = chatModel.call(prompt);
        return outputConverter.convert(chatResponse.getResult().getOutput().getText());
    }

    /**
     * Explain query based on prompt messages which are categorized as
     * zero shot, few shot, chain of thoughts prompting.
     * @param message
     * @param promptingType
     * @return
     */
    public String explainQuery(String message, PromptingTypes promptingType) {
        var promptMessage = switch(promptingType){
            case ZERO_SHOT -> PromptMessages.EXPLAIN_KAFKA_ZERO_SHOT_PROMPTING;
            case FEW_SHOT -> PromptMessages.ASK_QUESTION_WITH_FEW_SHOT_PROMPTING;
            case CHAIN_OF_THOUGHT -> PromptMessages.ANALYSE_CAUSE_WITH_CHAIN_OF_THOUGHT_PROMPTING;
        };

        var msg = new UserMessage(Optional.ofNullable(message).orElse(promptMessage));
        var prompt = new Prompt(msg, chatOptions);
        return chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();
    }

    public String getSportsInformation(String message, boolean stuffit) {
        PromptTemplate promptTemplate = new PromptTemplate(sportsPromptResource);
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("question", message);

        if(stuffit){
            placeholders.put("context", sportsContextResource);
        }else{
            placeholders.put("context", "");
        }
        Prompt prompt = promptTemplate.create(placeholders, chatOptions);
        return chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();
    }
}
