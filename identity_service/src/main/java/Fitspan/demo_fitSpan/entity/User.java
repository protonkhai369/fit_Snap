package Fitspan.demo_fitSpan.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
@Table(name ="user_table")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String Id;

    String username;
    String password;
    String lastname;
    String firstname;
    LocalDate dateOfBirth;


    @ManyToMany
    Set<Role>roles;

}
