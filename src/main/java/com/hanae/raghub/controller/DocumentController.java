package com.hanae.raghub.controller;

import com.hanae.raghub.common.Result;
import com.hanae.raghub.dto.DocumentResponse;
import com.hanae.raghub.dto.SearchRequest;
import com.hanae.raghub.entity.Document;
import com.hanae.raghub.service.DocumentService;
import com.hanae.raghub.service.RetrievalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService documentService;
    private final RetrievalService retrievalService;

    public DocumentController(DocumentService documentService,
                              RetrievalService retrievalService) {
        this.documentService = documentService;
        this.retrievalService = retrievalService;
    }

    @GetMapping("/documents")
    public Result<List<DocumentResponse>> list() {
        List<DocumentResponse> documents = documentService.listDocuments()
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return Result.success(documents);
    }

    @PostMapping("/documents")
    public Result<DocumentResponse> upload(@RequestParam("file") MultipartFile file) {
        Document doc = documentService.uploadAndProcess(file);
        return Result.success(DocumentResponse.from(doc));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public Result<List<Map<String, Object>>> search(@RequestBody SearchRequest body) {
        String query = body.getQuery();
        int topK = body.getTopK();

        List<org.springframework.ai.document.Document> results = retrievalService.search(query, topK);

        List<Map<String, Object>> items = results.stream()
                .map(doc -> Map.<String, Object>of(
                        "content", doc.getText(),
                        "metadata", doc.getMetadata()
                ))
                .toList();

        return Result.success(items);
    }
}
