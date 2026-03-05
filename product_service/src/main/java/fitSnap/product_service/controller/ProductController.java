package fitSnap.product_service.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import fitSnap.product_service.dto.ApiResponse;
import fitSnap.product_service.dto.request.ProductRequest;
import fitSnap.product_service.dto.response.ProductResponse;
import fitSnap.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestPart("image") MultipartFile image) throws IOException {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.create(image))
                .build();
    }

    @GetMapping("/my-products")
    public ApiResponse<List<ProductResponse>> getMyProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getMyProducts())
                .build();
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getById(productId))
                .build();
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable String productId, @RequestBody ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.update(productId, request))
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@PathVariable String productId) {
        productService.delete(productId);
        return ApiResponse.<Void>builder()
                .message("Product deleted successfully")
                .build();
    }
}
