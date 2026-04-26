package com.hanae.raghub.controller;

import com.hanae.raghub.common.Result;
import com.hanae.raghub.dto.RagRequest;
import com.hanae.raghub.dto.RagResponse;
import com.hanae.raghub.entity.User;
import com.hanae.raghub.service.RetrievalService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RagDemoController {

    private final ChatClient chatClient;
    private final RetrievalService retrievalService;

    public RagDemoController(@Qualifier("deepSeekChatModel") ChatModel chatModel,
                             RetrievalService retrievalService) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.retrievalService = retrievalService;
    }

    @PostMapping("/rag")
    public Result<RagResponse> generate(@AuthenticationPrincipal User user,
                                        @RequestBody RagRequest request) {
        List<Document> docs = retrievalService.search(request.getQuery(), request.getTopK(), user.getId());

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String answer = chatClient.prompt()
                .system("""
                        你是一个基于 RAG（检索增强生成）的问答助手。请根据以下参考资料回答用户的问题。

                        规则：
                        - 仅基于提供的参考资料作答，不要编造信息
                        - 如果参考资料中没有相关内容，请如实告知
                        - 回答要简洁、准确

                        参考资料：
                        """ + context)
                .user(request.getQuery())
                .call()
                .content();

        List<RagResponse.Source> sources = docs.stream()
                .map(doc -> new RagResponse.Source(doc.getText(), doc.getMetadata()))
                .toList();

        return Result.success(new RagResponse(answer, sources));
    }
}
