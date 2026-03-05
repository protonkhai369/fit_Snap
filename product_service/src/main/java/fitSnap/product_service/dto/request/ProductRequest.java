package fitSnap.product_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Setter
@Getter
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {

    String category;
    String type;
    String color;
    String material;
    String pattern;
    String style;
    String fit;
    String description;
}
