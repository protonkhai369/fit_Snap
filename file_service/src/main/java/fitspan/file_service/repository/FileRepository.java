package fitspan.file_service.repository;

import fitspan.file_service.dto.FileInfo;
import fitspan.file_service.entity.FileMgmt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

@Repository
public class FileRepository {

    @Value("${app.file.download-prefix}")
    private String downloadPrefix;

    @Autowired
    private S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    String bucketName;

    public FileRepository(S3Client s3Client) {
        this.s3Client = s3Client;
    }
    public FileInfo store(MultipartFile multipartFile) throws IOException {
        String fileExtension = Objects.requireNonNull(
                multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf('.') + 1),
                "bin"
        );
        String fileName = UUID.randomUUID() + "." + fileExtension;//key trong s3

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(multipartFile.getContentType())
                        .build(),
                RequestBody.fromBytes(multipartFile.getBytes())
        );
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String url = downloadPrefix + encodedFileName;

        return FileInfo.builder()
                .name(fileName)
                .size(multipartFile.getSize())
                .contentType(multipartFile.getContentType())
                .md5CheckSum(DigestUtils.md5DigestAsHex(multipartFile.getBytes()))
                .url(url)
                .path(fileName)
                .build();
    }
    public Resource read(FileMgmt fileMgmt) throws IOException {
        String key = fileMgmt.getPath(); // key chính là tên file trong S3

        var response = s3Client.getObject(
                b -> b.bucket(bucketName).key(key)
        );

        byte[] bytes = response.readAllBytes();
        return new ByteArrayResource(bytes);
    }


}
