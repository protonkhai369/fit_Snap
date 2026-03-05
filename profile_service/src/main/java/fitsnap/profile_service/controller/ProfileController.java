package fitsnap.profile_service.controller;

import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import fitsnap.profile_service.dto.ApiResponse;
import fitsnap.profile_service.dto.request.UpdateProfileRequest;
import fitsnap.profile_service.dto.response.ProfileResponse;
import fitsnap.profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ProfileController {
    ProfileService userProfileService;

    @GetMapping("/users")
    List<ProfileResponse> getAllProfiles() {
        return userProfileService.getAll();
    }

    @GetMapping("/users/{profileId}")
    ApiResponse<ProfileResponse> getProfile(@PathVariable String profileId) {
        return ApiResponse.<ProfileResponse>builder()
                .result(userProfileService.getById(profileId))
                .build();
    }

    @GetMapping("/users/my-profile")
    ApiResponse<ProfileResponse> getMyProfile() {
        return ApiResponse.<ProfileResponse>builder()
                .result(userProfileService.getMyProfile())
                .build();
    }

    @PutMapping("/users/my-profile-update")
    ApiResponse<ProfileResponse> updateMyProfile(@RequestBody UpdateProfileRequest request) {
        return ApiResponse.<ProfileResponse>builder()
                .result(userProfileService.update(request))
                .build();
    }

    @PutMapping("/users/avatar")
    ApiResponse<ProfileResponse> updateAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<ProfileResponse>builder()
                .result(userProfileService.updateAvatar(file))
                .build();
    }
}
