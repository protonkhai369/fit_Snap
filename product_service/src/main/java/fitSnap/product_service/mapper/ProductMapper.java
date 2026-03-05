package fitSnap.product_service.mapper;

import org.mapstruct.Mapper;

import fitSnap.product_service.dto.request.ProductRequest;
import fitSnap.product_service.dto.response.ProductResponse;
import fitSnap.product_service.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductRequest request);

    ProductResponse toProductResponse(Product product);
}
