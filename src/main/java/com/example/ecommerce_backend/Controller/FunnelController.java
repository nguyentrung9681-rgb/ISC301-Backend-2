package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.MarketResearchService;
import com.example.ecommerce_backend.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/funnel")
public class FunnelController {

    @Autowired
    private MarketResearchService marketResearchService;

    @PostMapping("/track")
    public ResponseEntity<ApiResponse<Void>> trackEvent(
            @RequestParam String eventType,
            @RequestParam String sessionId,
            @RequestParam(required = false) Long productId) {
        
        marketResearchService.trackFunnelEvent(eventType, sessionId, productId);
        return ResponseEntity.ok(ApiResponse.ok("Đã ghi nhận sự kiện " + eventType, null));
    }
}
