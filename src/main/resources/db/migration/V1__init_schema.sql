-- documents: 文档主表
CREATE TABLE documents (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    filename     VARCHAR(255) NOT NULL,
    file_size    BIGINT       NOT NULL,
    mime_type    VARCHAR(64)  NOT NULL,
    status       VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    chunk_count  INT          NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_documents_status (status),
    KEY idx_documents_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- document_chunks: 分块表
CREATE TABLE document_chunks (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    document_id  BIGINT   NOT NULL,
    chunk_index  INT      NOT NULL,
    content      TEXT     NOT NULL,
    embedding    LONGTEXT NULL,
    token_count  INT      NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chunks_doc_index (document_id, chunk_index),
    KEY idx_chunks_document_id (document_id),
    CONSTRAINT fk_chunks_document
        FOREIGN KEY (document_id) REFERENCES documents (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
