package com.hanae.raghub.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore.MetadataField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisVectorStoreConfig {

    @Bean
    public JedisPooled jedisPooled(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port) {
        return new JedisPooled(host, port);
    }

    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("raghub-vectors")
                .prefix("raghub:")
                .metadataFields(
                        MetadataField.numeric("userId"),
                        MetadataField.numeric("documentId"),
                        MetadataField.numeric("chunkIndex"))
                .initializeSchema(true)
                .build();
    }
}
