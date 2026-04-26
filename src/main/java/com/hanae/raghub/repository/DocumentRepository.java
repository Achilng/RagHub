package com.hanae.raghub.repository;

import com.hanae.raghub.entity.Document;
import com.hanae.raghub.entity.DocumentChunk;
import com.hanae.raghub.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document,Long> {
    List<Document> findByStatus(DocumentStatus status);

    List<Document> findByUserId(Long userId);

    Optional<Document> findByIdAndUserId(Long id, Long userId);
}
