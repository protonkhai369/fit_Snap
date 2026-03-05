// java
package fitSnap.product_service.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fitSnap.product_service.dto.request.ProductRequest;
import fitSnap.product_service.dto.response.ProductResponse;
import fitSnap.product_service.entity.Product;
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
    private final GeminiClothService geminiClothService;

    public ProductResponse create(MultipartFile image) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("Creating product for user: {}", userId);

        // 1. Call Gemini to analyze + segment
        GeminiClothResult clothResult = geminiClothService.analyzeCloth(image.getBytes());
        if (clothResult == null) {
            throw new RuntimeException("Gemini returned no result");
        }

        // 2. Upload segmented image to S3
        String imageUrl = s3Service.uploadBytes(
                Optional.ofNullable(clothResult.getSegmentedImage()).orElse(image.getBytes()),
                "products/" + userId,
                "image/png");

        // 3. Build product from Gemini metadata
        Product product = new Product();
        product.setUserId(userId);
        product.setImageUrl(imageUrl);

        // Build name from Gemini metadata, or default
        String name = Optional.ofNullable(clothResult.getDescription())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> {
                    String composed = String.join(
                            " ",
                            Optional.ofNullable(clothResult.getColor()).orElse(""),
                            Optional.ofNullable(clothResult.getType()).orElse(""),
                            Optional.ofNullable(clothResult.getCategory()).orElse(""));
                    composed = composed.trim().replaceAll("\\s+", " ");
                    return composed.isEmpty() ? "Untitled product" : composed;
                });
        product.setName(name);

        if (clothResult.getCategory() != null) product.setCategory(clothResult.getCategory());
        if (clothResult.getType() != null) product.setType(clothResult.getType());
        if (clothResult.getColor() != null) product.setColor(clothResult.getColor());
        if (clothResult.getMaterial() != null) product.setMaterial(clothResult.getMaterial());
        if (clothResult.getPattern() != null) product.setPattern(clothResult.getPattern());
        if (clothResult.getStyle() != null) product.setStyle(clothResult.getStyle());
        if (clothResult.getFit() != null) product.setFit(clothResult.getFit());
        if (clothResult.getDescription() != null) product.setDescription(clothResult.getDescription());

        // 4. Save and return response
        product = productRepository.save(product);
        log.info("Product created successfully with id: {}", product.getId());
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

        // Verify ownership
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this product");
        }

        // Update fields
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

        // Verify ownership
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this product");
        }

        // Delete image from S3
        s3Service.deleteFile(product.getImageUrl());

        // Delete from DB
        productRepository.delete(product);

        log.info("Product deleted successfully");
    }
}
