package me.ai.training.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Rohit Muneshwar
 * @created on 2/25/2026
 *
 *
 */
@Service
@Slf4j
public class MultiHopSearchRagService {
    private final VectorStore vectorStore;

    public MultiHopSearchRagService(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }

    public Map<String, Object> multiHopSearch(String query, String startDocId, String tenantId,
                                              int maxHops, int topK, double threshold){
        // store result of each hop which contains documentId, tenantId, next reference
        List<Map<String, Object>> hops = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        // set the entry document
        String currentDocumentId = startDocId;
        // stop when maxHops is reached
        for(int hop=1; hop<=maxHops && currentDocumentId!=null; hop++){
            if(visited.contains(currentDocumentId)){
                break;
            }
            visited.add(currentDocumentId);
            String filter = "document_id=='"+currentDocumentId+"' AND tenant_id=='"+tenantId+"'";
            String hopQuery = (hop==1) ? "refer to organizations document": query;
            List<Document> retrieved = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(hopQuery)
                            .topK(topK)
                            .similarityThreshold(hop==1?0.15:threshold)
                            .filterExpression(filter).build()
            );

            Set<String> nextRef = new LinkedHashSet<>();
            for(Document d: retrieved){
                Object refs = d.getMetadata().get("references");
                if(refs instanceof List<?> list){
                    for(Object ref: list){
                        if(ref!=null){
                            nextRef.add(ref.toString());
                        }
                    }
                }
            }

            Map<String, Object> hopResult = new LinkedHashMap<>();
            hopResult.put("hop", hop);
            hopResult.put("document-id", currentDocumentId);
            hopResult.put("queryUsed", hopQuery);
            hopResult.put("snippets", retrieved.stream().map(Document::getText).toList());
            hopResult.put("nextReferences", nextRef);

            hops.add(hopResult);
            currentDocumentId = nextRef.stream().findFirst().orElse(null);
        }
        return Map.of("query", query,
                "startDocumentId", startDocId,
                "tenantId", tenantId,
                "visited", visited,
                "hops", hops);
    }
}
