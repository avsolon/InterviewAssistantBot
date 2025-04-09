package org.example.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GptResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private String systemFingerprint;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Integer index;
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;
        private Object logprobs; // Может быть null или содержать данные
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
        private Object refusal; // Может быть null
        private List<Object> annotations; // Может быть пустым
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    public String getFirstAnswer() {
        try {
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("No choices in response");
            }
            Message message = choices.get(0).getMessage();
            if (message == null || message.getContent() == null) {
                throw new IllegalStateException("No valid message content");
            }
            return message.getContent();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract answer: " + e.getMessage(), e);
        }
    }
}

//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)  // Игнорируем неизвестные поля
//public class GptResponse {
//
//    private List<Choice> choices;
//    private String model;  // Добавляем поле model из ответа
//
//    @Data
//    public static class Choice {
//        private Message message;
//        private Integer index;
//        @JsonProperty("finish_reason")
//        private String finishReason;
//
//        @JsonCreator
//        public Choice(
//                @JsonProperty("message") Message message,
//                @JsonProperty("index") Integer index,
//                @JsonProperty("finish_reason") String finishReason
//        ) {
//            this.message = message;
//            this.index = index;
//            this.finishReason = finishReason;
//        }
//    }
//
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class Message {
//        private String content;
//        private String role;  // Добавляем поле role ("system"/"user"/"assistant")
//
//        @JsonCreator
//        public Message(
//                @JsonProperty("content") String content,
//                @JsonProperty("role") String role
//        ) {
//            this.content = content;
//            this.role = role;
//        }
//    }
//
//    @JsonCreator
//    public GptResponse(
//            @JsonProperty("choices") List<Choice> choices,
//            @JsonProperty("model") String model
//    ) {
//        this.choices = choices;
//        this.model = model;
//    }
//
//    // Метод для безопасного извлечения ответа
//    public String getFirstAnswer() {
//        if (choices == null || choices.isEmpty()) {
//            throw new IllegalStateException("No choices in GPT response");
//        }
//        Message message = choices.get(0).getMessage();
//        if (message == null || message.getContent() == null) {
//            throw new IllegalStateException("No valid message in GPT response");
//        }
//        return message.getContent();
//    }
//}