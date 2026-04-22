package com.hanae.raghub.dto;

import com.hanae.raghub.entity.Document;
import com.hanae.raghub.entity.DocumentStatus;

import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private String filename;
    private Long fileSize;
    private String mimeType;
    private DocumentStatus status;
    private Integer chunkCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DocumentResponse from(Document doc) {
        DocumentResponse resp = new DocumentResponse();
        resp.id = doc.getId();
        resp.filename = doc.getFilename();
        resp.fileSize = doc.getFileSize();
        resp.mimeType = doc.getMimeType();
        resp.status = doc.getStatus();
        resp.chunkCount = doc.getChunkCount();
        resp.createdAt = doc.getCreatedAt();
        resp.updatedAt = doc.getUpdatedAt();
        return resp;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
