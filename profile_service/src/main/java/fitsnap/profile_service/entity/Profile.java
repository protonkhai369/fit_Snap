package fitsnap.profile_service.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "profile_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String profileId;

    String userId;
    String avatar;
    String firstname;
    String lastname;
    LocalDate dateOfBirth;
    double height;
    double weight;
    String gender;
    Integer age;
    String job;
    List<String> styles;
    List<String> colors;
    List<String> purposes;
}
