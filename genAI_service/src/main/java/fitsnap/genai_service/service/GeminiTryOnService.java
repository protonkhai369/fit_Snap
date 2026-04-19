package fitsnap.genAI_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GeminiTryOnService {

    @Value("${genai.api-key}")
    private String apiKey;

    // Gemini model image support
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent";

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] generateTryOn(String prompt, byte[] personImage, byte[] clothImage) {

        RestTemplate rest = new RestTemplate();

        // Convert images to Base64
        String personBase64 = Base64.getEncoder().encodeToString(personImage);
        String clothBase64 = Base64.getEncoder().encodeToString(clothImage);

        // Image parts
        Map<String, Object> personPart = Map.of(
                "inlineData", Map.of(
                        "mimeType", "image/png",
                        "data", personBase64
                )
        );

        Map<String, Object> clothPart = Map.of(
                "inlineData", Map.of(
                        "mimeType", "image/png",
                        "data", clothBase64
                )
        );

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", prompt),
                                        personPart,
                                        clothPart
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-goog-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        String rawResponse = rest.postForObject(GEMINI_URL, request, String.class);

        if (rawResponse == null) {
            throw new RuntimeException("Gemini returned NULL response");
        }

        try {
            JsonNode json = mapper.readTree(rawResponse);

            JsonNode parts = json
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts");

            for (JsonNode part : parts) {
                if (part.has("inlineData")) {
                    String base64 = part.get("inlineData").get("data").asText();
                    return Base64.getDecoder().decode(base64);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Gemini response: " + rawResponse, e
            );
        }

        throw new RuntimeException("Gemini did not return an image");
    }
}
