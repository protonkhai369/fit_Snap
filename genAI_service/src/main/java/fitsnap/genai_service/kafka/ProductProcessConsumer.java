package fitsnap.genAI_service.kafka;

import fitsnap.genAI_service.event.ProductCreationEvent;
import fitsnap.genAI_service.event.ProductProcessedEvent;
import fitsnap.genAI_service.service.GeminiClothResult;
import fitsnap.genAI_service.service.GeminiClothService;
import fitsnap.genAI_service.service.S3Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductProcessConsumer {

    KafkaTemplate<String, Object> kafkaTemplate;
    S3Service s3Service;
    GeminiClothService geminiClothService;

    @KafkaListener(
            topics = "product_process",
            groupId = "genai-service-group",
            properties = {"spring.json.value.default.type=fitsnap.genAI_service.event.ProductCreationEvent"})
    public void listen(ProductCreationEvent event) {
        log.info("Received product creation event for productId: {}", event.getId());
        try {
            // 1. Tải ảnh gốc từ S3
            byte[] imageBytes = s3Service.downloadByUrl(event.getImageUrl());

            // 2. Gọi Gemini AI để phân tích và xóa phông nền
            GeminiClothResult aiResult = geminiClothService.analyzeCloth(imageBytes);

            if (aiResult == null) {
                throw new RuntimeException("Gemini returned null result for productId: " + event.getId());
            }

            // 3. Upload ảnh đã xóa phông lên S3 (nếu có), nếu không dùng ảnh gốc
            String finalImageUrl = event.getImageUrl();
            if (aiResult.getSegmentedImage() != null) {
                finalImageUrl = s3Service.uploadBytes(
                        aiResult.getSegmentedImage(),
                        "products/" + event.getUserId() + "/" + event.getId() + "_processed",
                        "image/png"
                );
            }

            // 4. Sinh tên sản phẩm dựa trên các đặc tính do AI phân tích
            String name = Optional.ofNullable(aiResult.getDescription())
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .orElseGet(() -> {
                        String composed = String.join(
                                " ",
                                Optional.ofNullable(aiResult.getColor()).orElse(""),
                                Optional.ofNullable(aiResult.getType()).orElse(""),
                                Optional.ofNullable(aiResult.getCategory()).orElse(""));
                        composed = composed.trim().replaceAll("\\s+", " ");
                        return composed.isEmpty() ? "Untitled product" : composed;
                    });

            // 5. Gói tất cả kết quả vào Event
            ProductProcessedEvent result = ProductProcessedEvent.builder()
                    .id(event.getId())
                    .userId(event.getUserId())
                    .name(name)
                    .description(aiResult.getDescription())
                    .category(aiResult.getCategory())
                    .type(aiResult.getType())
                    .color(aiResult.getColor())
                    .material(aiResult.getMaterial())
                    .pattern(aiResult.getPattern())
                    .style(aiResult.getStyle())
                    .fit(aiResult.getFit())
                    .imageUrl(finalImageUrl)
                    .build();

            // 6. Gửi kết quả về lại cho product_service
            kafkaTemplate.send("product_processed_result", result);
            log.info("Sent product processed result for productId: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to process product: {}", event.getId(), e);
        }
    }
}
