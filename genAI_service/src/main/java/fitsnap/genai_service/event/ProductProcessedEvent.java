package fitsnap.genAI_service.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Setter
@Getter
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductProcessedEvent {
    String id;
    String userId;
    String name;
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
