package fitsnap.profile_service.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Setter
@Getter
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponse {
    String firstname;
    String lastname;
    LocalDate dateOfBirth;
    String avatar;
    double height;
    double weight;
    String gender;
    Integer age;
    String job;
    List<String> styles;
    List<String> colors;
    List<String> purposes;
}
