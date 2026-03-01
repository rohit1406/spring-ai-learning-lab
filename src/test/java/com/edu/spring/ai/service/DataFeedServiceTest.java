package com.edu.spring.ai.service;

import com.edu.spring.ai.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rohit Muneshwar
 * @created on 3/1/2026
 *
 *
 */
public class DataFeedServiceTest {

    @Mock
    VectorStore vectorStore;

    @Mock
    CustomerRepository customerRepository;
    DataFeedService dataFeedService = new DataFeedService(vectorStore, customerRepository);


    @Test
    void testSplit(){
        dataFeedService.ingestSmallerChunks(new ClassPathResource("subtitles/TheLastSignal-scifi-drama-fictional-short-film.srt"), "rohit");
        Assertions.assertTrue(true);
    }
}
