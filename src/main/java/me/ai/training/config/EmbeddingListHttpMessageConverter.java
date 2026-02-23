package me.ai.training.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Rohit Muneshwar
 * @created on 2/22/2026
 *
 *
 */
public class EmbeddingListHttpMessageConverter extends AbstractHttpMessageConverter<OpenAiApi.EmbeddingList> {
    public EmbeddingListHttpMessageConverter() {
        super(StandardCharsets.UTF_8, new MediaType("text","plain", StandardCharsets.UTF_8));
        // text/plain;charset=UTF-8
    }

    @Override
    protected boolean canRead(@Nullable MediaType mediaType) {
        return super.canRead(mediaType);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OpenAiApi.EmbeddingList.class == clazz;
    }

    @Override
    protected void writeInternal(OpenAiApi.EmbeddingList embeddingList, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

    }

    @Override
    protected OpenAiApi.EmbeddingList readInternal(Class<? extends OpenAiApi.EmbeddingList> clazz, HttpInputMessage inputMessage) throws IOException {
        long length = inputMessage.getHeaders().getContentLength();
        byte[] bytes = (length >= 0 && length <= Integer.MAX_VALUE ?
                inputMessage.getBody().readNBytes((int) length) : inputMessage.getBody().readAllBytes());
        ObjectMapper mapper =new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OpenAiApi.EmbeddingList.class, new EmbeddingListDeserializer(OpenAiApi.EmbeddingList.class));
        mapper.registerModule(module);
        return mapper.convertValue(new String(bytes), new TypeReference<OpenAiApi.EmbeddingList>() {
        });
    }
}
