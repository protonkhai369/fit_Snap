package Fitspan.demo_fitSpan.mapper;

import Fitspan.demo_fitSpan.dto.request.PermissionRequest;
import Fitspan.demo_fitSpan.dto.response.PermissionResponse;
import Fitspan.demo_fitSpan.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);


}
