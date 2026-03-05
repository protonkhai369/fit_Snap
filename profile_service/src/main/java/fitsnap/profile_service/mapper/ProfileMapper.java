package fitsnap.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import fitsnap.profile_service.dto.request.ProfileRequest;
import fitsnap.profile_service.dto.request.UpdateProfileRequest;
import fitsnap.profile_service.dto.response.ProfileResponse;
import fitsnap.profile_service.entity.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    Profile toProfile(ProfileRequest request);

    ProfileResponse toProfileResponse(Profile profile);

    void updateProfileFromRequest(@MappingTarget Profile profile, UpdateProfileRequest request);
}
