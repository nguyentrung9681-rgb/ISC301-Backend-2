package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.AiChatService;
import com.example.ecommerce_backend.dto.AiChatRequest;
import com.example.ecommerce_backend.dto.AiChatResponse;
import com.example.ecommerce_backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiChatResponse>> getChatResponse(@RequestBody AiChatRequest request) {
        AiChatResponse response = aiChatService.getChatResponse(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
