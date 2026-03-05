package fitSnap.product_service.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadBytes(byte[] data, String folder, String contentType) {
        String key = folder + "/" + java.util.UUID.randomUUID() + ".png";

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putReq, RequestBody.fromBytes(data));

        return "https://" + bucketName + ".s3.amazonaws.com/" + key;
    }

    public String generatePresignedUrl(String rawUrl, Duration expiry) {
        try {
            String key = extractKeyFromUrl(rawUrl);

            if (key == null) {
                log.error("Failed to extract S3 key from URL: {}", rawUrl);
                return rawUrl;
            }

            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder().bucket(bucketName).key(key).build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(expiry)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);

            return presigned.url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL for {}: {}", rawUrl, e.getMessage());
            return rawUrl;
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            if (key == null) return;

            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucketName).key(key).build());

            log.info("Deleted from S3: {}", key);

        } catch (Exception e) {
            log.error("Failed to delete S3 file {}", fileUrl, e);
        }
    }

    private String extractKeyFromUrl(String url) {
        try {
            // Tìm phần sau .amazonaws.com/
            int idx = url.indexOf(".amazonaws.com/");
            if (idx == -1) return null;

            String key = url.substring(idx + ".amazonaws.com/".length());
            return URLDecoder.decode(key, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("extractKeyFromUrl error: {}", e.getMessage());
            return null;
        }
    }
}
