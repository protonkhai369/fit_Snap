package fitsnap.genAI_service.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Setter
@Getter
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreationEvent {
    String id;
    String userId;
    String imageUrl;
}
