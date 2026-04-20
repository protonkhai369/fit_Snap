package Fitspan.demo_fitSpan.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreatedEvent {
    String userId;
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
