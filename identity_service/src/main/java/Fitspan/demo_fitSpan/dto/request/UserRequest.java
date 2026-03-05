package Fitspan.demo_fitSpan.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    String username;
    String password;
    String lastname;
    String firstname;
    LocalDate dateOfBirth;
    double height;
    double weight;
    String gender;

    String job;
    List<String> styles;
    List<String> colors;
    List<String> purposes;
}
