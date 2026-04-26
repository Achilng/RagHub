package com.hanae.raghub.controller;

import com.hanae.raghub.common.Result;
import com.hanae.raghub.dto.DocumentResponse;
import com.hanae.raghub.dto.SearchRequest;
import com.hanae.raghub.entity.Document;
import com.hanae.raghub.entity.User;
import com.hanae.raghub.service.DocumentService;
import com.hanae.raghub.service.RetrievalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public Result<List<DocumentResponse>> list(@AuthenticationPrincipal User user) {
        List<DocumentResponse> documents = documentService.listDocuments(user.getId())
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return Result.success(documents);
    }

    @PostMapping("/documents")
    public Result<DocumentResponse> upload(@AuthenticationPrincipal User user,
                                           @RequestParam("file") MultipartFile file) {
        Document doc = documentService.uploadAndProcess(file, user.getId());
        return Result.success(DocumentResponse.from(doc));
    }

    @DeleteMapping("/documents/{id}")
    public Result<Void> delete(@AuthenticationPrincipal User user,
                               @PathVariable Long id) {
        documentService.deleteDocument(id, user.getId());
        return Result.success(null);
    }

    @PostMapping("/search")
    public Result<List<Map<String, Object>>> search(@AuthenticationPrincipal User user,
                                                    @RequestBody SearchRequest body) {
        String query = body.getQuery();
        int topK = body.getTopK();

        List<org.springframework.ai.document.Document> results = retrievalService.search(query, topK, user.getId());

        List<Map<String, Object>> items = results.stream()
                .map(doc -> Map.<String, Object>of(
                        "content", doc.getText(),
                        "metadata", doc.getMetadata()
                ))
                .toList();

        return Result.success(items);
    }
}
