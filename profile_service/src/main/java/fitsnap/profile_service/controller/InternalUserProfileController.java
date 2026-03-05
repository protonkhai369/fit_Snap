package fitsnap.profile_service.controller;

import org.springframework.web.bind.annotation.*;

import fitsnap.profile_service.dto.ApiResponse;
import fitsnap.profile_service.dto.request.ProfileRequest;
import fitsnap.profile_service.dto.response.ProfileResponse;
import fitsnap.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserProfileController {

    ProfileService profileService;

    @PostMapping("/internal/users")
    ApiResponse<ProfileResponse> createProfile(@RequestBody ProfileRequest request) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.create(request))
                .build();
    }

    @GetMapping("/internal/users/{userId}")
    ApiResponse<ProfileResponse> getProfile(@PathVariable String userId) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.getById(userId))
                .build();
    }
}
