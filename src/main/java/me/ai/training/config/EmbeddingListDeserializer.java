package me.ai.training.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.openai.api.OpenAiApi;

import java.io.IOException;
import java.util.List;

/**
 * @author Rohit Muneshwar
 * @created on 2/23/2026
 *
 *
 */

public class EmbeddingListDeserializer extends StdDeserializer<OpenAiApi.EmbeddingList> {
    protected EmbeddingListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public OpenAiApi.EmbeddingList deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        List<OpenAiApi.EmbeddingList> embeddingListFromJson = mapper.readValue(node.asText(), new TypeReference<List<OpenAiApi.EmbeddingList>>() {
        });
        OpenAiApi.EmbeddingList embeddingList = embeddingListFromJson.get(0);
        OpenAiApi.Embedding deserializedEmbeddings = mapper.convertValue(embeddingList.data().get(0), OpenAiApi.Embedding.class);
        return new OpenAiApi.EmbeddingList(embeddingList.object(),
                List.of(deserializedEmbeddings), embeddingList.model(), embeddingList.usage());

    }
}
