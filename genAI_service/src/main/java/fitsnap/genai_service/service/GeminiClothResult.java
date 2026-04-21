package fitsnap.genAI_service.service;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeminiClothResult {

    // Ảnh đã tách nền (byte array của PNG)
    byte[] segmentedImage;

    String category;
    String type;
    String color;
    String material;
    String pattern;
    String style;
    String fit;
    String description;
}
