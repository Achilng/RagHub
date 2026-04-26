package com.hanae.raghub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RetrievalService.class);

    private final VectorStore vectorStore;

    public RetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> search(String query, int topK, Long userId) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression("userId == " + userId)
                .build();

        List<Document> results = vectorStore.similaritySearch(request);

        log.info("用户 {} 检索 '{}', 返回 {} 条结果", userId, query, results.size());

        return results;
    }
}