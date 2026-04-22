package com.hanae.raghub.repository;

import com.hanae.raghub.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);

    @Query("SELECT c.vectorId FROM DocumentChunk c WHERE c.documentId = :documentId AND c.vectorId IS NOT NULL")
    List<String> findVectorIdsByDocumentId(@Param("documentId") Long documentId);

    void deleteByDocumentId(Long documentId);
}
