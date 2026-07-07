package com.example.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatRequest {
    private String message;
    private List<ChatMessage> history;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessage {
        private String role; // "user" or "model" (Gemini uses "user" and "model")
        private String text;
    }
}
