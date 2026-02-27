package me.ai.training.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rohit Muneshwar
 * @created on 2/27/2026
 *
 *
 */
@Service
@Slf4j
public class AudioService {
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final OpenAiAudioApi openAiAudioApi;
    public AudioService(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel,
                        OpenAiAudioApi openAiAudioApi){
        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
        this.openAiAudioApi = openAiAudioApi;
    }

    /**
     * Converts audio to text
     * @param filename
     * @return
     */
    public String getAnswerWithManualConfig(String filename) {
        // load audio file from classpath
        ClassPathResource audioResource = new ClassPathResource("audio/"+filename);
        // convert audio to raw byte[]
        try(InputStream in = audioResource.getInputStream()){
            byte[] bytes = in.readAllBytes();
            return openAiAudioApi.createTranscription(OpenAiAudioApi.TranscriptionRequest.builder()
                            .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.VERBOSE_JSON)
                            .file(bytes)
                            .fileName(filename)
                            .build(), OpenAiAudioApi.StructuredResponse.class).getBody().text();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts audio to text
     * @param filename
     * @return
     */
    public String getAnswer(String filename) {
        // load audio file from classpath
        ClassPathResource audioResource = new ClassPathResource("audio/"+filename);
        AudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.VERBOSE_JSON)
                .build();
        AudioTranscriptionPrompt audioTranscriptionPrompt = new AudioTranscriptionPrompt(audioResource, options);
        return openAiAudioTranscriptionModel.call(audioTranscriptionPrompt)
                .getResult().getOutput();
    }
}
