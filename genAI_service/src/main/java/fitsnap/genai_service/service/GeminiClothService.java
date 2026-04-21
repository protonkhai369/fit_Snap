package fitsnap.genAI_service.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiClothService {

    @Value("${genai.api-key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final ObjectMapper objectMapper;
    private final RemoveBackgroundService removeBackgroundService;

    // Safety padding (30/1000 = 3%)
    private static final int SAFETY_PADDING = 30;

    public GeminiClothResult analyzeCloth(byte[] imageBytes) {
        // FIX 1: Validate input immediately
        if (imageBytes == null || imageBytes.length == 0) {
            throw new RuntimeException("Input image is empty or null");
        }

        RestTemplate rest = new RestTemplate();

        Map<String, Object> inlineImage =
                Map.of("mimeType", "image/png", "data", Base64.getEncoder().encodeToString(imageBytes));

        String prompt =
                """
		You are a precise fashion AI.
		Analyze the clothing item in the image.

		Tasks:
		1. Identify the main garment.
		2. Detect the Bounding Box covering the **ENTIRE** garment.
		- MUST include: collar, long sleeves (if any), hemline, and buttons.
		- Do NOT cut off any part of the fabric.
		- Coordinates must be on a 0-1000 scale.

		STRICT JSON FORMAT:
		{
		"metadata": {
			"category": "<upper|lower|full>",
			"type": "<t-shirt|hoodie|jeans|dress|...>",
			"color": "<primary color>",
			"material": "<cotton|denim|...>",
			"pattern": "<...>",
			"style": "<...>",
			"fit": "<...>",
			"description": "<short description>"
		},
		"box_2d": [ymin, xmin, ymax, xmax]
		}
		""";

        Map<String, Object> generationConfig = Map.of("maxOutputTokens", 2000, "responseMimeType", "application/json");

        Map<String, Object> body = Map.of(
                "contents",
                List.of(Map.of("parts", List.of(Map.of("text", prompt), Map.of("inlineData", inlineImage)))),
                "generationConfig",
                generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        try {
            String response = rest.postForObject(GEMINI_URL, request, String.class);
            JsonNode root = objectMapper.readTree(response);

            String jsonText = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
            JsonNode jsonResult = objectMapper.readTree(jsonText);
            JsonNode metadata = jsonResult.path("metadata");

            byte[] finalImage = imageBytes;

            if (jsonResult.has("box_2d")) {
                JsonNode box = jsonResult.get("box_2d");
                int ymin = box.get(0).asInt();
                int xmin = box.get(1).asInt();
                int ymax = box.get(2).asInt();
                int xmax = box.get(3).asInt();

                // Attempt cropping
                finalImage = cropImageWithPadding(imageBytes, xmin, ymin, xmax, ymax);
            }

            // Apply Remove Background
            finalImage = removeBackgroundService.removeBackground(finalImage);
            log.info("Background removed, keeping only clothing");

            return GeminiClothResult.builder()
                    .segmentedImage(finalImage)
                    .category(metadata.path("category").asText(null))
                    .type(metadata.path("type").asText(null))
                    .color(metadata.path("color").asText(null))
                    .material(metadata.path("material").asText(null))
                    .pattern(metadata.path("pattern").asText(null))
                    .style(metadata.path("style").asText(null))
                    .fit(metadata.path("fit").asText(null))
                    .description(metadata.path("description").asText(null))
                    .build();

        } catch (Exception e) {
            log.error("Gemini Error: ", e);
            // Return original image on failure to avoid breaking the flow
            return GeminiClothResult.builder().segmentedImage(imageBytes).build();
        }
    }

    private byte[] cropImageWithPadding(byte[] originalBytes, int xmin, int ymin, int xmax, int ymax) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(originalBytes);
            BufferedImage src = ImageIO.read(bais);

            // FIX 2: CRITICAL NULL CHECK
            // This prevents "Cannot invoke .getWidth() because src is null"
            if (src == null) {
                log.warn("ImageIO failed to read the image bytes. Skipping crop and using original image.");
                return originalBytes;
            }

            int width = src.getWidth();
            int height = src.getHeight();

            // 1. Expand padding
            int padXMin = Math.max(0, xmin - SAFETY_PADDING);
            int padYMin = Math.max(0, ymin - SAFETY_PADDING);
            int padXMax = Math.min(1000, xmax + SAFETY_PADDING);
            int padYMax = Math.min(1000, ymax + SAFETY_PADDING);

            // 2. Convert to pixels
            int realX = (int) Math.floor((padXMin / 1000.0) * width);
            int realY = (int) Math.floor((padYMin / 1000.0) * height);
            int realX2 = (int) Math.ceil((padXMax / 1000.0) * width);
            int realY2 = (int) Math.ceil((padYMax / 1000.0) * height);

            int realW = realX2 - realX;
            int realH = realY2 - realY;

            // 3. Bounds check
            if (realX + realW > width) realW = width - realX;
            if (realY + realH > height) realH = height - realY;

            if (realW <= 0 || realH <= 0) return originalBytes;

            BufferedImage cropped = src.getSubimage(realX, realY, realW, realH);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(cropped, "png", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error cropping image with padding", e);
            return originalBytes;
        }
    }
}
