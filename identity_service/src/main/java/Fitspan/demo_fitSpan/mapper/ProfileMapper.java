package Fitspan.demo_fitSpan.mapper;

import Fitspan.demo_fitSpan.dto.request.ProfileRequest;
import Fitspan.demo_fitSpan.dto.request.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileRequest toProfileRequest(UserRequest request);
}
