package com.hanae.raghub.service;

import com.hanae.raghub.entity.Document;
import com.hanae.raghub.entity.DocumentChunk;
import com.hanae.raghub.entity.DocumentStatus;
import com.hanae.raghub.exception.ResourceNotFoundException;
import com.hanae.raghub.repository.DocumentChunkRepository;
import com.hanae.raghub.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentChunkRepository chunkRepository,
                           EmbeddingModel embeddingModel,
                           VectorStore vectorStore) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    public List<Document> listDocuments(Long userId) {
        return documentRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteDocument(Long id, Long userId) {
        Document doc = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("文档不存在: " + id));

        List<String> vectorIds = chunkRepository.findVectorIdsByDocumentId(id);
        if (!vectorIds.isEmpty()) {
            vectorStore.delete(vectorIds);
            log.info("已从向量库删除文档 {} 的 {} 个分块", doc.getFilename(), vectorIds.size());
        }

        chunkRepository.deleteByDocumentId(id);
        documentRepository.delete(doc);

        log.info("文档 {} (id={}) 已删除", doc.getFilename(), id);
    }

    @Transactional
    public Document uploadAndProcess(MultipartFile file, Long userId) {
        Document doc = new Document();
        doc.setUserId(userId);
        doc.setFilename(file.getOriginalFilename());
        doc.setMimeType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setStatus(DocumentStatus.PROCESSING);
        doc = documentRepository.save(doc);

        try {
            Resource resource = file.getResource();
            DocumentReader reader = new TikaDocumentReader(resource);
            List<org.springframework.ai.document.Document> parsedDocs = reader.get();

            TokenTextSplitter splitter = new TokenTextSplitter();
            List<org.springframework.ai.document.Document> chunks = splitter.apply(parsedDocs);

            log.info("文档 {} 解析完成，共 {} 个分块", doc.getFilename(), chunks.size());

            List<org.springframework.ai.document.Document> vectorDocs = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                org.springframework.ai.document.Document chunk = chunks.get(i);

                EmbeddingResponse response = embeddingModel.embedForResponse(
                        List.of(chunk.getText()));
                float[] embedding = response.getResult().getOutput();
                int token = response.getMetadata().getUsage().getPromptTokens();

                String vectorId = UUID.randomUUID().toString();

                DocumentChunk dbChunk = new DocumentChunk();
                dbChunk.setDocumentId(doc.getId());
                dbChunk.setChunkIndex(i);
                dbChunk.setContent(chunk.getText());
                dbChunk.setEmbedding(arrayToJson(embedding));
                dbChunk.setVectorId(vectorId);
                dbChunk.setTokenCount(token);
                chunkRepository.save(dbChunk);

                org.springframework.ai.document.Document vectorDoc =
                        new org.springframework.ai.document.Document(
                                vectorId,
                                chunk.getText(),
                                Map.of(
                                        "documentId", doc.getId(),
                                        "chunkIndex", i,
                                        "userId", userId
                                )
                        );
                vectorDocs.add(vectorDoc);
            }

            vectorStore.add(vectorDocs);

            doc.setStatus(DocumentStatus.COMPLETED);
            doc.setChunkCount(chunks.size());
            documentRepository.save(doc);

            log.info("文档 {} 处理完成, 共 {} 个分块已入库", doc.getFilename(), chunks.size());
        } catch (Exception e) {
            log.error("文档 {} 处理失败: {}", doc.getFilename(), e.getMessage(), e);
            doc.setStatus(DocumentStatus.FAILED);
            documentRepository.save(doc);
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
        return doc;
    }

    private String arrayToJson(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
