package fitSnap.product_service.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
@Service
public class RemoveBackgroundService {

    @Value("${removebg.api-key:}")
    private String removebgApiKey;

    private static final String REMOVEBG_API_URL = "https://api.remove.bg/v1.0/removebg";
    private static final String REMOVEBG_MODEL = "general_1_5";

    private final OkHttpClient httpClient = new OkHttpClient();

    public byte[] removeBackground(byte[] imageBytes) {
        // Nếu không có API key, trả về ảnh gốc
        if (removebgApiKey == null || removebgApiKey.isBlank()) {
            log.warn("Remove.bg API key not configured, returning original image");
            return imageBytes;
        }

        try {
            // Tạo multipart request với type "clothing" để tách riêng quần áo
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "image_file", "image.png", RequestBody.create(imageBytes, MediaType.parse("image/png")))
                    .addFormDataPart("model", REMOVEBG_MODEL)
                    .addFormDataPart("format", "png")
                    .addFormDataPart("type", "clothing")
                    .addFormDataPart("size", "auto")
                    .build();

            Request request = new Request.Builder()
                    .url(REMOVEBG_API_URL)
                    .addHeader("X-Api-Key", removebgApiKey)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Remove.bg API error: {} - {}", response.code(), response.message());
                    if (response.body() != null) {
                        log.error("Response body: {}", response.body().string());
                    }
                    return imageBytes;
                }

                if (response.body() == null) {
                    log.warn("Remove.bg API returned empty response");
                    return imageBytes;
                }

                byte[] result = response.body().bytes();
                log.info("Successfully extracted clothing, output size: {} bytes", result.length);
                return result;
            }

        } catch (IOException e) {
            log.error("Error calling Remove.bg API", e);
            return imageBytes;
        }
    }

    public void checkQuota() {
        if (removebgApiKey == null || removebgApiKey.isBlank()) {
            log.warn("Remove.bg API key not configured");
            return;
        }

        try {
            Request request = new Request.Builder()
                    .url("https://api.remove.bg/v1.0/account")
                    .addHeader("X-Api-Key", removebgApiKey)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String quota = response.body().string();
                    log.info("Remove.bg Account Info: {}", quota);
                } else {
                    log.error("Failed to check quota: {} - {}", response.code(), response.message());
                }
            }

        } catch (IOException e) {
            log.error("Error checking Remove.bg quota", e);
        }
    }
}
