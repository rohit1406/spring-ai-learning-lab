package me.ai.training.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Rohit Muneshwar
 * @created on 2/19/2026
 *
 *
 */
@Service
public class DataFeedService {
    @Autowired
    private VectorStore vectorStore;

    public void saveData(List<String> data){
        List<Document> documents = data.stream().map(Document::new).toList();
        vectorStore.add(documents);
    }
}
