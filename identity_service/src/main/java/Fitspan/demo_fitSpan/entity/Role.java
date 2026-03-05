package Fitspan.demo_fitSpan.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name ="role_table")
public class Role {
    @Id
    String name;

    String description;

    @ManyToMany
    Set<Permission> permissions;

}
