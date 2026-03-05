package Fitspan.demo_fitSpan.service;

import Fitspan.demo_fitSpan.dto.request.PermissionRequest;
import Fitspan.demo_fitSpan.dto.response.PermissionResponse;
import Fitspan.demo_fitSpan.entity.Permission;
import Fitspan.demo_fitSpan.mapper.PermissionMapper;
import Fitspan.demo_fitSpan.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionMapper permissionMapper;
    PermissionRepository permissionRepository;
    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }
    public void delete(String permissionName){
        permissionRepository.deleteById(permissionName);

    }
    public List<PermissionResponse>getAll(){
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }
}
