package Fitspan.demo_fitSpan.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Builder
@Setter
@Getter
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ProfileResponse {
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
