package fitSnap.product_service.kafka;

import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fitSnap.product_service.entity.Product;
import fitSnap.product_service.event.ProductProcessedEvent;
import fitSnap.product_service.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductProcessedConsumer {

    private final ProductRepository productRepository;

    @KafkaListener(
            topics = "product_processed_result",
            groupId = "product-service-group",
            properties = {"spring.json.value.default.type=fitSnap.product_service.event.ProductProcessedEvent"})
    public void handleProcessResult(ProductProcessedEvent result) {
        log.info("Received AI processing result for product: {}", result.getId());

        Optional<Product> productOpt = productRepository.findById(result.getId());
        if (productOpt.isEmpty()) {
            log.error("Product not found for ID: {}", result.getId());
            return;
        }

        Product product = productOpt.get();
        product.setStatus("COMPLETED");

        if (result.getName() != null) product.setName(result.getName());
        if (result.getImageUrl() != null) product.setImageUrl(result.getImageUrl());
        if (result.getCategory() != null) product.setCategory(result.getCategory());
        if (result.getType() != null) product.setType(result.getType());
        if (result.getColor() != null) product.setColor(result.getColor());
        if (result.getMaterial() != null) product.setMaterial(result.getMaterial());
        if (result.getPattern() != null) product.setPattern(result.getPattern());
        if (result.getStyle() != null) product.setStyle(result.getStyle());
        if (result.getFit() != null) product.setFit(result.getFit());
        if (result.getDescription() != null) product.setDescription(result.getDescription());

        productRepository.save(product);
        log.info("Product updated successfully, status: COMPLETED");
    }
}
