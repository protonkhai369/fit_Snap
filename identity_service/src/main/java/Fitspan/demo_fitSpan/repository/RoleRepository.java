package Fitspan.demo_fitSpan.repository;

import Fitspan.demo_fitSpan.dto.request.RoleRequest;
import Fitspan.demo_fitSpan.dto.response.RoleResponse;
import Fitspan.demo_fitSpan.entity.Role;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

}
