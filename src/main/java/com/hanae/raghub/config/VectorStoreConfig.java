package com.hanae.raghub.config;

import com.hanae.raghub.entity.DocumentChunk;
import com.hanae.raghub.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Bean
    @Profile("simple")
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Profile("simple")
    public CommandLineRunner rebuildVectorIndex(VectorStore vectorStore,
                                               DocumentChunkRepository chunkRepository) {
        return args -> {
            List<DocumentChunk> chunks = chunkRepository.findAll();

            if (chunks.isEmpty()) {
                log.info("没有已有的文档分块，跳过向量索引重建");
                return;
            }

            List<Document> documents = chunks.stream()
                    .filter(chunk -> chunk.getEmbedding() != null)
                    .map(chunk -> {
                        String id = chunk.getVectorId() != null
                                ? chunk.getVectorId()
                                : UUID.randomUUID().toString();
                        return new Document(
                                id,
                                chunk.getContent(),
                                Map.of(
                                        "documentId", chunk.getDocumentId(),
                                        "chunkIndex", chunk.getChunkIndex()
                                )
                        );
                    })
                    .toList();

            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("向量索引重建完成，加载了 {} 个分块", documents.size());
            } else {
                log.info("没有包含 embedding 的分块，跳过重建");
            }
        };
    }

    @Bean
    @Profile("redis")
    public CommandLineRunner rebuildRedisIndex(VectorStore vectorStore,
                                               DocumentChunkRepository chunkRepository,
                                               StringRedisTemplate redisTemplate){
        return args -> {
            long count = chunkRepository.count();
            if (count==0){
                return;
            }

            Set<String> keys = redisTemplate.keys("raghub:*");
            if (keys != null && !keys.isEmpty()){
                return;
            }

            // 重建环节
            List<DocumentChunk> documentChunks = chunkRepository.findAll();

            // 这里要转为通用的Document
            List<Document> documents = documentChunks
                    .stream()
                    .map(chunk -> new Document(
                            chunk.getVectorId() != null ? chunk.getVectorId() : UUID.randomUUID().toString(),
                            chunk.getContent(),
                            Map.of(
                                    "documentId",chunk.getDocumentId(),
                                    "chunkIndex",chunk.getChunkIndex()
                            )
                    ))
                    .toList();


            vectorStore.add(documents);
        };
    }
}
