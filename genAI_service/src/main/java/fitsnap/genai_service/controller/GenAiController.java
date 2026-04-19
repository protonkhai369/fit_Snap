package fitsnap.genAI_service.controller;

import fitsnap.genAI_service.service.GeminiTryOnService;
import fitsnap.genai_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/genai")
@RequiredArgsConstructor
public class GenAiController {

    private final GeminiTryOnService tryOnService;
    private final S3Service s3Service;

    @PostMapping(
            value = "/tryon",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.IMAGE_PNG_VALUE
    )
    @ResponseBody
    public byte[] tryOn(
            @RequestPart("person") MultipartFile person,      // FE upload
            @RequestParam("clothUrl") String clothUrl         // cloth lấy từ S3
    ) throws IOException {

        byte[] personBytes = person.getBytes();

        byte[] clothBytes = s3Service.downloadByUrl(clothUrl);

        String prompt = """
You are an advanced virtual try-on image generation model.

If multiple requests use the same person image but different clothing images,
then each request must produce a visually different result that clearly reflects the new garment.
The person must stay the same, only the clothing changes.

Two images are provided to you for this request ONLY:
- Image 1: The person image.
- Image 2: The clothing image (upper-body or fullbody garment).

Your task:
Generate a hyper-realistic virtual try-on result where the person is wearing the garment from the clothing image.

Strict Requirements:

1. PERSON PRESERVATION
- Keep the person’s face, hair, skin tone, pose, body shape, and background 100% unchanged.
- Do NOT modify facial expressions, body proportions, hairstyle, or environment.

2. GARMENT TRANSFER
- Replace ONLY the person’s upper-body garment with the clothing from Image 2.
- Maintain accurate collar shape, sleeve length, garment outline, and chest width.
- Do NOT alter pants, skirts, accessories, or any unrelated regions.

3. NATURAL FIT & ALIGNMENT
- Align the garment realistically to the person's body.
- Respect shoulder direction, arm position, and torso orientation.
- Apply accurate warping and perspective adjustments.

4. FABRIC DETAIL PRESERVATION
- Preserve the garment’s texture, patterns, logos, stitching, wrinkles, and colors.
- Keep all details sharp and undistorted.

5. LIGHTING & SHADOW INTEGRATION
- Match the lighting, shadows, and reflectance with the original person image.
- Ensure the garment blends seamlessly into the scene.

6. ARTIFACT-FREE RENDERING
Avoid:
- blurry areas
- duplicated limbs
- ghosting artifacts
- distorted body shapes
- mismatched tones
- unnatural edges

7. OUTPUT FORMAT
- Output ONLY the final virtual try-on result.
- No additional elements, borders, text, or captions.
- High-resolution, photorealistic output.

8. PRIVACY, MEMORY, AND DATA HANDLING (IMPORTANT)
- You MUST NOT store, memorize, learn, or retain any user image or metadata.
- You MUST operate in a fully stateless mode.
- You MUST NOT use any information from previous requests.
- After generating the output image, permanently FORGET ALL input images and intermediate data.
- Immediately flush and erase all internal buffers, caches, embeddings, or representations of user data.
- Do NOT use any past request content in any form.

Return ONLY the final try-on image.
""";



      
        return tryOnService.generateTryOn(
                prompt,
                personBytes,
                clothBytes
        );
    }
}
