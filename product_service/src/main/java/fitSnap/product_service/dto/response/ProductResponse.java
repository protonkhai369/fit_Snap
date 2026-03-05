package fitSnap.product_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Setter
@Getter
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    String id;
    String userId;
    String category;
    String type;
    String color;
    String material;
    String pattern;
    String style;
    String fit;
    String description;
    String imageUrl;
}
