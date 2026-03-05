package Fitspan.demo_fitSpan.mapper;

import Fitspan.demo_fitSpan.dto.request.UserRequest;
import Fitspan.demo_fitSpan.dto.request.UserUpdateRequest;
import Fitspan.demo_fitSpan.dto.response.UserResponse;
import Fitspan.demo_fitSpan.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRequest request);
    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
