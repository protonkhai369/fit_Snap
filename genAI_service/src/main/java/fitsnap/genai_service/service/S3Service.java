package fitsnap.genai_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public byte[] downloadByUrl(String url) {
        String key = extractKeyFromUrl(url);

        if (key == null) {
            throw new IllegalArgumentException("Cannot extract S3 key from URL: " + url);
        }

        log.info(" Download from S3 bucket={} key={}", bucketName, key);

        ResponseBytes<GetObjectResponse> bytes =
                s3Client.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());

        return bytes.asByteArray();
    }

    private String extractKeyFromUrl(String url) {
        try {
            int idx = url.indexOf(".amazonaws.com/");
            if (idx == -1) {
                log.error("Invalid S3 URL: {}", url);
                return null;
            }

            String keyWithParams = url.substring(idx + ".amazonaws.com/".length());

            int qIndex = keyWithParams.indexOf("?");
            String key = (qIndex != -1)
                    ? keyWithParams.substring(0, qIndex)
                    : keyWithParams;

            key = URLDecoder.decode(key, StandardCharsets.UTF_8);

            log.info(" Extracted key: {}", key);
            return key;

        } catch (Exception e) {
            log.error("extractKeyFromUrl ERROR: {}", e.getMessage());
            return null;
        }
    }
}
