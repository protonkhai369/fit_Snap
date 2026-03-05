package Fitspan.demo_fitSpan.repository;

import Fitspan.demo_fitSpan.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,String> {

    String findByName(String name);
}
