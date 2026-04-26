package com.hanae.raghub.dto;

import java.util.List;
import java.util.Map;

public class RagResponse {

    private String answer;
    private List<Source> sources;

    public RagResponse(String answer, List<Source> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public List<Source> getSources() {
        return sources;
    }

    public static class Source {

        private String content;
        private Map<String, Object> metadata;

        public Source(String content, Map<String, Object> metadata) {
            this.content = content;
            this.metadata = metadata;
        }

        public String getContent() {
            return content;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }
}
