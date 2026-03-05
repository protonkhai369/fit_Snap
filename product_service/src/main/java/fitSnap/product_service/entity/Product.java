package fitSnap.product_service.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String userId;

    String name;
    String category;
    String type;
    String color;
    String material;
    String pattern;
    String style;
    String season;
    String gender;
    String fit;
    String description;

    // Link ảnh lưu ở S3
    String imageUrl;
}
