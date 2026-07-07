package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductStatus;
import com.example.ecommerce_backend.Repository.ProductRepository;
import com.example.ecommerce_backend.dto.AiChatRequest;
import com.example.ecommerce_backend.dto.AiChatResponse;
import com.example.ecommerce_backend.dto.ProductResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiChatService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    public AiChatService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public AiChatResponse getChatResponse(AiChatRequest request) {
        List<Product> activeProducts = productRepository.findByProductStatus(ProductStatus.ACTIVE);

        // Nếu API Key rỗng hoặc null, dùng Mock AI Stylist fallback an toàn
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return getMockResponse(request.getMessage(), activeProducts);
        }

        try {
            // 1. Tạo System Instruction Prompt
            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append("Bạn là Trợ lý Thời trang AI (AI Fashion Stylist) của thương hiệu thời trang Jusstlife.\n");
            systemPrompt.append("Hãy tư vấn nhiệt tình, thân thiện, trả lời các thắc mắc của khách hàng về thời trang, phối đồ, hoặc chính sách của shop:\n");
            systemPrompt.append("- Đổi trả hàng: Hỗ trợ đổi trả trong vòng 7 ngày kể từ khi nhận hàng. Điều kiện sản phẩm nguyên tag mác, chưa qua sử dụng, chưa giặt là và không có mùi lạ.\n");
            systemPrompt.append("- Giao hàng & Phí ship: Đồng giá ship 30k toàn quốc. Miễn phí vận chuyển cho đơn hàng từ 500k trở lên. Giao hàng từ 2-4 ngày toàn quốc.\n");
            systemPrompt.append("- Hotline hỗ trợ: 1900-6789 (Hỗ trợ từ 8:00 đến 22:00 hàng ngày).\n\n");
            systemPrompt.append("Đây là danh sách sản phẩm thực tế có trong cửa hàng hiện tại. ");
            systemPrompt.append("Nếu khách hàng hỏi phối đồ, chọn đồ hoặc hỏi về sản phẩm, hãy giới thiệu các sản phẩm phù hợp từ danh sách này bằng cách chèn thẻ [PRODUCT_RECOMMEND:id] vào câu trả lời của bạn, trong đó id là số ID của sản phẩm đó. ");
            systemPrompt.append("Bạn được gợi ý tối đa 3 sản phẩm trong một câu trả lời. ");
            systemPrompt.append("Tuyệt đối KHÔNG gợi ý các sản phẩm KHÔNG có trong danh sách này.\n\n");
            systemPrompt.append("DANH SÁCH SẢN PHẨM CỦA SHOP:\n");

            for (Product p : activeProducts) {
                systemPrompt.append(String.format("- ID: %d | Tên: %s | Danh mục: %s | Giá: %s VND | Mô tả: %s | Kích thước: %s | Màu sắc: %s\n",
                        p.getId(),
                        p.getProductName(),
                        categoryService.resolveCategoryLabel(p.getCategory()),
                        p.getPrice().toString(),
                        p.getDescription() != null ? p.getDescription() : "Chưa cập nhật",
                        p.getSize() != null ? p.getSize() : "Chưa cập nhật",
                        p.getColor() != null ? p.getColor() : "Chưa cập nhật"
                ));
            }

            // 2. Xây dựng request JSON payload gửi đến Gemini API
            Map<String, Object> requestPayload = new LinkedHashMap<>();

            // Thêm systemInstruction
            Map<String, Object> systemInstructionPart = new HashMap<>();
            systemInstructionPart.put("parts", List.of(Map.of("text", systemPrompt.toString())));
            requestPayload.put("systemInstruction", systemInstructionPart);

            // Thêm contents (bao gồm history + tin nhắn mới)
            List<Map<String, Object>> contents = new ArrayList<>();

            if (request.getHistory() != null) {
                for (AiChatRequest.ChatMessage historyMsg : request.getHistory()) {
                    Map<String, Object> historyContent = new LinkedHashMap<>();
                    historyContent.put("role", historyMsg.getRole().equalsIgnoreCase("user") ? "user" : "model");
                    historyContent.put("parts", List.of(Map.of("text", historyMsg.getText())));
                    contents.add(historyContent);
                }
            }

            // Thêm câu hỏi hiện tại
            Map<String, Object> currentContent = new LinkedHashMap<>();
            currentContent.put("role", "user");
            currentContent.put("parts", List.of(Map.of("text", request.getMessage())));
            contents.add(currentContent);

            requestPayload.put("contents", contents);

            String requestBody = objectMapper.writeValueAsString(requestPayload);

            // 3. Thực hiện gọi API Gemini
            String apiResponse = sendGeminiRequest(geminiApiKey, requestBody);

            // 4. Phân tích kết quả trả về từ Gemini
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            JsonNode candidate = rootNode.path("candidates").get(0);
            String aiTextResponse = candidate.path("content").path("parts").get(0).path("text").asText();

            // 5. Trích xuất ID sản phẩm từ cú pháp [PRODUCT_RECOMMEND:id]
            List<ProductResponseDTO> recommendedProducts = extractRecommendedProducts(aiTextResponse, activeProducts);

            // Làm sạch văn bản: Loại bỏ các thẻ [PRODUCT_RECOMMEND:id] hoặc thay thế chúng để giao diện hiển thị gọn gàng
            String cleanedText = aiTextResponse.replaceAll("\\[PRODUCT_RECOMMEND:\\d+\\]", "").trim();

            return new AiChatResponse(cleanedText, recommendedProducts);

        } catch (Exception e) {
            System.err.println("Lỗi kết nối Gemini API, kích hoạt Mock Stylist: " + e.getMessage());
            return getMockResponse(request.getMessage(), activeProducts);
        }
    }

    private String sendGeminiRequest(String apiKey, String requestJson) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP Error " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    private List<ProductResponseDTO> extractRecommendedProducts(String text, List<Product> activeProducts) {
        List<ProductResponseDTO> recommended = new ArrayList<>();
        Set<Long> productIds = new LinkedHashSet<>();

        Pattern pattern = Pattern.compile("\\[PRODUCT_RECOMMEND:(\\d+)\\]");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            try {
                productIds.add(Long.parseLong(matcher.group(1)));
            } catch (NumberFormatException ignored) {}
        }

        for (Long id : productIds) {
            activeProducts.stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .ifPresent(p -> recommended.add(new ProductResponseDTO(p, categoryService.resolveCategoryLabel(p.getCategory()))));
        }

        return recommended;
    }

    /**
     * Mock AI Fashion Stylist Fallback khi không cấu hình Gemini API Key
     */
    private AiChatResponse getMockResponse(String userMessage, List<Product> activeProducts) {
        String lowerMessage = userMessage.toLowerCase();
        List<ProductResponseDTO> recommended = new ArrayList<>();
        String responseText;

        if (lowerMessage.contains("đổi trả") || lowerMessage.contains("hoàn tiền")) {
            responseText = "Dạ, Jusstlife hỗ trợ đổi trả sản phẩm trong vòng 7 ngày kể từ ngày nhận hàng thành công. " +
                    "Yêu cầu sản phẩm đổi trả phải còn nguyên mác tag, chưa qua sử dụng hay giặt là và không có mùi lạ nhé ạ!";
        } else if (lowerMessage.contains("giao hàng") || lowerMessage.contains("ship") || lowerMessage.contains("vận chuyển")) {
            responseText = "Thời gian giao hàng tiêu chuẩn là 2 - 4 ngày trên toàn quốc. " +
                    "Phí ship đồng giá 30k toàn quốc, đặc biệt shop miễn phí vận chuyển cho các đơn hàng từ 500k trở lên đó ạ!";
        } else if (lowerMessage.contains("hotline") || lowerMessage.contains("liên hệ") || lowerMessage.contains("sđt")) {
            responseText = "Số hotline chính thức chăm sóc khách hàng của Jusstlife là 1900-6789. Bọn mình hỗ trợ tư vấn từ 8:00 đến 22:00 hàng ngày nha.";
        } else {
            // Phối đồ Stylist mock
            String keyword = "";
            if (lowerMessage.contains("áo") || lowerMessage.contains("phông") || lowerMessage.contains("sơ mi") || lowerMessage.contains("t-shirt")) {
                keyword = "ao";
                responseText = "Chào bạn! Đây là một số mẫu Áo sành điệu, dễ phối đồ tại Jusstlife gợi ý riêng cho bạn. Bạn có thể phối cùng quần jean hoặc chân váy để tăng sự năng động nha!";
            } else if (lowerMessage.contains("váy") || lowerMessage.contains("đầm") || lowerMessage.contains("dress")) {
                keyword = "vay";
                responseText = "Jusstlife gợi ý cho bạn những mẫu Váy cực kỳ thanh lịch và tôn dáng dưới đây, rất thích hợp cho các buổi tiệc hoặc đi dạo phố cuối tuần đó ạ!";
            } else if (lowerMessage.contains("quần") || lowerMessage.contains("jean") || lowerMessage.contains("pant")) {
                keyword = "quan";
                responseText = "Gửi bạn một số mẫu Quần chất lượng tốt, đứng dáng và cực kỳ thoải mái của Jusstlife. Các mẫu này đều rất dễ mặc và phối đồ hàng ngày!";
            } else {
                responseText = "Xin chào! Mình là Trợ lý thời trang AI (AI Fashion Stylist) của Jusstlife. Bạn cần mình tư vấn chọn size, tìm kiểu đồ mặc đi tiệc, đi chơi hay hỗ trợ thông tin gì không ạ? " +
                        "Dưới đây là một số sản phẩm bán chạy nhất hôm nay để bạn tham khảo nha!";
            }

            final String finalKeyword = keyword;
            List<Product> filtered = activeProducts.stream()
                    .filter(p -> finalKeyword.isEmpty() || p.getCategory().toLowerCase().contains(finalKeyword) || p.getProductName().toLowerCase().contains(finalKeyword))
                    .limit(3)
                    .toList();

            // Nếu lọc không ra sản phẩm nào, lấy đại 3 sản phẩm đầu tiên
            if (filtered.isEmpty()) {
                filtered = activeProducts.stream().limit(3).toList();
            }

            for (Product p : filtered) {
                recommended.add(new ProductResponseDTO(p, categoryService.resolveCategoryLabel(p.getCategory())));
            }
        }

        return new AiChatResponse(responseText, recommended);
    }
}
