package com.edu.spring.ai.service;

import lombok.extern.slf4j.Slf4j;
import com.edu.spring.ai.entity.Customer;
import com.edu.spring.ai.exceptions.DataStoreException;
import com.edu.spring.ai.repository.CustomerRepository;
import com.edu.spring.ai.util.DataUtil;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Rohit Muneshwar
 * @created on 2/19/2026
 *
 *
 */
@Service
@Slf4j
public class DataFeedService {
    private VectorStore vectorStore;
    private CustomerRepository customerRepository;
    public DataFeedService(VectorStore vectorStore, CustomerRepository customerRepository){
        this.vectorStore = vectorStore;
        this.customerRepository = customerRepository;
    }

    public void saveData(List<String> data){
        List<Document> documents = data.stream().map(Document::new).toList();
        vectorStore.add(documents);
    }

    public void ingestDocument(Resource resource, String documentId, String tenantId){
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        /**
         * Convert document into chunks
         * chunkSize: target size of each text chunk in size
         * minChunkSizeChars: min size of each text chunk in char
         * minChunkLengthToEmbed: min length of chunk to be included
         * maxNumChunk:  max no of chunks to generate from a text
         * keepSeparator: whether to keep separator
         * punctuationMarks: list of punctuation marks
         */
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(800, 350, 5, 10000, true, List.of('.', '?', '!', '\n'));
        List<Document> chunkedDocuments =tokenTextSplitter.apply(documents);
        // add metadata for filtering
        List<Document> enrichedDocuments = chunkedDocuments.stream()
                .map(doc -> {
                    // enrich the topic of the subject
                    String topic = getTopic(doc);
                    return new Document(doc.getText(), Map.of("document_id", documentId, "tenant_id", tenantId, "topic", topic));
                })
                .toList();
        vectorStore.add(enrichedDocuments);
    }

    public void ingestTestDocumentWithReferences(Resource resource, String documentId, String tenantId){
        log.info("inserting doc:{} with tenant-id:{}", documentId, tenantId);
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        /**
         * Convert document into chunks
         * chunkSize: target size of each text chunk in size
         * minChunkSizeChars: min size of each text chunk in char
         * minChunkLengthToEmbed: min length of chunk to be included
         * maxNumChunk:  max no of chunks to generate from a text
         * keepSeparator: whether to keep separator
         * punctuationMarks: list of punctuation marks
         */
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(800, 350, 5, 10000, true, List.of('.', '?', '!', '\n'));
        List<Document> chunkedDocuments =tokenTextSplitter.apply(documents);
        // add metadata for filtering
        // document level reference to metadata
        List<String> references = switch (documentId){
            case "DOC1" -> List.of("DOC2");
            case "DOC2" -> List.of("DOC3");
            default -> List.of();
        };
        List<Document> enrichedDocuments = chunkedDocuments.stream()
                .map(doc -> {
                    log.info("putting metadata: docId:{}, tenantId:{}, references:{}",documentId, tenantId, references);
                    Map<String,Object> metadata=new HashMap<>();
                    metadata.put("document_id",documentId);
                    metadata.put("tenant_id",tenantId);
                    metadata.put("references",references);

                    return new Document(doc.getText(), metadata);
                })
                .toList();
        vectorStore.add(enrichedDocuments);
    }

    public void ingestSmallerChunks(Resource resource, String tenantId){
        String documentId = resource.getFilename();
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        List<Document> originalDocuments = tikaDocumentReader.get();
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(128, 60, 5, 1500, true, List.of('.', '?', '!', '\n'));
        List<Document> splitDocuments = tokenTextSplitter.apply(originalDocuments);
        List<Document> enrichedDocuments = splitDocuments.stream().map(doc -> new Document(doc.getText(), Map.of("document_id", documentId, "tenant_id", tenantId))).toList();
        log.info("document:{}, orgDocuments:{} and splitDocuments:{}",documentId, originalDocuments.size(), splitDocuments.size());
        vectorStore.add(enrichedDocuments);
        log.info("chunks of size {} saved to vector db", enrichedDocuments.size());
    }

    private static String getTopic(Document doc) {
        String text = doc.getText();
        String topic = "GENERAL";
        if(text != null) {
            if (text.contains("leave")) {
                topic = "LEAVE";
            } else if (text.contains("expense")) {
                topic = "EXPENSE";
            } else if (text.contains("tax")) {
                topic = "TAX";
            } else if (text.contains("code of conduct")) {
                topic = "CONDUCT";
            }
        }
        return topic;
    }

    public void saveJavaTestData() {
        this.validateData(DataUtil.WHAT_IS_JAVA);
        log.info("Saving data");
        this.saveData(DataUtil.getData());
    }

    public void saveBigBunnySubtitleData() {
        this.validateData(DataUtil.BIG_BUNNY_SUBTITLE_CHUNK);
        log.info("Saving data");
        this.saveData(DataUtil.getBigBuckBunnySubtitle());
    }

    public void validateData(String content) {
        log.info("checking first if data already saved");
        if(vectorStore.similaritySearch(SearchRequest.builder().query(content).build()).stream()
                .anyMatch(text -> !Objects.isNull(text.getText()) && text.getText().contains(content))){
            throw new DataStoreException("Data already saved to db");
        }
    }

    /**
     * It uses same data to save which is present in data.sql
     */
    public void saveCustomerData() {
        List<Customer> customerList = List.of(
                getCustomer("98337ba9-abb1-486a-bbe6-c3a7b3328a21", "roh143", "N. Modi", 2300),
                getCustomer("98337ba9-abb1-486a-bbe6-c3a7b3328a31", "swap1432", "D. Trump", 250.50)
        );
        if(customerRepository.findById("98337ba9-abb1-486a-bbe6-c3a7b3328a21").isPresent()){
            throw new DataStoreException("Customer Data already saved to db");
        }
        customerRepository.saveAll(customerList);
    }

    private Customer getCustomer(String id, String customerId, String name, double balance){
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setCustomerId(customerId);
        customer.setBalance(balance);
        return customer;
    }
}
