package com.hanae.raghub;

import com.hanae.raghub.entity.Document;
import jakarta.transaction.Transactional;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class RagHubApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Test
    void vectorStoreTest() {
        var result = vectorStore.similaritySearch("test");
        System.out.println("结果数量："+result.size());
        result.forEach(doc -> System.out.println(doc.getText()));
    }

    @Test
    void DocumentServiceTest(){
        final Logger logs = LoggerFactory.getLogger(RagHubApplicationTests.class);
        logs.info("测试测试");
        logs.error("这里是error");
        logs.debug("这里是debug");
    }

}
