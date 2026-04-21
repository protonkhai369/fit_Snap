package fitSnap.product_service.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fitSnap.product_service.dto.request.ProductRequest;
import fitSnap.product_service.dto.response.ProductResponse;
import fitSnap.product_service.entity.Product;
import fitSnap.product_service.event.ProductCreationEvent;
import fitSnap.product_service.mapper.ProductMapper;
import fitSnap.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final S3Service s3Service;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductResponse create(MultipartFile image) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("Creating product for user (Async): {}", userId);

        String imageUrl = s3Service.uploadBytes(image.getBytes(), "products/" + userId + "/raw", "image/png");

        Product product = new Product();
        product.setUserId(userId);
        product.setImageUrl(imageUrl);
        product.setName("Processing Product...");
        product.setStatus("PROCESSING");

        product = productRepository.save(product);
        log.info("Product created with PROCESSING status, id: {}", product.getId());

        ProductCreationEvent event = ProductCreationEvent.builder()
                .id(product.getId())
                .userId(userId)
                .imageUrl(imageUrl)
                .build();

        kafkaTemplate.send("product_process", event);

        return productMapper.toProductResponse(product);
    }

    public List<ProductResponse> getMyProducts() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return productRepository.findByUserId(userId).stream()
                .map(p -> {
                    ProductResponse resp = productMapper.toProductResponse(p);

                    if (resp.getImageUrl() != null) {
                        resp.setImageUrl(s3Service.generatePresignedUrl(resp.getImageUrl(), Duration.ofMinutes(30)));
                    }

                    return resp;
                })
                .collect(Collectors.toList());
    }

    public ProductResponse getById(String id) {
        log.info("Fetching product with id: {}", id);

        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        ProductResponse resp = productMapper.toProductResponse(product);
        if (resp.getImageUrl() != null) {
            String presigned = s3Service.generatePresignedUrl(resp.getImageUrl(), Duration.ofMinutes(15));
            resp.setImageUrl(presigned);
        }
        return resp;
    }

    public ProductResponse update(String id, ProductRequest request) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this product");
        }

        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getType() != null) product.setType(request.getType());
        if (request.getColor() != null) product.setColor(request.getColor());
        if (request.getMaterial() != null) product.setMaterial(request.getMaterial());
        if (request.getPattern() != null) product.setPattern(request.getPattern());
        if (request.getStyle() != null) product.setStyle(request.getStyle());
        if (request.getFit() != null) product.setFit(request.getFit());
        if (request.getDescription() != null) product.setDescription(request.getDescription());

        product = productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    public void delete(String id) {
        log.info("Deleting product with id: {}", id);

        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this product");
        }

        s3Service.deleteFile(product.getImageUrl());

        productRepository.delete(product);

        log.info("Product deleted successfully");
    }
}
