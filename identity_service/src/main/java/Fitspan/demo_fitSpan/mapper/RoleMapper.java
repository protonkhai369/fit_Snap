package Fitspan.demo_fitSpan.mapper;

import Fitspan.demo_fitSpan.dto.request.RoleRequest;
import Fitspan.demo_fitSpan.dto.response.RoleResponse;
import Fitspan.demo_fitSpan.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole (RoleRequest request);

    RoleResponse toRoleResponse (Role role);
}
