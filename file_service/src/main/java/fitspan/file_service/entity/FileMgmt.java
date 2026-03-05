package fitspan.file_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
@Table(name ="fileMgmt")
public class FileMgmt {
    @Id
    String id;

    String contentType;
    Long size;
    String md5CheckSum;
    String path;
    String ownedID;
}
