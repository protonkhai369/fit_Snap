package fitsnap.profile_service.service;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fitsnap.profile_service.dto.request.ProfileRequest;
import fitsnap.profile_service.dto.request.UpdateProfileRequest;
import fitsnap.profile_service.dto.response.ProfileResponse;
import fitsnap.profile_service.entity.Profile;
import fitsnap.profile_service.exception.AppException;
import fitsnap.profile_service.exception.ErrorCode;
import fitsnap.profile_service.mapper.ProfileMapper;
import fitsnap.profile_service.repository.ProfileRepository;
import fitsnap.profile_service.repository.httpClient.FileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {

    ProfileRepository profileRepository;

    ProfileMapper profileMapper;
    FileClient fileClient;

    public ProfileResponse create(ProfileRequest request) {
        Profile profile = profileMapper.toProfile(request);
        profile = profileRepository.save(profile);
        return profileMapper.toProfileResponse(profile);
    }

    public void delete(String id) {
        profileRepository.deleteById(id);
    }

    public ProfileResponse getById(String id) {
        Profile profile =
                profileRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOTEXISITED));
        return profileMapper.toProfileResponse(profile);
    }

    public List<ProfileResponse> getAll() {
        return profileRepository.findAll().stream()
                .map(profileMapper::toProfileResponse)
                .toList();
    }

    public ProfileResponse getMyProfile() {
        var context = SecurityContextHolder.getContext().getAuthentication();
        var userId = context.getName();
        Profile profile =
                profileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOTEXISITED));

        return profileMapper.toProfileResponse(profile);
    }

    public ProfileResponse update(UpdateProfileRequest request) {
        var context = SecurityContextHolder.getContext().getAuthentication();
        var userId = context.getName();
        Profile profile =
                profileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOTEXISITED));
        profileMapper.updateProfileFromRequest(profile, request);
        return profileMapper.toProfileResponse(profileRepository.save(profile));
    }

    public ProfileResponse updateAvatar(MultipartFile file) throws IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        var profile =
                profileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOTEXISITED));

        var response = fileClient.uploadMedia(file);

        profile.setAvatar(response.getResult().getUrl());

        return profileMapper.toProfileResponse(profileRepository.save(profile));
    }
}
